package com.weiyi.gameserver.handler;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

@Component
public class GameServerChannelInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
        // .addLast(new ProtobufVarint32FrameDecoder())
        // .addLast(new ProtobufDecoder())
        // .addLast(new ProtobufVarint32LengthFieldPrepender())
        // .addLast(new ProtobufEncoder())
        // .addLast(new NettyServerHandler());


        .addLast(new DelimiterBasedFrameDecoder(1024 * 1024, Delimiters.lineDelimiter()))
        .addLast(new StringDecoder())
        .addLast(new StringEncoder());
    }
}