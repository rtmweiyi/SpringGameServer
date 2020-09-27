package com.weiyi.zhumao.server.netty;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import com.weiyi.zhumao.server.ServerManager;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Component
public class ServerManagerImpl implements ServerManager
{
	private Set<AbstractNettyServer> servers;
    
    @Autowired
	AbstractNettyServer tcpServer;

	@Autowired
	InetSocketAddress socketAddress;

	
	public ServerManagerImpl()
	{
		servers = new HashSet<AbstractNettyServer>();
	}
	
	@Override
	public void startServers() throws Exception 
	{
		log.info("Server start!");
		tcpServer.startServer(socketAddress);
		servers.add(tcpServer);
	}
	
	@Override
	public void stopServers() throws Exception
	{
		for(AbstractNettyServer nettyServer: servers){
			try
			{
				nettyServer.stopServer();
			}
			catch (Exception e)
			{
				log.error("Unable to stop server {} due to error {}", nettyServer,e);
				throw e;
			}
		}
	}

}
