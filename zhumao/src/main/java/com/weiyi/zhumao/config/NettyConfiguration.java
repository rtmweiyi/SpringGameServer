package com.weiyi.zhumao.config;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;

import com.weiyi.zhumao.server.netty.NettyConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.LengthFieldPrepender;

import com.weiyi.zhumao.service.SessionRegistryService;
import com.weiyi.zhumao.service.impl.SessionRegistry;

@Configuration
@EnableConfigurationProperties(NettyProperties.class)
public class NettyConfiguration {
    @Autowired
    NettyProperties nettyProperties;

    @Autowired
    @Qualifier("bossgroup")
    NioEventLoopGroup bossGroup;

    @Autowired
    @Qualifier("workergroup")
    NioEventLoopGroup workerGroup;

    // @Bean
    // public ServerBootstrap bootstrap(@Autowired GameServerChannelInitializer gameServerChannelInitializer){
    //     ServerBootstrap b = new ServerBootstrap();
    //     b.group(bossGroup(), workerGroup())
    //     .channel(NioServerSocketChannel.class)
    //     .handler(new LoggingHandler(LogLevel.DEBUG))
    //     .childHandler(gameServerChannelInitializer)
    //     .option(ChannelOption.SO_BACKLOG, nettyProperties.getBacklog());
    //     return b;
    // }

    @Bean(name = "bossgroup",destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(nettyProperties.getBossCount());
    }

    @Bean(name = "workergroup",destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(nettyProperties.getWorkerCount());
    }


    @Bean
    public LengthFieldPrepender createLengthFieldPrepender(){
        return new LengthFieldPrepender(4);
    }

    @Bean("UDPConfig")
    public NettyConfig getUDPConfig(){
        var config = new NettyConfig();
        var udpOpstions = new HashMap<ChannelOption<?>, Object>();
        udpOpstions.put(ChannelOption.SO_SNDBUF, 65536);
        udpOpstions.put(ChannelOption.SO_RCVBUF, 65536);
        udpOpstions.put(ChannelOption.SO_BROADCAST, false);
        config.setChannelOptions(udpOpstions);
        config.setBossGroup(bossGroup);
        config.setWorkerGroup(workerGroup);
        config.setPortNumber(nettyProperties.getUdpPort());
        return config;
    }

    @Bean("TCPConfig")
    public NettyConfig getTCPConfig(){
        var config = new NettyConfig();
        var tcpOpstions = new HashMap<ChannelOption<?>, Object>();
        tcpOpstions.put(ChannelOption.SO_KEEPALIVE, true);
        tcpOpstions.put(ChannelOption.SO_BACKLOG, 100);
        config.setChannelOptions(tcpOpstions);
        config.setBossGroup(bossGroup);
        config.setWorkerGroup(workerGroup);
        config.setPortNumber(nettyProperties.getTcpPort());
        return config;
    }

    @Bean("udpSessionRegistry")
    public SessionRegistryService<SocketAddress> getUdpSessionRegistry(){
        return new SessionRegistry<SocketAddress>();
    }
}