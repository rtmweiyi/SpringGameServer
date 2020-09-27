package com.weiyi.zhumao.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
// import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;

import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.app.impl.GameRoomSession;

import com.weiyi.zhumao.communication.NettyMessageBuffer;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.NetworkEvent;
import com.weiyi.zhumao.event.impl.DefaultNetworkEvent;
import com.weiyi.zhumao.service.impl.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleGameRoom extends GameRoomSession {
    private Queue<float[]> spawnPoints = new ArrayBlockingQueue<>(5);
    private volatile int tick = 1;
    private LinkedBlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();
    private int nums;
    private UserService userService;
    private ScheduledFuture<?> taskHolder;
    private int robots;
    private List<Long> robotsIds = new ArrayList<>();
    private Map<Long, Long> robotsAndPlayers = new HashMap<>();

    public void setUserService(UserService service) {
        userService = service;
    }

    public SimpleGameRoom(GameRoomSessionBuilder gameRoomSessionBuilder, int nums, int robots) {
        super(gameRoomSessionBuilder);
        this.nums = nums;
        spawnPoints.add(new float[] { 20.01f, 0.5f, 0 });
        spawnPoints.add(new float[] { -11.87f, 0.5f, 3.2f });
        spawnPoints.add(new float[] { -5.1f, 0.5f, 0 });
        spawnPoints.add(new float[] { -3.1f, 0.5f, 26.4f });
        spawnPoints.add(new float[] { 20.8f, 0.5f, 27f });
        this.robots = robots;
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
    public synchronized boolean connectSession(PlayerSession playerSession) {
        boolean result = super.connectSession(playerSession);
        if (sessions.size() == nums - robots) {
            for (PlayerSession playerSession2 : sessions) {
                spawnPlayer(playerSession2);
            }

            for (int i = 0; i < robots; i++) {
                // 生成机器人
                float[] robotPoint = spawnPoints.poll();
                NettyMessageBuffer robotBuf = new NettyMessageBuffer();
                robotBuf.writeInt(tick);
                robotBuf.writeLong(System.currentTimeMillis());
                robotBuf.writeInt(Commands.robot_spawn);
                long ronbotId = Tools.getRobotId();
                robotBuf.writeLong(ronbotId);
                robotsIds.add(ronbotId);
                robotBuf.writeFloat(robotPoint[0]);
                robotBuf.writeFloat(robotPoint[1]);
                robotBuf.writeFloat(robotPoint[2]);
                // TODO 到时候用随机名字
                robotBuf.writeString("Robot" + i);
                // 服务端发端信息,这里暂时用最大值做id，以后再看有没有更好的办法
                robotBuf.writeLong(Long.MAX_VALUE);
                var event = Events.event(robotBuf, Events.NETWORK_MESSAGE);
                NetworkEvent networkEvent = new DefaultNetworkEvent(event);
                sendBroadcast(networkEvent);
            }

            // 当前哪个客户端激活
            int playerIdToActiveRobot = 0;
            int playerNums = sessions.size();
            for (int i = 0; i < robots; i++) {
                Long robotId = robotsIds.get(i);
                if (playerIdToActiveRobot > playerNums - 1) {
                    playerIdToActiveRobot = 0;
                }

                NettyMessageBuffer activeBuf = new NettyMessageBuffer();
                activeBuf.writeInt(tick);
                activeBuf.writeLong(System.currentTimeMillis());
                activeBuf.writeInt(Commands.active_robot);
                long activeInPlayerId = (long) sessions.get(playerIdToActiveRobot).getPlayer().getId();
                robotsAndPlayers.put(robotId, activeInPlayerId);
                activeBuf.writeLong(robotId);
                playerIdToActiveRobot++;
                activeBuf.writeLong(activeInPlayerId);
                activeBuf.writeLong(Long.MAX_VALUE);
                var event = Events.event(activeBuf, Events.NETWORK_MESSAGE);
                NetworkEvent networkEvent = new DefaultNetworkEvent(event);
                sendBroadcast(networkEvent);
            }

        }
        return result;
    }

    @Override
    public void spawnPlayer(PlayerSession playerSession) {
        float[] point = spawnPoints.poll();
        if (point == null) {
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
        // 从数据库中获取currentcar
        var user = userService.getUserById((long) playerSession.getPlayer().getId());
        buf.writeString(user.getCurrentCar());
        buf.writeLong((long) playerSession.getPlayer().getId());
        var event = Events.event(buf, Events.NETWORK_MESSAGE);
        NetworkEvent networkEvent = new DefaultNetworkEvent(event);
        sendBroadcast(networkEvent);
    }

    @Override
    public synchronized void removePlayer(Command cmd) {

        // 方案二
        // 从session中拿到channel ，channel改变protocal
        // 从sessions 移除 该session
        // 删除session
        // 如果没有session了，说明本场游戏已经完结，删除gameroom
        long deathId = (long) cmd.getContent().get(0);
        if (robotsAndPlayers.containsKey(deathId)) {
            log.info("移除机器人: " + deathId);
            robotsAndPlayers.remove(deathId);
            robots--;
        } else {
            log.info("removePlayer-- id :" + deathId);
            PlayerSession session = getPlayerSessionById(deathId);
            if (session != null) {
                sessions.remove(session);
                this.eventDispatcher.removeHandlersForSession(session);
                log.info("send Death 1");
                var wapper1 = new NettyMessageBuffer(cmd.getBuf());
                var deathEvent1 = Events.event(wapper1, Events.NETWORK_MESSAGE);
                NetworkEvent deathNetworkEvent1 = new DefaultNetworkEvent(deathEvent1);
                session.getTcpSender().sendMessage(deathNetworkEvent1);
                if(sessions.size()>0){
                    for (long robotId : robotsAndPlayers.keySet()) {
                        long playerId = robotsAndPlayers.get(robotId);
                        if (deathId == playerId) {
                            // 重新激活挂在该player的机器人
                            int index = new Random().ints(0, sessions.size()).limit(1).findFirst().getAsInt();
                            long activeInPlayerId = (long) sessions.get(index).getPlayer().getId();
                            NettyMessageBuffer activeBuf = new NettyMessageBuffer();
                            activeBuf.writeInt(tick);
                            activeBuf.writeLong(System.currentTimeMillis());
                            activeBuf.writeInt(Commands.active_robot);
                            robotsAndPlayers.put(robotId, activeInPlayerId);
                            activeBuf.writeLong(robotId);
                            activeBuf.writeLong(activeInPlayerId);
                            activeBuf.writeLong(Long.MAX_VALUE);
                            var event = Events.event(activeBuf, Events.NETWORK_MESSAGE);
                            NetworkEvent networkEvent = new DefaultNetworkEvent(event);
                            sendBroadcast(networkEvent);
                        }
                    }
                }
            }
        }

        // 其他玩家收到死亡消息
        if (sessions.size() > 1) {
            addCommand(cmd);
        } else if (sessions.size() == 1) {
            if (robots == 0) {
                PlayerSession winSession = (PlayerSession) sessions.toArray()[0];
                log.info("send Death 2");
                var wapper2 = new NettyMessageBuffer(cmd.getBuf());
                var deathEvent2 = Events.event(wapper2, Events.NETWORK_MESSAGE);
                NetworkEvent deathNetworkEvent2 = new DefaultNetworkEvent(deathEvent2);
                winSession.getTcpSender().sendMessage(deathNetworkEvent2);
                // 房间只剩一个玩家，且没有机器人，发送玩家赢事件
                NettyMessageBuffer buf = new NettyMessageBuffer();
                buf.writeInt(tick);
                buf.writeLong(System.currentTimeMillis());
                buf.writeInt(Commands.win);
                buf.writeLong((long) winSession.getPlayer().getId());
                buf.writeLong((long) winSession.getPlayer().getId());
                var event = Events.event(buf, Events.NETWORK_MESSAGE);
                NetworkEvent networkEvent = new DefaultNetworkEvent(event);
                winSession.getTcpSender().sendMessage(networkEvent);
                sessions.remove(winSession);
                this.eventDispatcher.removeHandlersForSession(winSession);
                // TODO 以后生成新Task 检查一个Room是否存在很长时间，如果是的话，就delete
                taskHolder.cancel(true);
            } else {
                addCommand(cmd);
            }
        } else if (sessions.size() == 0) {
            taskHolder.cancel(true);
        }
    }

    private PlayerSession getPlayerSessionById(long id) {
        for (PlayerSession playerSession : sessions) {
            if ((long) playerSession.getPlayer().getId() == id) {
                return playerSession;
            }
        }
        return null;
    }

    @Override
    public void setTaskHolder(ScheduledFuture<?> holder) {
        taskHolder = holder;
    }

    @Override
    public ScheduledFuture<?> getTaskHolder() {
        return taskHolder;
    }

    @Override
    public void leaveProtocol(PlayerSession session) {
        lobbyProtocol.applyProtocol(session, true);
    }

}