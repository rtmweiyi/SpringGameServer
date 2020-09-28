package com.weiyi.zhumao.handlers.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import com.weiyi.zhumao.communication.NettyMessageBuffer;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.Events;

import org.springframework.stereotype.Component;
import io.netty.channel.ChannelHandler.Sharable;

/**
 * This decoder will convert a Netty {@link ChannelBuffer} to a
 * {@link NettyMessageBuffer}. It will also convert
 * {@link Events#NETWORK_MESSAGE} events to {@link Events#SESSION_MESSAGE}
 * event.
 * 
 * @author Abraham Menacherry
 * 
 */
@Slf4j
@Sharable
@Component
public class MessageBufferEventDecoder extends MessageToMessageDecoder<ByteBuf>{


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (null == in) {
            log.error("Null message received in MessageBufferEventDecoder");
        }

        // byte opcode = in.readByte();
        // if (opcode == Events.NETWORK_MESSAGE) {
        //     opcode = Events.SESSION_MESSAGE;
        // }
        // ByteBuf buf = Unpooled.buffer(in.readableBytes());
        // in.readBytes(buf);
        
        // out.add(Events.event(new NettyMessageBuffer(buf), opcode));
        out.add(decode(ctx, in));
    }

    public Event decode(ChannelHandlerContext ctx, ByteBuf in){
		byte opcode = in.readByte();
		if (opcode == Events.NETWORK_MESSAGE) 
		{
			opcode = Events.SESSION_MESSAGE;
		}
		ByteBuf data = in.readBytes(in.readableBytes());
		return Events.event(new NettyMessageBuffer(data), opcode);
	}
}
