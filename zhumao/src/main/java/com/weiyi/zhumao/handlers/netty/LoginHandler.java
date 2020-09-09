package com.weiyi.zhumao.handlers.netty;

// import java.net.InetSocketAddress;
import java.net.SocketAddress;
// import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import lombok.extern.slf4j.Slf4j;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.app.Player;
import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.app.impl.DefaultPlayer;
import com.weiyi.zhumao.communication.NettyTCPMessageSender;
import com.weiyi.zhumao.entity.Shop;
import com.weiyi.zhumao.entity.User;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.impl.ReconnetEvent;
import com.weiyi.zhumao.game.CustomMatchmaker;
// import com.weiyi.zhumao.game.SyncTask;
import com.weiyi.zhumao.service.SessionRegistryService;
import com.weiyi.zhumao.service.TaskManagerService;
// import com.weiyi.zhumao.service.UniqueIDGeneratorService;
import com.weiyi.zhumao.service.impl.ReconnectSessionRegistry;
import com.weiyi.zhumao.service.impl.ShopService;
import com.weiyi.zhumao.service.impl.UserService;

// import com.weiyi.zhumao.util.JetConfig;
import com.weiyi.zhumao.util.NettyUtils;
import com.weiyi.zhumao.util.RandomString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Sharable
@Component
public class LoginHandler extends SimpleChannelInboundHandler<Object> {
	// Autowired
	@Autowired
	UserService userService;
	@Autowired
	ShopService shopService;

	@Autowired
	CustomMatchmaker customMatchmaker;
	@Autowired
	TaskManagerService taskManagerService;

	protected SessionRegistryService<SocketAddress> udpSessionRegistry;
	protected ReconnectSessionRegistry reconnectRegistry;

	// @Autowired
	// protected UniqueIDGeneratorService idGeneratorService;

	/**
	 * Used for book keeping purpose. It will count all open channels. Currently
	 * closed channels will not lead to a decrement.
	 */
	private static final AtomicInteger CHANNEL_COUNTER = new AtomicInteger(0);

