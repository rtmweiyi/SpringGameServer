package com.weiyi.zhumao.game;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import com.weiyi.zhumao.app.impl.SimpleGame;
import com.weiyi.zhumao.app.impl.GameRoomSession.GameRoomSessionBuilder;
import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.app.Player;
import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.communication.NettyTCPMessageSender;
import com.weiyi.zhumao.config.AppProperties;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.protocols.impl.LobbyProtocol;
import com.weiyi.zhumao.protocols.impl.MessageBufferProtocol;
import com.weiyi.zhumao.service.SessionRegistryService;
import com.weiyi.zhumao.service.TaskManagerService;
import com.weiyi.zhumao.service.UniqueIDGeneratorService;
import com.weiyi.zhumao.service.impl.ReconnectSessionRegistry;
import com.weiyi.zhumao.service.impl.UserService;
import com.weiyi.zhumao.util.JetConfig;
import com.weiyi.zhumao.util.NettyUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableConfigurationProperties(AppProperties.class)
public class CustomMatchmaker {
    @Autowired
    protected UniqueIDGeneratorService idGeneratorService;
    @Autowired
	TaskManagerService taskManagerService;
	@Autowired
	MessageBufferProtocol messageBufferProtocol;
	@Autowired
	LobbyProtocol lobbyProtocol;
	@Autowired
	AppProperties appProperties;
	@Autowired
	UserService userService;

	@Autowired
	SessionRegistryService<SocketAddress> udpSessionRegistry;


	private int nums;
	private boolean checkPoint = false;
	
	AtomicLong roomId = new AtomicLong(0);

	@PostConstruct
	public void init(){
		nums = appProperties.getNumsInRoom();
	}
    
    //TODO Autowired
    protected ReconnectSessionRegistry reconnectRegistry;

	private Map<Player,Channel> playersInWaitingMap = new HashMap<>();
	

    public synchronized void addPlayerAndChannel(Player player,Channel channel){
		int playersNum = playersInWaitingMap.size();
		if(playersNum==0){
			checkPoint = true;
		}
		playersInWaitingMap.put(player, channel);	
        if(playersNum==nums){
			SimpleGameRoom gameRoom = createGameRoom(0);
			gameRoom.setUserService(userService);
            for(Map.Entry<Player,Channel> entry:playersInWaitingMap.entrySet()){
                Player _player = entry.getKey();
                Channel _channel = entry.getValue();
				handleGameRoomJoin(gameRoom,_player,_channel);
			}
			playersInWaitingMap.clear();
			var holder = taskManagerService.scheduleAtFixedRate(new SyncTask(gameRoom), 0, 50, TimeUnit.MILLISECONDS);
			gameRoom.setTaskHolder(holder);
			checkPoint = false;
        }
	}
	
	public synchronized void startGameWithRobots(){
		if(checkPoint){
			int playersNum = playersInWaitingMap.size();
			int robots = nums - playersNum;
			log.info("开始游戏:"+robots);
			SimpleGameRoom gameRoom = createGameRoom(robots);
			gameRoom.setUserService(userService);
			for (Map.Entry<Player, Channel> entry : playersInWaitingMap.entrySet()) {
				Player _player = entry.getKey();
				Channel _channel = entry.getValue();
				handleGameRoomJoin(gameRoom, _player, _channel);
			}
			playersInWaitingMap.clear();
			var holder = taskManagerService.scheduleAtFixedRate(new SyncTask(gameRoom), 0, 50, TimeUnit.MILLISECONDS);
			gameRoom.setTaskHolder(holder);
			checkPoint = false;
		}
	}

    public void handleGameRoomJoin(GameRoom gameRoom,Player player, Channel channel) {

		if (null != gameRoom) {
			PlayerSession playerSession = gameRoom.createPlayerSession(player);
			gameRoom.onLogin(playerSession);
			String reconnectKey = (String) idGeneratorService.generateFor(playerSession.getClass());
			playerSession.setAttribute(JetConfig.RECONNECT_KEY, reconnectKey);
			playerSession.setAttribute(JetConfig.RECONNECT_REGISTRY, reconnectRegistry);
			log.trace("Sending GAME_ROOM_JOIN_SUCCESS to channel {}", channel.id());
			ByteBuf reconnectKeyBuffer = Unpooled.wrappedBuffer(
					NettyUtils.createBufferForOpcode(Events.GAME_ROOM_JOIN_SUCCESS),
					NettyUtils.writeString(reconnectKey));
			ChannelFuture future = channel.write(reconnectKeyBuffer);
			loginUdp(playerSession, channel);
			connectToGameRoom(gameRoom, playerSession, future);
			channel.flush();
		}

		// else {
		// // Write failure and close channel.
		// ChannelFuture future =
		// channel.write(NettyUtils.createBufferForOpcode(Events.GAME_ROOM_JOIN_FAILURE));
		// future.addListener(ChannelFutureListener.CLOSE);
		// log.error("Invalid ref key provided by client: {}. Channel {} will be
		// closed", refKey, channel.id());
		// }
    }
    
    public void connectToGameRoom(final GameRoom gameRoom, final PlayerSession playerSession, ChannelFuture future) {
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				Channel channel = future.channel();
				log.debug("Sending GAME_ROOM_JOIN_SUCCESS to channel {} completed", channel.id());
				if (future.isSuccess()) {
					log.info("Going to clear pipeline");
					// Clear the existing pipeline
					NettyUtils.clearPipeline(channel.pipeline());
					// Set the tcp channel on the session.
					NettyTCPMessageSender tcpSender = new NettyTCPMessageSender(channel);
					playerSession.setTcpSender(tcpSender);
					// Connect the pipeline to the game room.
					gameRoom.connectSession(playerSession);
					// // Send the connect event so that it will in turn send the START event.
					// playerSession.onEvent(Events.connectEvent(sender));
					// send the start event to remote client.
					tcpSender.sendMessage(Events.event(null, Events.START));
				} else {
					log.error("GAME_ROOM_JOIN_SUCCESS message sending to client was failure, channel will be closed");
					channel.close();
				}
			}
		});
	}

	public SimpleGameRoom createGameRoom(int robots){
		GameRoomSessionBuilder sessionBuilder = new GameRoomSessionBuilder();
		long id = getNextId();
        sessionBuilder.parentGame(new SimpleGame(id,"defaultgame")).gameRoomName("defaultroom"+id).protocol(messageBufferProtocol).lobbyProtocol(lobbyProtocol);
        SimpleGameRoom gameroom = new SimpleGameRoom(sessionBuilder,nums,robots);
        return gameroom;
	}

	public long getNextId() {
        return roomId.incrementAndGet();
	}
	
	protected void loginUdp(PlayerSession playerSession, Channel channel) {
		var remoteAdress = channel.remoteAddress();
		if (null != remoteAdress) {
			udpSessionRegistry.putSession(remoteAdress, playerSession);
		}
	}

}