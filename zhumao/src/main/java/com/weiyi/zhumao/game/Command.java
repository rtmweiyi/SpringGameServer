package com.weiyi.zhumao.game;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Command implements Comparable<Command>{
    protected int tick;
    protected long timestamp;
    protected int playerId;
    protected int cmdType;
    protected List<Object> content = new ArrayList<>();
    protected ByteBuf buf;

    public Command(ByteBuf inbuf) {
        buf = inbuf.copy();
        tick = inbuf.readInt();
        timestamp = inbuf.readLong();
        cmdType = inbuf.readInt();
        // switch (cmdType) {
        //     case 1:
        //         content.add((float) inbuf.readFloat());
        //         content.add((float) inbuf.readFloat());
        //         break;
        //     case 3:
        //         content.add((int) inbuf.readInt());
        //         content.add((long) inbuf.readLong());
        //         break;
        //     case 4:
        //         content.add((long) inbuf.readLong());
        //         break;
        //     case 5:
        //         content.add((long) inbuf.readLong());
        //         break;
        //     default:
        //         break;
        // }
        // playerId = inbuf.readInt();
    }

    public Command() {

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Command other = (Command) obj;
        if (other.tick != tick) {
            return false;
        }
        if (other.playerId != playerId) {
            return false;
        }
        if (other.cmdType != cmdType) {
            return false;
        }
        if (!buf.equals(other.buf)) {
            return false;
        }
        // for (int index = 0; index < content.size(); index++) {
        //     if (content.get(index) != other.content.get(index)) {
        //         return false;
        //     }
        // }
        if(!content.equals(other.content)){
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = tick + playerId + cmdType + content.hashCode() + buf.hashCode();
        return result;
    }


    @Override
    public int compareTo(Command other) {
        if(tick<other.tick){
            return -1;
        }
        if(tick>other.tick){
            return 1;
        }
        return 0;
    }
}