	public void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
		final Event event = (Event) msg;
		ByteBuf buffer = (ByteBuf) event.getSource();
		final Channel channel = ctx.channel();
		int type = event.getType();
		if (Events.LOG_IN == type) {
			log.debug("Login attempt from {}", channel.remoteAddress());
			Player player = createPlayer(buffer, channel);
			handleLogin(player, channel, buffer);
		} else if (Events.RECONNECT == type) {
			log.debug("Reconnect attempt from {}", channel.remoteAddress());
			String reconnectKey = NettyUtils.readString(buffer);
			PlayerSession playerSession = lookupSession(reconnectKey);
			handleReconnect(playerSession, channel, buffer);
		} else if (Events.GET_TOKEN == type) {
			String token = RandomString.getAlphaNumericString(10);
			var user = userService.register("Guest", token);
			shopService.register(user.getId(), "0,1","0");

			ByteBuf rebuffer = Unpooled.wrappedBuffer(NettyUtils.createBufferForOpcode(Events.GET_TOKEN_SUCCESS),
					NettyUtils.writeString(token));
			log.debug("now write {} back to client", token);
			channel.writeAndFlush(rebuffer);
		} 
		else if(Events.GAME_ROOM_JOIN==type){
			//由下一个JoinRoomHandler处理
			ctx.fireChannelRead(msg);
		}
		else if(Events.CHANGE_CAR==type){
			String token = NettyUtils.readString(buffer);
			User user = userService.getUserByToken(token);
			Shop shop = shopService.getShopById(user.getId());
			String currentCar = NettyUtils.readString(buffer);
			if(!currentCar.equals(shop.getCurrentCar())){
				shop.setCurrentCar(currentCar);
				shopService.updateShop(shop);
			}
			ByteBuf rebuffer = Unpooled.wrappedBuffer(NettyUtils.createBufferForOpcode(Events.CHANGE_CAR_SUCCESS),
					NettyUtils.writeString(currentCar));
			channel.writeAndFlush(rebuffer);
		}
		else {
			log.error("Invalid event {} sent from remote address {}. " + "Going to close channel {}",
					new Object[] { event.getType(), channel.remoteAddress(), channel.id() });
			closeChannelWithLoginFailure(channel);
		}
	}

	// @Override
	// public void channelActive(ChannelHandlerContext ctx)
	// throws Exception {
	// // AbstractNettyServer.ALL_CHANNELS.add(ctx.channel());
	// log.debug("Added Channel with id: {} as the {}th open channel", ctx
	// .channel().id(), CHANNEL_COUNTER.incrementAndGet());
	// }

	public Player createPlayer(final ByteBuf buffer, final Channel channel) {
		String token = NettyUtils.readString(buffer);
		User user = userService.getUserByToken(token);
		if (user == null) {
			log.info("no user");
			return null;
		}
		return new DefaultPlayer(user.getId(), user.getName());
	}

	public PlayerSession lookupSession(final String reconnectKey) {
		PlayerSession playerSession = (PlayerSession) reconnectRegistry.getSession(reconnectKey);
		if (null != playerSession) {
			synchronized (playerSession) {
				// if its an already active session then do not allow a
				// reconnect. So the only state in which a client is allowed to
				// reconnect is if it is "NOT_CONNECTED"
				if (playerSession.getStatus() == Session.Status.NOT_CONNECTED) {
					playerSession.setStatus(Session.Status.CONNECTING);
				} else {
					playerSession = null;
				}
			}
		}
		return playerSession;
	}

	public void handleLogin(Player player, Channel channel, ByteBuf buffer) {
		if (null != player) {
			Shop shop = shopService.getShopById((long)player.getId());
			ByteBuf buf = NettyUtils.createBufferForOpcode(Events.LOG_IN_SUCCESS);
			buf.writeLong((long)player.getId());
			buf = Unpooled.wrappedBuffer(buf,NettyUtils.writeString(player.getName()));
			buf = Unpooled.wrappedBuffer(buf,NettyUtils.writeString(shop.getCars()));
			buf = Unpooled.wrappedBuffer(buf,NettyUtils.writeString(shop.getCurrentCar()));
			channel.writeAndFlush(buf);
			// handleGameRoomJoin(player, channel, buffer);
			channel.pipeline().addLast("JoinRoomHandler",new JoinRoomHandler(player, customMatchmaker));
		} else {
			// Write future and close channel
			closeChannelWithLoginFailure(channel);
		}
	}

	protected void handleReconnect(PlayerSession playerSession, Channel channel, ByteBuf buffer) {
		if (null != playerSession) {
			channel.write(NettyUtils.createBufferForOpcode(Events.LOG_IN_SUCCESS));
			GameRoom gameRoom = playerSession.getGameRoom();
			gameRoom.disconnectSession(playerSession);
			if (null != playerSession.getTcpSender())
				playerSession.getTcpSender().close();

			if (null != playerSession.getUdpSender())
				playerSession.getUdpSender().close();

			handleReJoin(playerSession, gameRoom, channel, buffer);
		} else {
			// Write future and close channel
			closeChannelWithLoginFailure(channel);
		}
	}

	/**
	 * Helper method which will close the channel after writing
	 * {@link Events#LOG_IN_FAILURE} to remote connection.
	 * 
	 * @param channel The tcp connection to remote machine that will be closed.
	 */
	private void closeChannelWithLoginFailure(Channel channel) {
		ChannelFuture future = channel.write(NettyUtils.createBufferForOpcode(Events.LOG_IN_FAILURE));
		future.addListener(ChannelFutureListener.CLOSE);
		channel.flush();
	}

	// public void handleGameRoomJoin(Player player, Channel channel, ByteBuf buffer) {

	// 	GameRoom gameRoom = testGameRoom;
	// 	if (null != gameRoom) {
	// 		PlayerSession playerSession = gameRoom.createPlayerSession(player);
	// 		gameRoom.onLogin(playerSession);
	// 		String reconnectKey = (String) idGeneratorService.generateFor(playerSession.getClass());
	// 		playerSession.setAttribute(JetConfig.RECONNECT_KEY, reconnectKey);
	// 		playerSession.setAttribute(JetConfig.RECONNECT_REGISTRY, reconnectRegistry);
	// 		log.trace("Sending GAME_ROOM_JOIN_SUCCESS to channel {}", channel.id());
	// 		ByteBuf reconnectKeyBuffer = Unpooled.wrappedBuffer(
	// 				NettyUtils.createBufferForOpcode(Events.GAME_ROOM_JOIN_SUCCESS),
	// 				NettyUtils.writeString(reconnectKey));
	// 		ChannelFuture future = channel.write(reconnectKeyBuffer);
	// 		connectToGameRoom(gameRoom, playerSession, future);
	// 		channel.flush();
	// 	}
	// }

	protected void handleReJoin(PlayerSession playerSession, GameRoom gameRoom, Channel channel, ByteBuf buffer) {
		log.trace("Going to clear pipeline");
		// Clear the existing pipeline
		NettyUtils.clearPipeline(channel.pipeline());
		// Set the tcp channel on the session.
		NettyTCPMessageSender sender = new NettyTCPMessageSender(channel);
		playerSession.setTcpSender(sender);
		// Connect the pipeline to the game room.
		gameRoom.connectSession(playerSession);
		playerSession.setWriteable(true);// TODO remove if unnecessary. It should be done in start event
		// Send the re-connect event so that it will in turn send the START event.
		playerSession.onEvent(new ReconnetEvent(sender));
		// loginUdp(playerSession, buffer);
	}

	// public void connectToGameRoom(final GameRoom gameRoom, final PlayerSession playerSession, ChannelFuture future) {
	// 	future.addListener(new ChannelFutureListener() {
	// 		@Override
	// 		public void operationComplete(ChannelFuture future) throws Exception {
	// 			Channel channel = future.channel();
	// 			log.debug("Sending GAME_ROOM_JOIN_SUCCESS to channel {} completed", channel.id());
	// 			if (future.isSuccess()) {
	// 				log.info("Going to clear pipeline");
	// 				// Clear the existing pipeline
	// 				NettyUtils.clearPipeline(channel.pipeline());
	// 				// Set the tcp channel on the session.
	// 				NettyTCPMessageSender sender = new NettyTCPMessageSender(channel);
	// 				playerSession.setTcpSender(sender);
	// 				// Connect the pipeline to the game room.
	// 				gameRoom.connectSession(playerSession);
	// 				// Send the connect event so that it will in turn send the START event.
	// 				playerSession.onEvent(Events.connectEvent(sender));
	// 				taskManagerService.scheduleAtFixedRate(new SyncTask(gameRoom), 0, 30, TimeUnit.MILLISECONDS);
	// 			} else {
	// 				log.error("GAME_ROOM_JOIN_SUCCESS message sending to client was failure, channel will be closed");
	// 				channel.close();
	// 			}
	// 		}
	// 	});
	// }

	/**
	 * This method adds the player session to the {@link SessionRegistryService}.
	 * The key being the remote udp address of the client and the session being the
	 * value.
	 * 
	 * @param playerSession
	 * @param buffer        Used to read the remote address of the client which is
	 *                      attempting to connect via udp.
	 */
	// protected void loginUdp(PlayerSession playerSession, ByteBuf buffer) {
	// InetSocketAddress remoteAdress = NettyUtils.readSocketAddress(buffer);
	// if (null != remoteAdress) {
	// udpSessionRegistry.putSession(remoteAdress, playerSession);
	// }
	// }

	// public UniqueIDGeneratorService getIdGeneratorService() {
	// 	return idGeneratorService;
	// }

	// public void setIdGeneratorService(UniqueIDGeneratorService idGeneratorService) {
	// 	this.idGeneratorService = idGeneratorService;
	// }

	public SessionRegistryService<SocketAddress> getUdpSessionRegistry() {
		return udpSessionRegistry;
	}

	public void setUdpSessionRegistry(SessionRegistryService<SocketAddress> udpSessionRegistry) {
		this.udpSessionRegistry = udpSessionRegistry;
	}

	public ReconnectSessionRegistry getReconnectRegistry() {
		return reconnectRegistry;
	}

	public void setReconnectRegistry(ReconnectSessionRegistry reconnectRegistry) {
		this.reconnectRegistry = reconnectRegistry;
	}

}
