package com.weiyi.zhumao.handlers.netty;

import java.util.List;

import com.weiyi.zhumao.communication.MessageBuffer;
import com.weiyi.zhumao.event.Event;

import org.springframework.stereotype.Component;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
@Component
public class MessageBufferEventEncoder extends MessageToMessageEncoder<Event> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Event event, List<Object> out) throws Exception {
        out.add(encode(ctx, event));
    }

    protected ByteBuf encode(ChannelHandlerContext ctx, Event event)
	{
		if(null ==event){
            log.error("Null message received in MessageBufferEventEncoder");
            return null;
        }
		
        ByteBuf msg = null;
        if(null != event.getSource())
		{
			@SuppressWarnings("unchecked")
			MessageBuffer<ByteBuf> msgBuffer = (MessageBuffer<ByteBuf>)event.getSource();
            ByteBuf data = Unpooled.copiedBuffer(msgBuffer.getNativeBuffer());
            ByteBuf opcode = ctx.alloc().buffer(1);
		    opcode.writeByte(event.getType());
			msg = Unpooled.wrappedBuffer(opcode, data);
        }
        else{
            msg = ctx.alloc().buffer(1);
			msg.writeByte(event.getType());
        }
        return msg;
	}
    
}