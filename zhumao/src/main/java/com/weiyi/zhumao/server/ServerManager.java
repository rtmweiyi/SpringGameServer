package com.weiyi.zhumao.server;

/**
 * A generic interface used to manage a server.
 * @author Abraham Menacherry
 *
 */
public interface ServerManager
{	
	public void startServers() throws Exception;
	/**
	 * Used to stop the server and manage cleanup of resources. 
	 * 
	 */
	public void stopServers() throws Exception;
}
