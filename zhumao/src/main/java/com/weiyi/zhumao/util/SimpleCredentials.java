package com.weiyi.zhumao.util;

import io.netty.buffer.ByteBuf;
// import io.netty.buffer.ChannelBuffer;

public class SimpleCredentials implements Credentials
{
	private final String username;
	private final String password;
	
	public SimpleCredentials(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	public SimpleCredentials(ByteBuf buffer)
	{
		this.username = NettyUtils.readString(buffer);
		this.password = NettyUtils.readString(buffer);
	}

	/* (non-Javadoc)
	 * @see org.menacheri.jetserver.jetserver.util.ICredentials#getUsername()
	 */
	@Override
	public String getUsername()
	{
		return username;
	}

	/* (non-Javadoc)
	 * @see org.menacheri.jetserver.jetserver.util.ICredentials#getPassword()
	 */
	@Override
	public String getPassword()
	{
		return password;
	}

	@Override
	public String toString()
	{
		return username;
	}
}
