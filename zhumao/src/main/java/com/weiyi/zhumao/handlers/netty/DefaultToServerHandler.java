package com.weiyi.zhumao.handlers.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

// import com.weiyi.zhumao.app.GameEvent;
import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.Event;

/**
 * This class will handle on the {@link GameEvent}s by forwarding message
 * events to the associated session instance.
 * 
 * @author Abraham Menacherry
 * 
 */
@Slf4j
public class DefaultToServerHandler extends SimpleChannelInboundHandler<Object>
{
	
	/**
	 * The player session associated with this stateful business handler.
	 */
	private final PlayerSession playerSession;

	public DefaultToServerHandler(PlayerSession playerSession)
	{
		super();
		this.playerSession = playerSession;
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception
	{
		log.error("Exception during network communication: {}.", ctx.channel().id());
		Event event = Events.event(cause, Events.EXCEPTION);
		playerSession.onEvent(event);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx)
			throws Exception
	{
		log.debug("Netty Channel {} is closed.", ctx.channel().id());
		if (!playerSession.isShuttingDown())
		{
			// Should not send close to session, since reconnection/other
			// business logic might be in place.
			Event event = Events.event(ctx, Events.DISCONNECT);
			playerSession.onEvent(event);
		}
	}

	public PlayerSession getPlayerSession()
	{
		return playerSession;
	}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        playerSession.onEvent((Event) msg);
    }

}
