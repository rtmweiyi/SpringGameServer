package com.weiyi.zhumao.handlers.netty;

import java.net.SocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
// import io.netty.channel.MessageEvent;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;
import lombok.extern.slf4j.Slf4j;

import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.communication.MessageSender.Fast;
import com.weiyi.zhumao.communication.NettyUDPMessageSender;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.service.SessionRegistryService;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

@Slf4j
public class UDPUpstreamHandler extends SimpleChannelInboundHandler<Object>
{
	private SessionRegistryService<SocketAddress> udpSessionRegistry;
	public UDPUpstreamHandler()
	{
		super();
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object e)
			throws Exception
	{
		Channel channel = ctx.channel();
		// Get the session using the remoteAddress.
		SocketAddress remoteAddress = channel.remoteAddress();
		Session session = udpSessionRegistry.getSession(remoteAddress);
		if(null != session)
		{
			Event event = (Event)e;
			// If the session's UDP has not been connected yet then send a
			// CONNECT event.
			if (!session.isUDPEnabled())
			{
				event = getUDPConnectEvent(event, remoteAddress,
						(DatagramChannel) channel);
				// Pass the connect event on to the session
				session.onEvent(event);
			}
			else if (event.getType() == Events.CONNECT)
			{
				// Duplicate connect just discard.
				log.trace("Duplicate CONNECT {} received in UDP channel, "
						+ "for session: {} going to discard", event, session);
			}
			else
			{
				// Pass the original event on to the session
				session.onEvent(event);
			}
		}
		else
		{
			log.trace("Packet received from unknown source address: {}, going to discard",remoteAddress);
		}
	}

	public Event getUDPConnectEvent(Event event, SocketAddress remoteAddress,
			DatagramChannel udpChannel)
	{
		log.debug("Incoming udp connection remote address : {}",
				remoteAddress);
		
		if (event.getType() != Events.CONNECT)
		{
			log.warn("Going to discard UDP Message Event with type {} "
					+ "It will get converted to a CONNECT event since "
					+ "the UDP MessageSender is not initialized till now",
					event.getType());
		}
		Fast messageSender = new NettyUDPMessageSender(remoteAddress, udpChannel, udpSessionRegistry);
		Event connectEvent = Events.connectEvent(messageSender);
		
		return connectEvent;
	}

	public SessionRegistryService<SocketAddress> getUdpSessionRegistry()
	{
		return udpSessionRegistry;
	}

	public void setUdpSessionRegistry(
			SessionRegistryService<SocketAddress> udpSessionRegistry)
	{
		this.udpSessionRegistry = udpSessionRegistry;
	}
	
}
