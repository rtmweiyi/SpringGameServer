package com.weiyi.zhumao.app.impl;

import org.jetlang.channels.MemoryChannel;
import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.app.Player;
import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.concurrent.LaneStrategy.LaneStrategies;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.EventDispatcher;
import com.weiyi.zhumao.event.impl.EventDispatchers;
import com.weiyi.zhumao.protocols.Protocol;

/**
 * This implementation of the {@link PlayerSession} interface is used to both
 * receive and send messages to a particular player using the
 * {@link #onEvent(com.weiyi.zhumao.event.Event)}. Broadcasts from the
 * {@link GameRoom} are directly patched to the {@link EventDispatcher} which
 * listens on the room's {@link MemoryChannel} for events and in turn publishes
 * them to the listeners.
 * 
 * @author Abraham Menacherry
 * 
 */
public class DefaultPlayerSession extends DefaultSession implements
		PlayerSession
{

	/**
	 * Each session belongs to a Player. This variable holds the reference.
	 */
	final protected Player player;

	/**
	 * Each incoming connection is made to a game room. This reference holds the
	 * association to the game room.
	 */
	protected GameRoom parentGameRoom;
	/**
	 * This variable holds information about the type of binary communication
	 * protocol to be used with this session.
	 */
	protected Protocol protocol;

	protected DefaultPlayerSession(PlayerSessionBuilder playerSessionBuilder)
	{
		super(playerSessionBuilder);
		this.player = playerSessionBuilder.player;
		this.parentGameRoom = playerSessionBuilder.parentGameRoom;
		this.protocol = playerSessionBuilder.protocol;
	}

	public static class PlayerSessionBuilder extends SessionBuilder
	{
		protected Player player = null;
		protected GameRoom parentGameRoom;
		protected Protocol protocol;

		public PlayerSession build()
		{
			return new DefaultPlayerSession(this);
		}

		public PlayerSessionBuilder player(Player player)
		{
			this.player = player;
			return this;
		}

		public PlayerSessionBuilder parentGameRoom(GameRoom parentGameRoom)
		{
			if (null == parentGameRoom)
			{
				throw new IllegalArgumentException(
						"GameRoom instance is null, session will not be constructed");
			}
			this.parentGameRoom = parentGameRoom;
			return this;
		}

		@Override
		protected void validateAndSetValues()
		{
			if (null == eventDispatcher)
			{
				eventDispatcher = EventDispatchers.newJetlangEventDispatcher(
						parentGameRoom, LaneStrategies.GROUP_BY_ROOM);
			}
			super.validateAndSetValues();
		}

		public PlayerSessionBuilder protocol(Protocol protocol)
		{
			this.protocol = protocol;
			return this;
		}
	}

	@Override
	public Player getPlayer()
	{
		//TODO 游戏结束后加入到JoinRoomHandler,以便可以重新开始匹配 
		return player;
	}

	public GameRoom getGameRoom()
	{
		return parentGameRoom;
	}

	public void setGameRoom(GameRoom gameRoom)
	{
		this.parentGameRoom = gameRoom;
	}

	@Override
	public Protocol getProtocol()
	{
		return protocol;
	}

	@Override
	public void setProtocol(Protocol protocol)
	{
		this.protocol = protocol;
	}

	@Override
	public synchronized void close()
	{
		if (!isShuttingDown)
		{
			super.close();
			parentGameRoom.disconnectSession(this);
		}
	}

	@Override
	public void sendToGameRoom(Event event) {
		parentGameRoom.send(event);
	}
	
	@Override
	public String toString()
	{
		return "PlayerSession [id=" + id + "player=" + player
				+ ", parentGameRoom=" + parentGameRoom + ", protocol="
				+ protocol + ", isShuttingDown=" + isShuttingDown + "]";
	}
}
