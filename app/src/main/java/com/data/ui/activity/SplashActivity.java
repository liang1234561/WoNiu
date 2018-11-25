package com.data.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.data.data.GlobObject;
import com.data.data.bean.MsgBean;
import com.data.db.Friend;
import com.data.db.Message;
import com.data.operation.CoreService;
import com.data.util.MobileSystemUtil;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.juns.wechat.Constants;
import com.juns.wechat.GloableParams;
import com.juns.wechat.R;
import com.juns.wechat.bean.GroupInfo;
import com.juns.wechat.bean.User;
import com.juns.wechat.common.Utils;

import net.tsz.afinal.FinalDb;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends Activity implements EasyPermissions.PermissionCallbacks{
	private int REQUEST_CODE_CAMERA = 102;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		checkPermission();
	}

	private void checkPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			if (EasyPermissions.hasPermissions(this, needPermissions)) {
				getLogin();
			} else {
				EasyPermissions.requestPermissions(this, "拍照需要摄像头权限",
						REQUEST_CODE_CAMERA, needPermissions);
			}
		else {
			PackageManager pm = getPackageManager();
			boolean cameraPermission = (PackageManager.PERMISSION_GRANTED ==
					pm.checkPermission(Manifest.permission.CAMERA, "com.juns.wechat"));
			boolean readPermission = (PackageManager.PERMISSION_GRANTED ==
					pm.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, "com.juns.wechat"));
			boolean writePermission = (PackageManager.PERMISSION_GRANTED ==
					pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, "com.juns.wechat"));
			if (cameraPermission && readPermission && writePermission) {
				getLogin();
			} else {
				Utils.showLongToast(this,"没有授予权限");
			}
		}
	}

	private void getLogin() {
		mHandler.sendEmptyMessageDelayed(0, 600);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (!MobileSystemUtil.serviceIsRun(SplashActivity.this, "com.data.operation.CoreService")) {
				Log.e("JerryZhu", "start CoreService.....");
				startService(new Intent(SplashActivity.this,CoreService.class));
			}
			post(new Runnable() {
				@Override
				public void run() {
					if(Utils.getBooleanValue(SplashActivity.this,
							Constants.Conn)){
						Boolean isLogin = Utils.getBooleanValue(SplashActivity.this,
								Constants.LoginState);
						Intent intent = new Intent();
						if (isLogin) {
							intent.setClass(SplashActivity.this, MainActivity.class);
						} else {
							intent.setClass(SplashActivity.this, LoginActivity.class);
						}
						startActivity(intent);
						overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
						finish();
					}else{
						Utils.showLongToast(SplashActivity.this,"服务器连接不上");
						finish();
					}

				}
			});
		}
	};




	private void getChatserive(final String userName, final String password) {
		EMChatManager.getInstance().login(userName, password, new EMCallBack() {// 回调
					@Override
					public void onSuccess() {
						runOnUiThread(new Runnable() {
							public void run() {
								// TODO 保存用户信息
								Utils.putBooleanValue(SplashActivity.this,
										Constants.LoginState, true);
								Utils.putValue(SplashActivity.this,
										Constants.User_ID, userName);
								Utils.putValue(SplashActivity.this,
										Constants.PWD, password);

								Log.e("Token", EMChatManager.getInstance()
										.getAccessToken());
								Log.d("main", "登陆聊天服务器成功！");
								// 加载群组和会话
								EMGroupManager.getInstance().loadAllGroups();
								EMChatManager.getInstance()
										.loadAllConversations();
								mHandler.sendEmptyMessage(0);
							}
						});
					}

					@Override
					public void onProgress(int progress, String status) {

					}

					@Override
					public void onError(int code, String message) {
						Log.d("main", "登陆聊天服务器失败！");
					}
				});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	//成功
	@Override
	public void onPermissionsGranted(int requestCode, List<String> list) {
		if(list.size()>3){
			getLogin();
		}
	}

	//失败
	@Override
	public void onPermissionsDenied(int requestCode, List<String> list) {
		if (EasyPermissions.somePermissionPermanentlyDenied(this, list))
			new AppSettingsDialog.Builder(this).setTitle("需要相机权限").setRationale("请在系统设置去授予程序相机权限").build().show();
		else
			EasyPermissions.requestPermissions(SplashActivity.this, "拍照需要摄像头权限,没有权限将无法使用您的相机图片作为头像",
					REQUEST_CODE_CAMERA, needPermissions);
	}

	protected String[] needPermissions = {
			Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO
	};
}
