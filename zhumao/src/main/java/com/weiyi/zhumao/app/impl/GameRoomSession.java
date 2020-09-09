package com.weiyi.zhumao.app.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.weiyi.zhumao.app.Game;
import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.app.Player;
import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.concurrent.LaneStrategy;
import com.weiyi.zhumao.concurrent.LaneStrategy.LaneStrategies;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.EventHandler;
import com.weiyi.zhumao.event.NetworkEvent;
import com.weiyi.zhumao.event.impl.EventDispatchers;
import com.weiyi.zhumao.event.impl.NetworkEventListener;
import com.weiyi.zhumao.protocols.Protocol;
import com.weiyi.zhumao.service.GameStateManagerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GameRoomSession extends DefaultSession implements GameRoom
{
	
	/**
	 * The name of the game room, preferably unique across multiple games.
	 */
	protected String gameRoomName;
	/**
	 * The parent {@link SimpleGame} reference of this game room.
	 */
	protected Game parentGame;
	/**
	 * Each game room has separate state manager instances. This variable will
	 * manage the state for all the {@link DefaultPlayer}s connected to this game room.
	 */
	protected GameStateManagerService stateManager;

	/**
	 * The set of sessions in this object.
	 */
	protected Set<PlayerSession> sessions;
	
	/**
	 * Each game room has its own protocol for communication with client.
	 */
	protected Protocol protocol;

	//退出房间需要换回大厅的协议
	protected Protocol lobbyProtocol;
	
	protected GameRoomSession(GameRoomSessionBuilder gameRoomSessionBuilder)
	{
		super(gameRoomSessionBuilder);
		this.sessions = gameRoomSessionBuilder.sessions;
		this.parentGame = gameRoomSessionBuilder.parentGame;
		this.gameRoomName = gameRoomSessionBuilder.gameRoomName;
		this.protocol = gameRoomSessionBuilder.protocol;
		this.lobbyProtocol = gameRoomSessionBuilder.lobbyProtocol;
		if(null == gameRoomSessionBuilder.eventDispatcher)
		{
			this.eventDispatcher = EventDispatchers.newJetlangEventDispatcher(
					this, gameRoomSessionBuilder.laneStrategy);
		}
	}
	
	public static class GameRoomSessionBuilder extends SessionBuilder
	{
		protected Set<PlayerSession> sessions;
		protected Game parentGame;
		protected String gameRoomName;
		protected Protocol protocol;
		protected LaneStrategy<String, ExecutorService, GameRoom> laneStrategy;
		protected Protocol lobbyProtocol;
		
		@Override
		protected void validateAndSetValues()
		{
			if (null == id)
			{
				id = String.valueOf(ID_GENERATOR_SERVICE.generateFor(GameRoomSession.class));
			}
			if(null == sessionAttributes)
			{
				sessionAttributes = new HashMap<String, Object>();
			}
			if (null == sessions)
			{
				sessions = new HashSet<PlayerSession>();
			}
			if (null == laneStrategy)
			{
				laneStrategy = LaneStrategies.GROUP_BY_ROOM;
			}
			creationTime = System.currentTimeMillis();
		}
		
		public GameRoomSessionBuilder sessions(Set<PlayerSession> sessions)
		{
			this.sessions = sessions;
			return this;
		}
		
		public GameRoomSessionBuilder parentGame(Game parentGame)
		{
			this.parentGame = parentGame;
			return this;
		}
		
		public GameRoomSessionBuilder gameRoomName(String gameRoomName)
		{
			this.gameRoomName = gameRoomName;
			return this;
		}
		
		public GameRoomSessionBuilder protocol(Protocol protocol)
		{
			this.protocol = protocol;
			return this;
		}
		
		public GameRoomSessionBuilder laneStrategy(
				LaneStrategy<String, ExecutorService, GameRoom> laneStrategy)
		{
			this.laneStrategy = laneStrategy;
			return this;
		}

		//推出房间，则启用返回大厅的协议
		public GameRoomSessionBuilder lobbyProtocol(Protocol protocol){
			this.lobbyProtocol = protocol;
			return this;
		}
	}
	
	@Override
	public PlayerSession createPlayerSession(Player player)
	{
		PlayerSession playerSession = getSessionInstance(player);
		return playerSession;
	}
	
	@Override
	public abstract void onLogin(PlayerSession playerSession);
	
	@Override
	public synchronized boolean connectSession(PlayerSession playerSession)
	{
		if (!isShuttingDown)
		{
			playerSession.setStatus(Session.Status.CONNECTING);
			sessions.add(playerSession);
			playerSession.setGameRoom(this);
			log.debug("Protocol to be applied is: {}",protocol.getClass().getName());
			protocol.applyProtocol(playerSession,true);
			createAndAddEventHandlers(playerSession);
			playerSession.setStatus(Session.Status.CONNECTED);
			afterSessionConnect(playerSession);
			return true;
			// TODO send event to all other sessions?
		}
		else
		{
			log.warn("Game Room is shutting down, playerSession {} {}",
					playerSession,"will not be connected!");
			return false;
		}
	}

	@Override
	public void afterSessionConnect(PlayerSession playerSession)
	{
			
	}
	
	public synchronized boolean disconnectSession(PlayerSession playerSession)
	{
		final boolean removeHandlers = this.eventDispatcher.removeHandlersForSession(playerSession);
		//playerSession.getEventDispatcher().clear(); // remove network handlers of the session.
		return (removeHandlers && sessions.remove(playerSession));
	}

	@Override
	public void send(Event event) {
		onEvent(event);
	}
	
	@Override
	public void sendBroadcast(NetworkEvent networkEvent)
	{
		onEvent(networkEvent);
	}

	@Override
	public synchronized void close()
	{
		isShuttingDown = true;
		for(PlayerSession session: sessions)
		{
			session.close();
		}
	}
	
	public PlayerSession getSessionInstance(Player player)
	{
		PlayerSession playerSession = Sessions.newPlayerSession(this,player);
		return playerSession;
	}
	
	@Override
	public Set<PlayerSession> getSessions()
	{
		return sessions;
	}

	@Override
	public void setSessions(Set<PlayerSession> sessions)
	{
		this.sessions = sessions;
	}
	
	@Override
	public String getGameRoomName()
	{
		return gameRoomName;
	}

	@Override
	public void setGameRoomName(String gameRoomName)
	{
		this.gameRoomName = gameRoomName;
	}

	@Override
	public Game getParentGame()
	{
		return parentGame;
	}

	@Override
	public void setParentGame(Game parentGame)
	{
		this.parentGame = parentGame;
	}

	@Override
	public void setStateManager(GameStateManagerService stateManager)
	{
		this.stateManager = stateManager;
	}
	
	@Override
	public GameStateManagerService getStateManager()
	{
		return stateManager;
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
	public boolean isShuttingDown()
	{
		return isShuttingDown;
	}

	public void setShuttingDown(boolean isShuttingDown)
	{
		this.isShuttingDown = isShuttingDown;
	}

	/**
	 * Method which will create and add event handlers of the player session to
	 * the Game Room's EventDispatcher.
	 * 
	 * @param playerSession
	 *            The session for which the event handlers are created.
	 */
	protected void createAndAddEventHandlers(PlayerSession playerSession)
	{
		// Create a network event listener for the player session.
		EventHandler networkEventHandler = new NetworkEventListener(playerSession);
		// Add the handler to the game room's EventDispatcher so that it will
		// pass game room network events to player session session.
		this.eventDispatcher.addHandler(networkEventHandler);
		log.trace("Added Network handler to "
				+ "EventDispatcher of GameRoom {}, for session: {}", this,
				playerSession);
	}
}
