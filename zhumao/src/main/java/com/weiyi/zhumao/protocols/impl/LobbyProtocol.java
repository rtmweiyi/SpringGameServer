package com.weiyi.zhumao.protocols.impl;

import java.util.concurrent.TimeUnit;

import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.config.NettyProperties;
import com.weiyi.zhumao.game.CustomMatchmaker;
import com.weiyi.zhumao.handlers.netty.EventDecoder;
import com.weiyi.zhumao.handlers.netty.HeartbeatHandler;
import com.weiyi.zhumao.handlers.netty.JoinRoomHandler;
import com.weiyi.zhumao.handlers.netty.LoginHandler;
import com.weiyi.zhumao.protocols.AbstractNettyProtocol;
import com.weiyi.zhumao.util.NettyUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LobbyProtocol extends AbstractNettyProtocol {
    @Autowired
    NettyProperties nettyProperties;
    @Autowired
	  CustomMatchmaker customMatchmaker;

    @Autowired
    LoginHandler loginHandler;

    @Autowired 
    LengthFieldPrepender lengthFieldPrepender;

    private static final int MAX_IDLE_SECONDS = 60;

    public LobbyProtocol() {
        super("LOBBY-PROTOCOL");
    }

    @Override
    public void applyProtocol(PlayerSession playerSession) {
        log.trace("Going to apply {} on session: {}", getProtocolName(),
				playerSession);
        ChannelPipeline pipeline = NettyUtils
				.getPipeLineOfConnection(playerSession);
        pipeline.addLast("lengthBasedFrame",createLengthBasedFrameDecoder());
        pipeline.addLast("idleStateCheck", new IdleStateHandler( 0, 0,MAX_IDLE_SECONDS,TimeUnit.SECONDS));
        pipeline.addLast("heartbeatHandler",new HeartbeatHandler());
        pipeline.addLast("eventDecoder",new EventDecoder());
        pipeline.addLast("loginHandler",loginHandler);
        pipeline.addLast("lengthFieldPrepender",lengthFieldPrepender);
        var player = playerSession.getPlayer();
        pipeline.addLast("JoinRoomHandler", new JoinRoomHandler(player, customMatchmaker));
    }
    
}