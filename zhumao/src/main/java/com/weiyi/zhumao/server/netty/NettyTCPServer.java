package com.weiyi.zhumao.server.netty;

import java.net.InetSocketAddress;
// import java.util.concurrent.Executors;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

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
public class NettyTCPServer extends AbstractNettyServer
{

	private String[] args;
	
	public NettyTCPServer()
	{

	}

	@Override
	public TransmissionProtocol getTransmissionProtocol()
	{
		return TRANSMISSION_PROTOCOL.TCP;
	}

	@Override
	public void startServer(InetSocketAddress socketAddress)
	{
		this.socketAddress = socketAddress;
		try
		{
			// serverBootstrap.bind(socketAddress);
			ChannelFuture serverChannelFuture = serverBootstrap.bind(socketAddress).sync();
			log.info(toString());
			serverChannel = serverChannelFuture.channel().closeFuture().sync().channel();

			//非堵塞启动,因为堵塞启动会造成Spring test 堵塞
			// serverChannel = serverBootstrap.bind(socketAddress).channel().closeFuture().channel();
		}
		catch (Exception e)
		{
			log.error("Unable to start TCP server due to error {}",e);
			e.printStackTrace();
		}
	}

	public void stopServer() throws Exception
	{
		log.debug("In stopServer method of class: {}",
				this.getClass().getName());
		super.stopServer();
	}
	
	public String[] getArgs()
	{
		return args;
	}

	public void setArgs(String[] args)
	{
		this.args = args;
	}

	@Override
	public String toString()
	{
		return "NettyTCPServer ["+"socketAddress=" + socketAddress + ", portNumber=" + socketAddress.getPort()
				+ "]";
	}
	
}
