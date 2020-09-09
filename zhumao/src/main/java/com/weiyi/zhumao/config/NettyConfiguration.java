package com.weiyi.zhumao.config;

import java.net.InetSocketAddress;

import com.weiyi.zhumao.handlers.GameServerChannelInitializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.netty.bootstrap.ServerBootstrap;
// import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Configuration
@EnableConfigurationProperties(NettyProperties.class)
public class NettyConfiguration {
    @Autowired
    NettyProperties nettyProperties;

    @Bean
    public ServerBootstrap bootstrap(@Autowired GameServerChannelInitializer gameServerChannelInitializer){
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup(), workerGroup())
        .channel(NioServerSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.DEBUG))
        .childHandler(gameServerChannelInitializer)
        .option(ChannelOption.SO_BACKLOG, nettyProperties.getBacklog());
        return b;
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(nettyProperties.getBossCount());
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(nettyProperties.getWorkerCount());
    }

    @Bean
    public InetSocketAddress tcpSocketAddress() {
        return new InetSocketAddress(nettyProperties.getTcpPort());
    }

    @Bean
    public LengthFieldPrepender createLengthFieldPrepender(){
        return new LengthFieldPrepender(4);
    }
}