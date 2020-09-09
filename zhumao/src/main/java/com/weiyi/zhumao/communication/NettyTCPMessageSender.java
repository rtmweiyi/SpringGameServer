package com.weiyi.zhumao.communication;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import com.weiyi.zhumao.communication.DeliveryGuaranty.DeliveryGuarantyOptions;
import com.weiyi.zhumao.communication.MessageSender.Reliable;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.Event;

/**
 * A class that transmits messages reliably to remote machines/vm's. Internally
 * this class uses Netty tcp {@link Channel} to transmit the message.
 * 
 * @author Abraham Menacherry
 * 
 */
@Slf4j
public class NettyTCPMessageSender implements Reliable
{
	private final Channel channel;
	private static final DeliveryGuaranty DELIVERY_GUARANTY = DeliveryGuarantyOptions.RELIABLE;

	public NettyTCPMessageSender(Channel channel)
	{
		super();
		this.channel = channel;
	}

	@Override
	public Object sendMessage(Object message)
	{
		return channel.writeAndFlush(message);
	}

	@Override
	public DeliveryGuaranty getDeliveryGuaranty()
	{
		return DELIVERY_GUARANTY;
	}

	public Channel getChannel()
	{
		return channel;
	}

	/**
	 * Writes an the {@link Events#DISCONNECT} to the client, flushes
	 * all the pending writes and closes the channel.
	 * 
	 */
	@Override
	public void close()
	{
		log.debug("Going to close tcp connection in class: {}", this
				.getClass().getName());
		Event event = Events.event(null, Events.DISCONNECT);
		if (channel.isActive())
		{
			channel.write(event).addListener(ChannelFutureListener.CLOSE);
			channel.flush();
		}
		else
		{
			channel.close();
			log.trace("Unable to write the Event {} with type {} to socket",
					event, event.getType());
		}
	}

	@Override
	public String toString()
	{
		String channelId = "TCP channel with Id: ";
		if (null != channel)
		{
			channelId += channel.id().toString();
		}
		else
		{
			channelId += "0";
		}
		String sender = "Netty " + channelId;
		return sender;
	}
}
