package com.weiyi.zhumao.server.netty;

// import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;

import com.weiyi.zhumao.handlers.GameServerChannelInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
// import java.util.concurrent.Executors;
// import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This class is used for TCP IP communications with client. It uses Netty tcp
 * server bootstrap for this.
 * 
 * @author Abraham Menacherry
 * 
 */
@Slf4j
@Component
public class NettyTCPServer extends AbstractNettyServer {
	private ServerBootstrap serverBootstrap;

	public NettyTCPServer(@Qualifier("TCPConfig") NettyConfig nettyConfig, @Autowired GameServerChannelInitializer channelInitializer) {
		super(nettyConfig, channelInitializer);
	}

	private String[] args;

	@Override
	public TransmissionProtocol getTransmissionProtocol() {
		return TRANSMISSION_PROTOCOL.TCP;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}

	@Override
	public String toString() {
		return "NettyTCPServer [socketAddress=" + nettyConfig.getSocketAddress()
				+ ", portNumber=" + nettyConfig.getPortNumber() + "]";
	}

	@Override
	public void setChannelInitializer(ChannelInitializer<? extends Channel> initializer) {
		this.channelInitializer = initializer;
		serverBootstrap.childHandler(initializer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void startServer() throws Exception {
		try {
			serverBootstrap = new ServerBootstrap();
			Map<ChannelOption<?>, Object> channelOptions = nettyConfig.getChannelOptions();
			if(null != channelOptions){
				Set<ChannelOption<?>> keySet = channelOptions.keySet();
				for(@SuppressWarnings("rawtypes") ChannelOption option : keySet)
				{
					serverBootstrap.option(option, channelOptions.get(option));
				}
			}
			serverBootstrap.group(getBossGroup(),getWorkerGroup())
					.channel(NioServerSocketChannel.class)
					.childHandler(getChannelInitializer());
			Channel serverChannel = serverBootstrap.bind(nettyConfig.getSocketAddress()).sync()
					.channel();

			// 非堵塞启动,因为堵塞启动会造成Spring test 堵塞
			// Channel serverChannel =
			// serverBootstrap.bind(nettyConfig.getSocketAddress()).channel().closeFuture().channel();
			ALL_CHANNELS.add(serverChannel);
		} catch(Exception e) {
			log.error("TCP Server start error {}, going to shut down", e);
			super.stopServer();
			throw e;
		}

	}
	
}
