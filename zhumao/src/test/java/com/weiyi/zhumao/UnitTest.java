package com.weiyi.zhumao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import com.weiyi.zhumao.game.Command;
import com.weiyi.zhumao.util.CompressCommands;

import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.InstanceOf.VarArgAware;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class UnitTest {
    @Test
    void TestCommandsCompress(){
        List<Command> commands = new ArrayList<>();
        var cmd1 = new Command();
        cmd1.setCmdType(2);
        var cmd2 = new Command();
        cmd2.setCmdType(2);
        commands.add(cmd1);
        commands.add(cmd2);
        CompressCommands.compress(commands);
    }
    
    @Test
    void TestCommandEquals(){
        var buf1 = Unpooled.buffer();
        buf1.writeInt(10);
        var cmd1 = new Command();
        cmd1.setTick(1);
        cmd1.setPlayerId(2);
        List<Object> content1 = new ArrayList<>();
        content1.add(1);
        content1.add(2);
        cmd1.setContent(content1);
        cmd1.setBuf(buf1);

        var buf2 = Unpooled.buffer();
        buf2.writeInt(10);
        var cmd2 = new Command();
        cmd2.setTick(1);
        cmd2.setPlayerId(2);
        List<Object> content2 = new ArrayList<>();
        content2.add(1);
        content2.add(2);
        cmd2.setContent(content2);
        cmd2.setBuf(buf2);

        boolean result = cmd1.equals(cmd2);
        List<Command> testList = new ArrayList<>();
        testList.add(cmd1);
        testList.add(cmd2);
        assertEquals(2,testList.size());
    }

    @Test
    void TestCommandHashCode(){
        var buf1 = Unpooled.buffer();
        buf1.writeInt(234);
        buf1.writeShort(10);
        var cmd1 = new Command();
        cmd1.setTick(1);
        cmd1.setPlayerId(2);
        List<Object> content1 = new ArrayList<>();
        content1.add(1);
        content1.add(2);
        cmd1.setContent(content1);
        cmd1.setBuf(buf1);

        var buf2 = Unpooled.buffer();
        buf2.writeInt(234);
        buf2.writeShort(10);
        var cmd2 = new Command();
        cmd2.setTick(1);
        cmd2.setPlayerId(2);
        List<Object> content2 = new ArrayList<>();
        content2.add(1);
        content2.add(2);
        cmd2.setContent(content2);
        cmd2.setBuf(buf2);

        int content1HasCode = content1.hashCode();
        int content2HasCode = content2.hashCode();

        int buf1HashCode = buf1.hashCode();
        int buf2HashCode = buf2.hashCode();

        int result1 = cmd1.hashCode();
        int result2 = cmd2.hashCode();
        assertEquals(result2,result1);
    }

    @Test
    void TestCommandSet(){
        var buf1 = Unpooled.buffer();
        buf1.writeInt(234);
        buf1.writeShort(10);
        var cmd1 = new Command();
        cmd1.setTick(1);
        cmd1.setPlayerId(2);
        List<Object> content1 = new ArrayList<>();
        content1.add(1);
        content1.add(2);
        cmd1.setContent(content1);
        cmd1.setBuf(buf1);

        var buf2 = Unpooled.buffer();
        buf2.writeInt(234);
        buf2.writeShort(10);
        var cmd2 = new Command();
        cmd2.setTick(1);
        cmd2.setPlayerId(2);
        List<Object> content2 = new ArrayList<>();
        content2.add(1);
        content2.add(2);
        cmd2.setContent(content2);
        cmd2.setBuf(buf2);
        ConcurrentSkipListSet<Command> commandSet = new ConcurrentSkipListSet<>();

        // Set<Command> commandSet = new TreeSet<>();
        commandSet.add(cmd1);
        commandSet.add(cmd2);
        assertEquals(1, commandSet.size());
    }
}