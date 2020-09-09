package com.weiyi.zhumao.handlers.netty;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.weiyi.zhumao.event.Events;

@Slf4j
public class EventDecoder extends ByteToMessageDecoder
{
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception
	{
		if(in.readableBytes() < 1 )
		{
            log.error("Null msg received in EventDecoder");
            return;
		}
 		int opcode = in.readUnsignedByte();

		// if(Events.LOG_IN == opcode || Events.RECONNECT == opcode){
		// 	in.readUnsignedByte();// To read-destroy the protocol version byte.
		// }
		out.add(Events.event(in, opcode));
	}
	
}
