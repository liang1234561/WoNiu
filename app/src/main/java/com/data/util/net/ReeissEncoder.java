package com.data.util.net;

import com.data.util.net.bean.ProtocolContext;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class ReeissEncoder extends MessageToByteEncoder<ProtocolContext> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ProtocolContext msg, ByteBuf out) throws Exception {
		out.writeBytes(msg.getSendBuffer());
	}

}
