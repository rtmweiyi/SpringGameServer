package com.weiyi.zhumao.event.impl;

import com.weiyi.zhumao.communication.MessageSender.Reliable;
import com.weiyi.zhumao.event.Events;

public class ReconnetEvent extends DefaultConnectEvent
{
	private static final long serialVersionUID = 1L;

	public ReconnetEvent(Reliable tcpSender)
	{
		super(tcpSender, null);
	}

	public int getType()
	{
		return Events.RECONNECT;
	}
}