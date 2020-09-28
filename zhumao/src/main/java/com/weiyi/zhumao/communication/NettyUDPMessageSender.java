package com.weiyi.zhumao.communication;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.netty.channel.socket.DatagramChannel;
import lombok.extern.slf4j.Slf4j;

import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.communication.DeliveryGuaranty.DeliveryGuarantyOptions;
import com.weiyi.zhumao.communication.MessageSender.Fast;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.EventContext;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.impl.DefaultNetworkEvent;
import com.weiyi.zhumao.handlers.netty.UDPUpstreamHandler;
import com.weiyi.zhumao.service.SessionRegistryService;

/**
 * This class is used to send messages to a remote UDP client or server. An
 * instance of this class will be created by the {@link UDPUpstreamHandler} when
 * a {@link Events#CONNECT} event is received from client. The created instance
 * of this class is then sent as payload of a {@link DefaultNetworkEvent} to the
 * {@link Session}.
 * 
 * 
 * @author Abraham Menacherry
 * 
 */
@Slf4j
public class NettyUDPMessageSender implements Fast
{
	private final SocketAddress remoteAddress;
	private final DatagramChannel channel;
	private final SessionRegistryService<SocketAddress> sessionRegistryService;
	private final EventContext eventContext;
	private static final DeliveryGuaranty DELIVERY_GUARANTY = DeliveryGuarantyOptions.FAST;

	public NettyUDPMessageSender(SocketAddress remoteAddress,
			DatagramChannel channel,
			SessionRegistryService<SocketAddress> sessionRegistryService)
	{
		this.remoteAddress = remoteAddress;
		this.channel = channel;
		this.sessionRegistryService = sessionRegistryService;
		this.eventContext = new EventContextImpl((InetSocketAddress)remoteAddress);
	}

	@Override
	public Object sendMessage(Object message)
	{
		// TODO this might overwrite valid context, check for better design
		if(message instanceof Event){
			((Event)message).setEventContext(eventContext);
		}
		return channel.writeAndFlush(message);
	}

	@Override
	public DeliveryGuaranty getDeliveryGuaranty()
	{
		return DELIVERY_GUARANTY;
	}

	@Override
	public void close()
	{
		Session session = sessionRegistryService.getSession(remoteAddress);
		if (sessionRegistryService.removeSession(remoteAddress))
		{
			log.debug("Successfully removed session: {}", session);
		}
		else
		{
			log.trace("No udp session found for address: {}", remoteAddress);
		}

	}

	public SocketAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	public DatagramChannel getChannel()
	{
		return channel;
	}

	@Override
	public String toString()
	{
		String channelId = "UDP Channel with id: ";
		if (null != channel)
		{
			channelId += channel.id();
		}
		else
		{
			channelId += "0";
		}
		String sender = "Netty " + channelId + " RemoteAddress: "
				+ remoteAddress;
		return sender;
	}

	protected SessionRegistryService<SocketAddress> getSessionRegistryService()
	{
		return sessionRegistryService;
	}

	protected static class EventContextImpl implements EventContext
	{
		final InetSocketAddress clientAddress;
		public EventContextImpl(InetSocketAddress clientAddress){
			this.clientAddress = clientAddress;
		}
		@Override
		public Session getSession() {
			return null;
		}

		@Override
		public void setSession(Session session) {
		}

		@Override
		public InetSocketAddress getAttachment() {
			return clientAddress;
		}

		@Override
		public void setAttachment(Object attachement) {
		}
		
	}
}
