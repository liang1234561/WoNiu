package com.data.operation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.data.data.HeatResponse;
import com.data.data.UserSyn;
import com.data.pbprotocol.ChatProtocol;
import com.google.protobuf.InvalidProtocolBufferException;

import de.greenrobot.event.EventBus;

public class CoreService extends Service {
    private static WakeLock mWakeLock;
    private static PowerManager pm;
    private static Context mAppContext;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    /**
     * 保持手机在后台运行
     */
    public static void newacquireWakeLock() {
//		MonkeyCommond.startWakePhoneThread(OfferManager.getApplicationContext());
        if (mWakeLock == null) {
            if (mAppContext != null) {
                pm = (PowerManager) mAppContext.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tag");
                }
            }
        }

        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        newacquireWakeLock();
        EventBus.getDefault().register(this);
        mAppContext = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initLooperHandler();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initLooperHandler() {
        mHandlerThread = new HandlerThread("BeanHandler");
        mHandlerThread.start();
        mHandler = new BeanMessageHandler(mHandlerThread.getLooper(),this);
        mHandler.removeMessages(0);
        mHandler.sendMessage(mHandler.obtainMessage(BeanMessageHandler.INIT,
                getApplicationContext()));
        mHandler.sendEmptyMessageDelayed(BeanMessageHandler.HEAT,30*1000);
    }



    @Override
    public void onDestroy() {
        new Thread() {
            public void run() {
                try {
                    ((BeanMessageHandler) mHandler).waitQuit();
                    mHandler = null;
                    mHandlerThread.quit();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void onEventMainThread(HeatResponse protocol){
        ChatProtocol.Response response = null;
        try {
            response = ChatProtocol.Response.parseFrom(protocol.getProtocolContext().getBodyBuffer());
            Log.e("abc","处理推送:"+response.toString());
            if(response.getHeartbeat()!=null){
                ChatProtocol.HeartbeatResponse heartbeatResponse = response.getHeartbeat();
                if(heartbeatResponse.getFriendChanged() != 0){
                    mHandler.sendEmptyMessage(BeanMessageHandler.FRIENDSYNC);
                }
                if(heartbeatResponse.getMessageChanged() != 0){
                    mHandler.sendEmptyMessage(BeanMessageHandler.MESSAGESYSNC);
                }

            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    public void onEventMainThread(UserSyn userSyn){
        mHandler.sendEmptyMessage(BeanMessageHandler.USERSYN);
    }

}
