package com.weiyi.zhumao.protocols.impl;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

import com.weiyi.zhumao.app.PlayerSession;
import com.weiyi.zhumao.handlers.netty.DefaultToServerHandler;
import com.weiyi.zhumao.handlers.netty.MessageBufferEventDecoder;
import com.weiyi.zhumao.handlers.netty.MessageBufferEventEncoder;
import com.weiyi.zhumao.protocols.AbstractNettyProtocol;
import com.weiyi.zhumao.util.NettyUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageBufferProtocol extends AbstractNettyProtocol
{
	/**
	 * Utility handler provided by netty to add the length of the outgoing
	 * message to the message as a header.
	 */
	@Autowired
	private LengthFieldPrepender lengthFieldPrepender;

	@Autowired 
	MessageBufferEventDecoder messageBufferEventDecoder;

	@Autowired
	MessageBufferEventEncoder messageBufferEventEncoder;
	
	public MessageBufferProtocol()
	{
		super("MESSAGE_BUFFER_PROTOCOL");
	}
	
	//加入房间的时候会调用DefaultToServerHandler
	@Override
	public void applyProtocol(PlayerSession playerSession)
	{
		log.trace("Going to apply {} on session: {}", getProtocolName(),
				playerSession);
		
		ChannelPipeline pipeline = NettyUtils
				.getPipeLineOfConnection(playerSession);
		// Upstream handlers or encoders (i.e towards server) are added to
		// pipeline now.
		pipeline.addLast("lengthDecoder", createLengthBasedFrameDecoder());
		pipeline.addLast("messageBufferEventDecoder", messageBufferEventDecoder);
		pipeline.addLast("eventHandler", new DefaultToServerHandler(
				playerSession));

		// Downstream handlers - Filter for data which flows from server to
		// client. Note that the last handler added is actually the first
		// handler for outgoing data.
		pipeline.addLast("lengthFieldPrepender", lengthFieldPrepender);
		pipeline.addLast("messageBufferEventEncoder",messageBufferEventEncoder);

	}

	public LengthFieldPrepender getLengthFieldPrepender()
	{
		return lengthFieldPrepender;
	}

	public void setLengthFieldPrepender(LengthFieldPrepender lengthFieldPrepender)
	{
		this.lengthFieldPrepender = lengthFieldPrepender;
	}
	

}
