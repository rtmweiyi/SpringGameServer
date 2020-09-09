package com.weiyi.zhumao.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
// import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.app.impl.GameRoomSession;
import com.weiyi.zhumao.communication.NettyMessageBuffer;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.NetworkEvent;
import com.weiyi.zhumao.event.impl.DefaultNetworkEvent;
import com.weiyi.zhumao.service.impl.ShopService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleGameRoom extends GameRoomSession {
    private Queue<float[]> spawnPoints = new ArrayBlockingQueue<>(5);
    private volatile int tick = 1;
    private LinkedBlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();
    private int nums;
    private ShopService shopService;
    public void setShopService(ShopService shops){
        shopService = shops;
    }

    public SimpleGameRoom(GameRoomSessionBuilder gameRoomSessionBuilder,int nums) {
        super(gameRoomSessionBuilder);
        this.nums = nums;
        spawnPoints.add(new float[]{20.01f,0.5f,0});
        spawnPoints.add(new float[]{-11.87f,0.5f,3.2f});
        spawnPoints.add(new float[]{-5.1f,0.5f,0});
        spawnPoints.add(new float[]{-3.1f,0.5f,26.4f});
        spawnPoints.add(new float[]{20.8f,0.5f,27f});
    }

    @Override
    public void onLogin(PlayerSession playerSession) {
        SessionHandler listenter = new SessionHandler(playerSession);
        playerSession.addHandler(listenter);
        log.info("Added event listener in Simple Room");
    }

    @Override
    public int incrFrames() {
        return ++tick;
    }

    @Override
    public int tick() {
        return tick;
    }

    @Override
    public List<Command> getCommands() {
        List<Command> result = new ArrayList<>();
        while (true) {
            var tmp = commandQueue.poll();
            if (tmp == null) {
                return result;
            } else {
                if (tmp.getTick() == tick) {
                    result.add(tmp);
                }
            }
        }
    }

    @Override
    public void addCommand(Command cmd) {
        commandQueue.add(cmd);
    }

    @Override
    public synchronized boolean connectSession(PlayerSession playerSession){
        boolean result = super.connectSession(playerSession);
        if(sessions.size()==nums){
            for (PlayerSession playerSession2 : sessions) {
                spawnPlayer(playerSession2);
            }
        }
        return result;
    }

    @Override
    public void spawnPlayer(PlayerSession playerSession) {
        float[] point = spawnPoints.poll();
        if(point==null){
            log.error("Can't get point.");
            return;
        }
        NettyMessageBuffer buf = new NettyMessageBuffer();
        buf.writeInt(tick);
        buf.writeLong(System.currentTimeMillis());
        buf.writeInt(Commands.spawn);
        buf.writeFloat(point[0]);
        buf.writeFloat(point[1]);
        buf.writeFloat(point[2]);
        buf.writeString(playerSession.getPlayer().getName());
        //从数据库中获取currentcar
        var shop = shopService.getShopById((long)playerSession.getPlayer().getId());
        buf.writeString(shop.getCurrentCar());
        buf.writeLong((long)playerSession.getPlayer().getId());
        var event = Events.event(buf, Events.NETWORK_MESSAGE);
        NetworkEvent networkEvent = new DefaultNetworkEvent(event);
        sendBroadcast(networkEvent);
    }

    @Override
    public void removePlayer(long id) {
        //方案一
        //sessions 删除
        //playerSession handler删除 //session是否也要删除?
        //gameRoom NetworkEventListener 删除

        //方案二
        //从session中拿到channel ，channel改变protocal
        //从sessions 移除 该session
        //删除session 
        //如果没有session了，说明本场游戏已经完结，删除gameroom

        PlayerSession session = getPlayerSessionById(id);
        if(session!=null){
            lobbyProtocol.applyProtocol(session, true);
        }
        sessions.remove(session);
        if(sessions.size()==0){
            //TODO 删除GameRoom
            //貌似只有session持有gameroom，如果session删完了，那么gameroom也会回收
            //TODO 删除synctask
        }
    }

    private PlayerSession getPlayerSessionById(long id) {
        for (PlayerSession playerSession : sessions) {
            if((long)playerSession.getId()==id){
                return playerSession;
            }
        }
        return null;
    }

}