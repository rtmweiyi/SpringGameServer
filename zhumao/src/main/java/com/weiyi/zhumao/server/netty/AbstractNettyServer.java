package com.weiyi.zhumao.server.netty;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

// import io.netty.channel.group.DefaultChannelGroup;
import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.service.GameAdminService;
// import org.springframework.beans.factory.annotation.Required;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractNettyServer implements NettyServer {
	protected Session session;
	protected InetSocketAddress socketAddress;
	@Autowired
	protected ServerBootstrap serverBootstrap;
	protected GameAdminService gameAdminService;

	protected Channel serverChannel;

	public AbstractNettyServer() {
		super();
	}

	@Override
	public void stopServer() throws Exception {
		log.debug("In stopServer method of class: {}", this.getClass().getName());
		if (serverChannel != null) {
			serverChannel.close();
			serverChannel.parent().close();
		}
		gameAdminService.shutdown();
	}

	public GameAdminService getGameAdminService() {
		return gameAdminService;
	}

	public void setGameAdminService(GameAdminService gameAdminService) {
		this.gameAdminService = gameAdminService;
	}

	@Override
	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	public void setInetAddress(InetSocketAddress inetAddress) {
		this.socketAddress = inetAddress;
	}

	@Override
	public String toString() {
		return "NettyServer [socketAddress=" + socketAddress + ", portNumber=" + socketAddress.getPort() + "]";
	}

	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public void setSession(Session session) {
		this.session = session;
	}

}
