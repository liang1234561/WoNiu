package com.data.util.net;


import android.util.Log;

import com.data.util.net.bean.ProtocolContext;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class Dispatcher extends SimpleChannelInboundHandler {
	private NettyClient nettyClient;
	
	public Dispatcher(NettyClient nettyClient) {
		this.nettyClient = nettyClient;
	}
	
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
    	nettyClient.recvResponse((ProtocolContext)msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.e("abc","rsnetwork execption:"+cause.getMessage());
        ctx.close();
        nettyClient.connectInactive(cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ctx.close();
    }
}
