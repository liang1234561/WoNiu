package com.data.operation;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;

import com.data.util.MobileSystemUtil;

/**
 * 监听网络变化
 * @author Administrator
 *
 */
public class BrBroadcastReceiver extends BroadcastReceiver {
	private final String TAG = "BrBroadcastReceiver";
	private Handler handler ;
	
	public BrBroadcastReceiver(Handler msghander){
		handler = msghander;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(handler == null)//开启Service后立即停止Service此时的handler会为空
			return;
		
		//注册接收器时
		if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
			handlerConnectivityAction(context);
		}
	}
	
	private void handlerConnectivityAction(Context context) {
		String netInfo = null;
		if(MobileSystemUtil.isOnline(context)){
			Log.i("JerryZhu", "有网");
			handler.sendEmptyMessageDelayed(BeanMessageHandler.STARTCONN, 0);
		}else{
			netInfo = "设备未连接网络...";
			handler.sendEmptyMessage(BeanMessageHandler.STOPCONN);
		}
	}
}
