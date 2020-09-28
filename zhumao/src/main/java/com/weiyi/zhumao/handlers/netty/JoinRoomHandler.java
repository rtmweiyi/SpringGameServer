package com.weiyi.zhumao.handlers.netty;

import java.net.InetSocketAddress;

import com.weiyi.zhumao.app.Player;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.game.CustomMatchmaker;
import com.weiyi.zhumao.util.NettyUtils;

import io.netty.buffer.ByteBuf;
// import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class JoinRoomHandler extends SimpleChannelInboundHandler<Event> {
    private Player player;
    CustomMatchmaker customMatchmaker;

    public JoinRoomHandler(Player player,CustomMatchmaker customMatchmaker){
        this.player = player;
        this.customMatchmaker = customMatchmaker;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Event event) throws Exception {
        // final Event event = (Event) msg;
		// ByteBuf buffer = (ByteBuf) event.getSource();
        final Channel channel = ctx.channel();
        int type = event.getType();
        if(type==Events.GAME_ROOM_JOIN){
            var buffer = (ByteBuf)event.getSource();
            InetSocketAddress addre = NettyUtils.readSocketAddress(buffer);
            customMatchmaker.addPlayerAndChannel(player, channel, addre);
        }
    }
    
}