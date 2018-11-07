package com.data.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.friend.AddFriendAcyivity;
import com.data.ui.view.TT;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;

//搜索
public class SearchActivity extends BaseActivity implements OnClickListener {
	private TextView txt_title;
	private ImageView img_back;
	private EditText search_edit;
	private TextView txt_right;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.searchedit);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void initControl() {
		txt_title = (TextView) findViewById(R.id.txt_title);
		txt_title.setText("搜索");
		txt_right = (TextView)findViewById(R.id.txt_right);
		txt_right.setText("完成");
		img_back = (ImageView) findViewById(R.id.img_back);
		img_back.setVisibility(View.VISIBLE);
		search_edit = (EditText) findViewById(R.id.search_edit);
		search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				//以下方法防止两次发送请求
				if (actionId == EditorInfo.IME_ACTION_SEND ||
						(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					switch (event.getAction()) {
						case KeyEvent.ACTION_UP:

							JSONObject jsonObj = new JSONObject();
							jsonObj.put("username",v.getText().toString());
							sendPipe("search_friend_service",jsonObj.toJSONString(),v.getText().toString());
							TT.show(SearchActivity.this,v.getText().toString());
							return true;
						default:
							return true;
					}
				}
				return false;
			}
		});

	}


	private void sendPipe(String code, String json, final String username){
		if (!App.isOnline()) {
			return;
		}
		RsProtocolContext protocolContext = RequestParamTools.getPipeRequest(code,json);
		if (protocolContext != null) {
			BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
				@Override
				protected void onSuccess(ProtocolContext protocol) {
					try {
						ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
						Log.e("abc",response.toString());
						if(response.getErrorCode() == 0){
							ChatProtocol.PipeResponse pipeResponse = response.getPipe();
							Intent intent = new Intent(SearchActivity.this, AddFriendAcyivity.class);
							JSONObject jsonObject = JSONObject.parseObject(pipeResponse.getResponse());
							if(jsonObject.get("response") != null){
								if(JSONObject.parseObject(jsonObject.get("response").toString()).get("name") != null){
									intent.putExtra("nickname",JSONObject.parseObject(jsonObject.get("response").toString()).get("name").toString());
								}else{
									intent.putExtra("nickname",username);
								}
								intent.putExtra("username",username);
								if(getIntent().getStringExtra(Constants.TYPE)!=null){
									intent.putExtra(Constants.TYPE,getIntent().getStringExtra(Constants.TYPE));
								}
								intent.putExtra("add",1);
								startActivity(intent);
								finish();
								overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
							}
						}

					} catch (InvalidProtocolBufferException e) {
						e.printStackTrace();
					}
					super.onSuccess(protocol);
				}

				@Override
				protected void onFailed(int type) {

				}
			});
		} else {

		}
	}

	@Override
	protected void initView() {

	}

	@Override
	protected void initData() {
		// TODO 根据时间排序加载 订阅号信息列表
	}

	@Override
	protected void setListener() {
		img_back.setOnClickListener(this);
		txt_right.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_back:
			Utils.finish(SearchActivity.this);
			break;
		case R.id.txt_right:
			JSONObject jsonObj = new JSONObject();
			if(search_edit!=null && search_edit.getText()!=null&&search_edit.getText().toString().length()>0){
				jsonObj.put("username",search_edit.getText().toString());
				sendPipe("search_friend_service",jsonObj.toJSONString(),search_edit.getText().toString());
			}else{
				TT.show(this,"账号或者群号不能为空");
			}
			break;
		default:
			break;
		}
	}

}
