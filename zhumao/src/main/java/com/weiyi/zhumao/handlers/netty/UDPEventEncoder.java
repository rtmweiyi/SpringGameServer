package com.weiyi.zhumao.handlers.netty;

import com.weiyi.zhumao.event.Event;

import org.springframework.stereotype.Component;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.List;


@Sharable
@Component
public class UDPEventEncoder extends MessageBufferEventEncoder 
{
	@Override
	protected void encode(ChannelHandlerContext ctx, Event event,
			List<Object> out) throws Exception
	{
		ByteBuf data = (ByteBuf) super.encode(ctx, event);
		//这里写入length
		int length  = data.readableBytes();
		ByteBuf lenbuf = Unpooled.buffer(4);
		lenbuf.writeInt(length);

		InetSocketAddress clientAddress = (InetSocketAddress) event
				.getEventContext().getAttachment();
		out.add(new DatagramPacket(Unpooled.wrappedBuffer(lenbuf,data), clientAddress));
		// out.add(data);
	}
	
}