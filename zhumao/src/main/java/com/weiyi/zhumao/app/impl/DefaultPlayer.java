package com.weiyi.zhumao.app.impl;

import java.util.HashSet;
import java.util.Set;

import com.weiyi.zhumao.app.Player;
import com.weiyi.zhumao.app.PlayerSession;


public class DefaultPlayer implements Player
{
	/**
	 * This variable could be used as a database key.
	 */
	private Object id;

	/**
	 * The name of the gamer.
	 */
	private String name;

	/**
	 * One player can be connected to multiple games at the same time. Each
	 * session in this set defines a connection to a game. TODO, each player
	 * should not have multiple sessions to the same game.
	 */
	private Set<PlayerSession> playerSessions;
	
	public DefaultPlayer()
	{
		playerSessions = new HashSet<PlayerSession>();
	}
	
	public DefaultPlayer(Object id, String name)
	{
		super();
		this.id = id;
		this.name = name;
		playerSessions = new HashSet<PlayerSession>();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultPlayer other = (DefaultPlayer) obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.weiyi.zhumao.jetserver.app.impl.IGamer#getUniqueKey()
	 */
	public Object getId()
	{
		return id;
	}

	/* (non-Javadoc)
	 * @see com.weiyi.zhumao.jetserver.app.impl.IGamer#setUniqueKey(java.lang.String)
	 */
	public void setId(Object id)
	{
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see com.weiyi.zhumao.jetserver.app.impl.IGamer#getName()
	 */
	public String getName()
	{
		return name;
	}

	/* (non-Javadoc)
	 * @see com.weiyi.zhumao.jetserver.app.impl.IGamer#setName(java.lang.String)
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public synchronized boolean addSession(PlayerSession session)
	{
		return playerSessions.add(session);
	}

	@Override
	public synchronized boolean removeSession(PlayerSession session)
	{
		boolean remove = playerSessions.remove(session);
		if(playerSessions.size() == 0){
			logout(session);
		}
		return remove;
	}
	
	@Override
	public synchronized void logout(PlayerSession session)
	{
		session.close();
		 if(null != playerSessions)
		 {
			 playerSessions.remove(session);
		 }
	}

	public Set<PlayerSession> getPlayerSessions()
	{
		return playerSessions;
	}

	public void setPlayerSessions(Set<PlayerSession> playerSessions)
	{
		this.playerSessions = playerSessions;
	}

}
