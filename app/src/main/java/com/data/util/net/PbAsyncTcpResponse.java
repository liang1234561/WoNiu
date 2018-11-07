package com.data.util.net;

import com.data.util.net.bean.ProtocolContext;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;




public class PbAsyncTcpResponse {
	public static int CONN_SERVER_FAILE = 0;
	public static int SEND_REQUEST_TIMEOUT = 1;
	
    final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    protected void delayedTimeout(final int code, int timeout, final NettyClient nettyClient, final int type) {
        scheduledExecutorService.schedule(new TimerTask() {
            @Override
            public void run() {
                if (nettyClient.requestMap.containsKey(code)) {
                	nettyClient.requestMap.remove(code);
                	onFailed(type);
                }
            }
        }, timeout, TimeUnit.MILLISECONDS);

    }

    protected void handleSuccess(ProtocolContext protocol) {
        scheduledExecutorService.shutdownNow();
        onSuccess(protocol);
    }

    protected void onSuccess(ProtocolContext protocol) {
    }
    /**
     * CONN_SERVER_FAILE.未连接
     * SEND_REQUEST_TIMEOUT 发送数据超时
     * @param type
     */
    protected void onFailed(int type) {
    }
}
