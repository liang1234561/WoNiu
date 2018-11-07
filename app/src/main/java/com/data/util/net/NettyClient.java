package com.data.util.net;


import android.util.Log;

import com.data.data.HeatResponse;
import com.data.pbprotocol.ChatProtocol;
import com.data.util.net.bean.NetInfo;
import com.data.util.net.bean.ProtocolContext;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class NettyClient {
    private Bootstrap mBootstrap;
    private Channel mChannel;
    private EventLoopGroup mWorkerGroup;
    private OnServerConnectListener onServerConnectListener;
    private Dispatcher mDispatcher;
    private InetSocketAddress socketAddress;
    private int requestTimeout;

    protected ConcurrentHashMap<Integer, NetInfo> requestMap = new ConcurrentHashMap<Integer, NetInfo>();
    private int sequenceID = 0; // 用于标识协议序列sequenceID

    public NettyClient(InetSocketAddress socketAddress, int requestTimeout,
                       OnServerConnectListener onServerConnectListener) {
        mDispatcher = new Dispatcher(this);
        this.socketAddress = socketAddress;
        this.onServerConnectListener = onServerConnectListener;
        this.requestTimeout = requestTimeout;
    }

    public synchronized short getSequenceID() {
        return (short) (sequenceID++ % 10000);
    }

    /**
     * 开始建连
     */
    public void connect() throws Exception {
        if (mChannel != null && mChannel.isActive()) {
            return;
        }
        if (mBootstrap == null) {
            mWorkerGroup = new NioEventLoopGroup();
            mBootstrap = new Bootstrap();
            mBootstrap.group(mWorkerGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel)
                                throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("decoder", new ReeissDecoder(
                                    NettyClient.this));
                            pipeline.addLast("encoder", new ReeissEncoder());
                            pipeline.addLast("handler", mDispatcher);

                        }
                    }).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        }
        ChannelFuture future = mBootstrap.connect(socketAddress);
        future.addListener(mConnectFutureListener);
    }

    private ChannelFutureListener mConnectFutureListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture pChannelFuture)
                throws Exception {
            if (pChannelFuture.isSuccess()) {
                mChannel = pChannelFuture.channel();
                if (onServerConnectListener != null) {
                    onServerConnectListener.onConnectSuccess();
                }
            } else {
                if (onServerConnectListener != null) {
                    onServerConnectListener.onConnectFailed(pChannelFuture
                            .cause());
                }
            }
        }
    };

    /**
     * 处理连接无效
     */
    protected void connectInactive(Throwable cause) {
        if (onServerConnectListener != null) {
            onServerConnectListener.onConnectFailed(cause);
        }
    }

    /**
     * 发送请求
     *
     * @param protocol 发送数据
     *                 响应监听
     * @throws IllegalStateException
     */
    public synchronized void sendRequest(ProtocolContext protocol,
                                         PbAsyncTcpResponse pbAsyncTcpResponse) {
        if (mChannel == null || !mChannel.isWritable() || !mChannel.isActive()) {
            pbAsyncTcpResponse.onFailed(PbAsyncTcpResponse.CONN_SERVER_FAILE);
        }
        if (protocol != null) {
            NetInfo netInfo = new NetInfo();
            protocol.setSequenceID(getSequenceID());
            netInfo.setCode(protocol.getSequenceID());
            netInfo.setProtocol(protocol);
            netInfo.setPbAsyncTcpResponse(pbAsyncTcpResponse);
            requestMap.put(netInfo.getCode(), netInfo);
            if (mChannel != null) {
                mChannel.writeAndFlush(protocol);
            }
            pbAsyncTcpResponse.delayedTimeout(netInfo.getCode(),
                    requestTimeout, this, PbAsyncTcpResponse.SEND_REQUEST_TIMEOUT);
        }
    }

    public synchronized void recvResponse(ProtocolContext protocol) throws InvalidProtocolBufferException {
        if (protocol.getHeaderInfo() != null) {
            int code = protocol.getSequenceID();
            if (requestMap.containsKey(code)) {
                NetInfo netInfo = requestMap.get(code);
                PbAsyncTcpResponse pbAsyncTcpResponse = netInfo
                        .getPbAsyncTcpResponse();
                if (pbAsyncTcpResponse != null) {
                    pbAsyncTcpResponse.handleSuccess(protocol);
                }
                requestMap.remove(code);
            }
        } else {
            EventBus.getDefault().post(new HeatResponse(protocol));
        }
    }

    /**
     * 断开连接
     */
    public void disConnect() throws Exception {
        requestMap.clear();
        if (mWorkerGroup != null) {
            mWorkerGroup.shutdownGracefully();
            mWorkerGroup = null;
            mBootstrap = null;
        }
    }
}
