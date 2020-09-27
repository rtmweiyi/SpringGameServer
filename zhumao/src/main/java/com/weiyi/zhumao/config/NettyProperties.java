package com.weiyi.zhumao.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "netty")
public class NettyProperties {
    @Size(min=1000, max=65535)
    private int tcpPort;

    public void setTcpPort(int tcpPort){
        this.tcpPort = tcpPort;
    }

    public int getTcpPort(){
        return tcpPort;
    }

    @Size(min=1000, max=65535)
    private int udpPort;

    public void setUdpPort(int udpPort){
        this.udpPort = udpPort;
    }

    public int getUdpPort(){
        return udpPort;
    }

    @Min(1)
    private int bossCount;

    public void setBossCount(int bossCount){
        this.bossCount = bossCount;
    }

    public int getBossCount(){
        return bossCount;
    }

    @Min(2)
    private int workerCount;
    public void setWorkerCount(int workerCount){
        this.workerCount = workerCount;
    }

    public int getWorkerCount(){
        return workerCount;
    }

    private boolean keepAlive;
    public void setKeepAlive(boolean keepAlive){
        this.keepAlive = keepAlive;
    }

    public boolean getKeepAlive(){
        return keepAlive;
    }

    private int backlog;
    public void setBacklog(int backlog){
        this.backlog = backlog;
    }

    public int getBacklog(){
        return backlog;
    }

    private int frameSize;
    public void setFrameSize(int frameSize){
        this.frameSize = frameSize;
    }

    public int getFrameSize(){
        return frameSize;
    }
}