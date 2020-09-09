package com.weiyi.zhumao.app;

import java.util.List;

import com.weiyi.zhumao.communication.MessageSender;
import com.weiyi.zhumao.communication.MessageSender.Fast;
import com.weiyi.zhumao.communication.MessageSender.Reliable;
import com.weiyi.zhumao.event.Events;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.EventDispatcher;
import com.weiyi.zhumao.event.EventHandler;


public interface Session
{
	/**
	 * session status types
	 */
	enum Status
	{
		NOT_CONNECTED, CONNECTING, CONNECTED, CLOSED
	}

	Object getId();

	void setId(Object id);

	void setAttribute(String key, Object value);

	Object getAttribute(String key);

	void removeAttribute(String key);

	void onEvent(Event event);

	EventDispatcher getEventDispatcher();


	boolean isWriteable();

	void setWriteable(boolean writeable);

	/**
	 * A session would not have UDP capability when created. Depending on the
	 * network abilities of the client, it can request UDP communication to be
	 * enabled with the LOGIN_UDP and CONNECT_UDP events of the {@link Events}
	 * class. Once UDP is enabled this flag will be set to true on the session.
	 * 
	 * @return Returns true if the a UDP {@link MessageSender} instance is
	 *         attached to this session, else false.
	 */
	boolean isUDPEnabled();
	
	/**
	 * A session would not have UDP capability when created. Depending on the
	 * network abilities of the client, it can request UDP communication to be
	 * enabled with the LOGIN_UDP and CONNECT_UDP events of the {@link Events}
	 * class. Once UDP {@link MessageSender} instance is attached to the
	 * session, this method should be called with flag to true to signal that
	 * the session is now UDP enabled.
	 * 
	 * @param isEnabled
	 *            Should be true in most use cases. It is used to signal that
	 *            the UDP {@link MessageSender} has been attached to session.
	 */
	void setUDPEnabled(boolean isEnabled);
	
	boolean isShuttingDown();
	
	long getCreationTime();

	long getLastReadWriteTime();

	void setStatus(Status status);

	Status getStatus();

	boolean isConnected();

	void addHandler(EventHandler eventHandler);
	
	void removeHandler(EventHandler eventHandler);
	
	List<EventHandler> getEventHandlers(int eventType);
	
	void close();

	public abstract void setUdpSender(Fast udpSender);

	public abstract Fast getUdpSender();

	public abstract void setTcpSender(Reliable tcpSender);

	public abstract Reliable getTcpSender();
}
