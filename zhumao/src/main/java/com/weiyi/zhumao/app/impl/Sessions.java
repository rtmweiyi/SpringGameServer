package com.weiyi.zhumao.app.impl;

import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.app.Player;
import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.app.impl.DefaultPlayerSession.PlayerSessionBuilder;
import com.weiyi.zhumao.app.impl.DefaultSession.SessionBuilder;


/**
 * Factory class used to create a {@link PlayerSession} instance. It will
 * create a new instance, initialize it and set the {@link GameRoom} reference
 * if necessary.
 * 
 * @author Abraham Menacherry
 * 
 */
public class Sessions
{

	public static Session newSession()
	{
		return new SessionBuilder().build();
	}
	
	public static PlayerSession newPlayerSession(GameRoom gameRoom, Player player)
	{
		return new PlayerSessionBuilder().parentGameRoom(gameRoom).player(player).build();
	}

}