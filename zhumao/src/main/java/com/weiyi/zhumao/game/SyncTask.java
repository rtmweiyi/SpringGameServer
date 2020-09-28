package com.weiyi.zhumao.game;

import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.app.Task;
import com.weiyi.zhumao.communication.NettyMessageBuffer;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.NetworkEvent;
import com.weiyi.zhumao.event.impl.DefaultNetworkEvent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import static com.weiyi.zhumao.communication.DeliveryGuaranty.DeliveryGuarantyOptions.FAST;

public class SyncTask implements Task{
    private int id;
    private GameRoom room;

    @Override
    public void run() {
        var commands = room.getCommands();
        if(commands.size()>0){
            ByteBuf buf = Unpooled.buffer();
            for(var cmd:commands){
                buf.writeBytes(cmd.getBuf());
                var event = Events.event(new NettyMessageBuffer(buf), Events.NETWORK_MESSAGE);
                NetworkEvent networkEvent = new DefaultNetworkEvent(event);
                //用UDP发送
                if(cmd.getCmdType()==Commands.joystick){
                    networkEvent.setDeliveryGuaranty(FAST);
                }
                room.sendBroadcast(networkEvent);
            }
            room.incrFrames();
            
        }
    }

    public SyncTask(GameRoom room){
        this.room = room;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
    
}