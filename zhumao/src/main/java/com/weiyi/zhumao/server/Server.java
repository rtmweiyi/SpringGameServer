package com.weiyi.zhumao.server;

import java.net.InetSocketAddress;

import com.weiyi.zhumao.app.Session;

public interface Server {

	public interface TransmissionProtocol{
		
	}
	
	public enum TRANSMISSION_PROTOCOL implements TransmissionProtocol {
		TCP,UDP;
	}
	
	TransmissionProtocol getTransmissionProtocol();
	
	void startServer(InetSocketAddress socketAddress) throws Exception;
	
	void stopServer() throws Exception;
	
	InetSocketAddress getSocketAddress();
	
	Session getSession();
	
	void setSession(Session session);
}
