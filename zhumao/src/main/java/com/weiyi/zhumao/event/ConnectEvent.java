package com.weiyi.zhumao.event;

import com.weiyi.zhumao.communication.MessageSender.Fast;
import com.weiyi.zhumao.communication.MessageSender.Reliable;

public interface ConnectEvent extends Event
{
	public Reliable getTcpSender();
	public void setTcpSender(Reliable tcpSender);
	public Fast getUdpSender();
	public void setUdpSender(Fast udpSender);
}
