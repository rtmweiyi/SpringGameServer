package com.weiyi.zhumao.game;

import com.weiyi.zhumao.app.GameCommandInterpreter;
import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.app.impl.InvalidCommandException;
import com.weiyi.zhumao.communication.MessageBuffer;
import com.weiyi.zhumao.event.Event;
// import com.weiyi.zhumao.event.NetworkEvent;
// import com.weiyi.zhumao.event.impl.DefaultNetworkEvent;
import com.weiyi.zhumao.event.impl.DefaultSessionEventHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionHandler extends DefaultSessionEventHandler implements GameCommandInterpreter {

    public SessionHandler(Session session) {
        super(session);
    }

    @Override
    public void interpretCommand(Object command) throws InvalidCommandException {
        @SuppressWarnings("unchecked")
        MessageBuffer<ByteBuf> msgbuf = (MessageBuffer<ByteBuf>) command;
        ByteBuf buf = msgbuf.getNativeBuffer();
        ByteBuf timeBuf = Unpooled.buffer(8);
        timeBuf.writeLong(System.currentTimeMillis());
        buf = Unpooled.wrappedBuffer(timeBuf,buf);
        ByteBuf tickbuf = Unpooled.buffer(4);
        PlayerSession pSession = (PlayerSession) getSession();
        GameRoom gameRoom = pSession.getGameRoom();
        int tick = gameRoom.tick();
        tickbuf.writeInt(tick);
        ByteBuf cmdBuf = Unpooled.wrappedBuffer(tickbuf,buf);
        Command cmd = new Command(cmdBuf);
        if(cmd.getCmdType()==Commands.leave_room){
            long id = (long)cmd.getContent().get(0);
            gameRoom.removePlayer(id);
        }
        gameRoom.addCommand(cmd);
    }

    public void onDataIn(Event event){
        try{
            interpretCommand(event.getSource());
        }
        catch (InvalidCommandException e){
            e.printStackTrace();
            log.error("{}",e);
        }
        // if (null != getSession())
		// {
        //     NetworkEvent networkEvent = new DefaultNetworkEvent(event);
        //     PlayerSession pSession = (PlayerSession) getSession();
        //     pSession.getGameRoom().sendBroadcast(networkEvent);
        // }
    }
    
}