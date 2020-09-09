package com.weiyi.zhumao.handlers.netty;

import com.weiyi.zhumao.communication.MessageBuffer;
import com.weiyi.zhumao.event.Event;

import org.springframework.stereotype.Component;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
@Component
public class MessageBufferEventEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(null ==msg){
            log.error("Null message received in MessageBufferEventEncoder");
        }

        Event event = (Event) msg;
		ByteBuf opcode = Unpooled.buffer(1);
		opcode.writeByte(event.getType());
        ByteBuf buffer = null;
        
        if(null != event.getSource())
		{
			@SuppressWarnings("unchecked")
			MessageBuffer<ByteBuf> msgBuffer = (MessageBuffer<ByteBuf>)event.getSource();
			ByteBuf data = msgBuffer.getNativeBuffer();
			buffer = Unpooled.wrappedBuffer(opcode, data);
        }
        else{
            buffer = opcode;
        }
        out.writeBytes(buffer);
    }
    
}