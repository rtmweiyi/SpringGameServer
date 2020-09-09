package com.weiyi.zhumao.event.impl;

import java.util.concurrent.ExecutorService;

import org.jetlang.channels.MemoryChannel;
import org.jetlang.fibers.Fiber;
import com.weiyi.zhumao.app.GameRoom;
import com.weiyi.zhumao.concurrent.Fibers;
import com.weiyi.zhumao.concurrent.Lane;
import com.weiyi.zhumao.concurrent.LaneStrategy;
import com.weiyi.zhumao.event.Event;
import com.weiyi.zhumao.event.EventDispatcher;

public class EventDispatchers
{
	public static EventDispatcher newJetlangEventDispatcher(GameRoom room,
			LaneStrategy<String, ExecutorService, GameRoom> strategy)
	{
		Fiber fiber = null;
		JetlangEventDispatcher dispatcher = null;
		if (null == room)
		{
			fiber = Fibers.pooledFiber();
			dispatcher = new JetlangEventDispatcher(new MemoryChannel<Event>(),
					fiber, null);
		}
		else
		{
			Lane<String, ExecutorService> lane = strategy.chooseLane(room);
			fiber = Fibers.pooledFiber(lane);
			dispatcher = new JetlangEventDispatcher(new MemoryChannel<Event>(),
					fiber, lane);
		}
		dispatcher.initialize();

		return dispatcher;
	}
}
