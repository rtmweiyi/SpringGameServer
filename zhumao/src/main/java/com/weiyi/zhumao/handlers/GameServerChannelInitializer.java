package com.weiyi.zhumao.handlers;

import java.util.concurrent.TimeUnit;

// import com.google.gson.Gson;
import com.weiyi.zhumao.config.NettyProperties;
import com.weiyi.zhumao.handlers.netty.EventDecoder;
import com.weiyi.zhumao.handlers.netty.HeartbeatHandler;
import com.weiyi.zhumao.handlers.netty.LoginHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
// import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@EnableConfigurationProperties(NettyProperties.class)
public class GameServerChannelInitializer extends ChannelInitializer<Channel> {
    // @Autowired
    // private Gson gson;

    @Autowired
    NettyProperties nettyProperties;

    @Autowired
    LoginHandler loginHandler;

    @Autowired 
    LengthFieldPrepender lengthFieldPrepender;

    private static final int MAX_IDLE_SECONDS = 60;



    @Override
    protected void initChannel(Channel ch) throws Exception {
		ch.pipeline()
        .addLast("framer",createLengthBasedFrameDecoder())
        .addLast("idleStateCheck", new IdleStateHandler( 0, 0,
                MAX_IDLE_SECONDS,TimeUnit.SECONDS))
        .addLast("heartbeatHandler",new HeartbeatHandler())
        .addLast("eventDecoder",new EventDecoder())
        .addLast("loginHandler",loginHandler)
        .addLast("lengthFieldPrepender",lengthFieldPrepender);
    }

    public ChannelHandler createLengthBasedFrameDecoder()
	{
        log.info("frame size: "+nettyProperties.getFrameSize());
        return new LengthFieldBasedFrameDecoder(nettyProperties.getFrameSize(), 0, 4, 0, 4);
    }
    
}