package com.weiyi.zhumao.event.impl;

import com.weiyi.zhumao.app.Session;
import com.weiyi.zhumao.event.EventContext;


public class DefaultEventContext implements EventContext
{

	private Object attachement;
	private Session session;

	@Override
	public Object getAttachment()
	{
		return attachement;
	}

	@Override
	public Session getSession()
	{
		return session;
	}

	@Override
	public void setAttachment(Object attachement)
	{
		this.attachement = attachement;
	}

	@Override
	public void setSession(Session session)
	{
		this.session = session;
	}

}
