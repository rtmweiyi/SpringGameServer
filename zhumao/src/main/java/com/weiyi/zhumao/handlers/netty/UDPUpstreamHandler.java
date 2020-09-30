package com.weiyi.zhumao.handlers.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.netty.buffer.ByteBuf;
// import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
// import io.netty.channel.MessageEvent;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.communication.MessageSender.Fast;
import com.weiyi.zhumao.communication.NettyMessageBuffer;
import com.weiyi.zhumao.communication.NettyUDPMessageSender;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.service.SessionRegistryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UDPUpstreamHandler extends SimpleChannelInboundHandler<DatagramPacket>
{
	private static final String UDP_CONNECTING = "UDP_CONNECTING";
	@Autowired
	@Qualifier("udpSessionRegistry")
	private SessionRegistryService<SocketAddress> udpSessionRegistry;

	@Autowired
	private MessageBufferEventDecoder messageBufferEventDecoder;
	public UDPUpstreamHandler()
	{
		super();
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
			throws Exception
	{
		// Get the session using the remoteAddress.
		SocketAddress remoteAddress = packet.sender();
		
		Session session = udpSessionRegistry.getSession(remoteAddress);
		if(null != session)
		{
			ByteBuf buffer = packet.content();
			//读一下长度
			int len = buffer.readInt();
			var buuf = buffer.readBytes(len);
			Event event = (Event) messageBufferEventDecoder
					.decode(null, buuf);
			// If the session's UDP has not been connected yet then send a
			// CONNECT event.
			if (!session.isUDPEnabled())
			{
				if (null == session.getAttribute(UDP_CONNECTING)
						|| (!(Boolean) session.getAttribute(UDP_CONNECTING))) 
				{
					session.setAttribute(UDP_CONNECTING, true);
					event = getUDPConnectEvent(event, remoteAddress,
							(DatagramChannel) ctx.channel());
					// Pass the connect event on to the session
					session.onEvent(event);
					while(buffer.readerIndex()<buffer.writerIndex()){
						int l = buffer.readInt();
						var b = buffer.readBytes(l);
						var e = (Event) messageBufferEventDecoder.decode(null, b);
						session.onEvent(e);
					}
				}
				else
				{
					log.info("Going to discard UDP Message Event with type {} "
							+ "the UDP MessageSender is not initialized fully",
							event.getType());
				}
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
				while(buffer.readerIndex()<buffer.writerIndex()){
					int l = buffer.readInt();
					var b = buffer.readBytes(l);
					var e = (Event) messageBufferEventDecoder.decode(null, b);
					session.onEvent(e);
				}
			}
		}
		else
		{
			try {
				log.info("哔哔哔 remoteAddress：" + remoteAddress);
				// 看看是不是连接命令
				ByteBuf buffer = packet.content();
				// 读长度
				buffer.readInt();
				// 读类型
				Event event = (Event) messageBufferEventDecoder.decode(null, buffer);
				if (event.getType() == Events.CONNECT) {
					// 返回客户端地址，等发起加入房间命令的时候再发送过来
					var eventContext = new NettyUDPMessageSender.EventContextImpl((InetSocketAddress) remoteAddress);
					var reBuf = new NettyMessageBuffer();
					reBuf.writeString(eventContext.getAttachment().getHostString());
					reBuf.writeInt(eventContext.getAttachment().getPort());
					var message = Events.event(reBuf, Events.UDP_CONNECT);
					message.setEventContext(eventContext);
					var channel = (DatagramChannel) ctx.channel();
					channel.writeAndFlush(message);
				}
			} catch (Exception e) {
				log.error("Packet received from unknown source address: {}, going to discard", remoteAddress);
			}
		}
	}

	public Event getUDPConnectEvent(Event event, SocketAddress remoteAddress,
			DatagramChannel udpChannel)
	{
		log.debug("Incoming udp connection remote address : {}",
				remoteAddress);
		
		if (event.getType() != Events.CONNECT)
		{
			log.info("Going to discard UDP Message Event with type {} "
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
