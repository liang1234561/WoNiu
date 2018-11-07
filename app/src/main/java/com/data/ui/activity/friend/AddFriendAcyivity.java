package com.data.ui.activity.friend;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.BaseActivity;
import com.data.ui.activity.group.CreateGroupChatActivity;
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

public class AddFriendAcyivity extends BaseActivity implements View.OnClickListener{
    private TextView txt_title;
    private ImageView img_back;
    private EditText search_edit;
    private Button but_add;
    private TextView tv_nickname;
    private int add;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.addfriend);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("添加好友");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        but_add = (Button) findViewById(R.id.btn_add);
        tv_nickname.setText(getIntent().getStringExtra("nickname"));
        add = getIntent().getIntExtra("add",1);
    }


    private void sendPipe(String code,String json){
        if (!App.isOnline()) {
            return;
        }
        Log.e("abc",json);
        RsProtocolContext protocolContext = RequestParamTools.getPipeRequest(code,json);
        if (protocolContext != null) {
            BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
                        Log.e("abc",response.toString());
                        if(response.getErrorCode() == 0){
                            JSONObject jsonObject = JSONObject.parseObject(response.getPipe().getResponse());
                            if(jsonObject.getInteger("code")==0){
                                finish();
                            }else{
                                TT.show(AddFriendAcyivity.this,"添加失败");
                            }
                        }else {
                            TT.show(AddFriendAcyivity.this,""+response.getErrorMessage());
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
        but_add.setOnClickListener(this);
        img_back.setOnClickListener(this);
    }

    private void addfriend(){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("type","add");
        jsonObj.put("username",getIntent().getStringExtra("username"));
        sendPipe("user_friend_service",jsonObj.toJSONString());
    }

    private void agreedfriend(){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("type","agreed");
        jsonObj.put("id",getIntent().getLongExtra("id",0));
        sendPipe("user_friend_service",jsonObj.toJSONString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Utils.finish(AddFriendAcyivity.this);
                break;
            case R.id.btn_add:
                if(add == 1){////2 是同意好友，1是加好友（加群），3是群同意进群
                    if(getIntent().getStringExtra(Constants.TYPE)!=null){
                        if(getIntent().getStringExtra(Constants.TYPE).equals("0")){
                            addfriend();
                        }else{
                            addfriend();
                        }
                    }else{
                        addfriend();
                    }
                }else if(add == 2){
                    agreedfriend();
                }


                break;
            default:
                break;
        }
    }
}
