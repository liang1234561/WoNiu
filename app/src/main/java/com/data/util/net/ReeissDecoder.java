package com.data.util.net;



import android.util.Log;

import com.data.util.net.bean.ProtocolContext;
import com.data.util.net.bean.ProtocolHeader;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;



public class ReeissDecoder extends ByteToMessageDecoder {
	private NettyClient nettyClient;
	
	public ReeissDecoder(NettyClient nettyClient) {
		this.nettyClient = nettyClient;
	}
	
	private enum State {
		Header, Body
	}

	private State state = State.Header;
	private ProtocolHeader header;
	private int totalBodySize;
	private short sequence;
	private ProtocolContext protocolContext;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		switch (state) {
		case Header:
			if (in.readableBytes() < 20) {
				return;
			}
			byte[] headBuffer = new byte[14];
			totalBodySize = in.readInt();
			sequence = in.readShort();
			if (nettyClient.requestMap.containsKey((int) sequence)) {
				protocolContext = nettyClient.requestMap.get((int) sequence)
						.getProtocol();
				in.readBytes(headBuffer);
				protocolContext.parseHeader(headBuffer);
				header = protocolContext.getHeaderInfo();
				header.setSequenceID(sequence);
				state = State.Body;
			}else{
				protocolContext = new RsProtocolContext();
				in.readBytes(headBuffer);
				//如果缓存里面数据未存在，则判断数据因超时或者清空等原因直接丢弃
				state = State.Body;
				return;
			}
		case Body:
			if (in.readableBytes() < totalBodySize) {
				return;
			}
			byte[] body = new byte[totalBodySize];
			in.readBytes(body);
			if (protocolContext != null) {
				protocolContext.setBodyBufferRes(body);
				out.add(protocolContext);
			}
			header = null;
			protocolContext = null;
			state = State.Header;
		}
	}
}
