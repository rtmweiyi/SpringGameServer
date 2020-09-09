package com.weiyi.zhumao.event.impl;

import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.NetworkEvent;
import com.weiyi.zhumao.event.SessionEventHandler;

/**
 * A listener class which will be used by {@link GameRoom} to send
 * {@link NetworkEvent}s to the connected sessions. When the game room
 * publishes such events to its channel, this listener will pick it up and
 * transmit it to the session which in turn will transmit it to the remote
 * machine/vm.
 * 
 * @author Abraham Menacherry
 * 
 */
public class NetworkEventListener implements SessionEventHandler
{

	private static final int EVENT_TYPE = Events.NETWORK_MESSAGE;
	private final Session session;

	public NetworkEventListener(Session session)
	{
		this.session = session;
	}

	@Override
	public void onEvent(Event event)
	{
		session.onEvent(event);
	}

	@Override
	public int getEventType()
	{
		return EVENT_TYPE;
	}

	@Override
	public Session getSession()
	{
		return session;
	}

	@Override
	public void setSession(Session session)
	{
		throw new UnsupportedOperationException(
				"Session is a final field in this class. "
						+ "It cannot be reset");
	}

}
