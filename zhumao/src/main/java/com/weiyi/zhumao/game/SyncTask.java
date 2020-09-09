package com.weiyi.zhumao.game;

import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.app.Task;
import com.weiyi.zhumao.communication.NettyMessageBuffer;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.NetworkEvent;
import com.weiyi.zhumao.event.impl.DefaultNetworkEvent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SyncTask implements Task{
    private Object id;
    private GameRoom room;

    @Override
    public void run() {
        var commands = room.getCommands();
        if(commands.size()>0){
            ByteBuf buf = Unpooled.buffer();
            for(var cmd:commands){
                buf.writeBytes(cmd.getBuf());
            }
            room.incrFrames();
            var event = Events.event(new NettyMessageBuffer(buf), Events.NETWORK_MESSAGE);
            NetworkEvent networkEvent = new DefaultNetworkEvent(event);
            room.sendBroadcast(networkEvent);
        }
    }

    public SyncTask(GameRoom room){
        this.room = room;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = id;
    }
    
}