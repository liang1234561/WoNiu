package com.data.ui.activity.set;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.data.data.UpdataGroupUser;
import com.data.db.User;
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
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;

import org.litepal.crud.DataSupport;

import de.greenrobot.event.EventBus;

public class UpdataGroupNameActivity extends BaseActivity implements View.OnClickListener{

    private TextView txt_title;
    private ImageView img_back;
    private TextView txt_right;
    private EditText et_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.menu_update_corporate_name);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("修改群名");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);

        txt_right = (TextView)findViewById(R.id.txt_right);
        txt_right.setText("完成");

        et_username =(EditText)findViewById(R.id.et_username);
        et_username.setText(getIntent().getStringExtra("name"));
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

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
                Utils.finish(UpdataGroupNameActivity.this);
                break;
            case R.id.txt_right:
                if(et_username.getText().toString().length()>0){
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("id",Long.parseLong(getIntent().getStringExtra("id")));
                    jsonObj.put("name",et_username.getText().toString());
                    sendPipe("modify_qun_service",jsonObj.toJSONString(),et_username.getText().toString());
                }else{
                    TT.show(this,"用户名不能为空");
                }
                break;
            default:
                break;
        }
    }

    private void sendPipe(String code, String json, final String name){
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
                        Log.e("abc","修改群名 response"+response.toString());
                        if (response.getErrorCode() == 0) {
                            JSONObject jsonObject = JSONObject.parseObject(response.getPipe().getResponse());
                            if(jsonObject.getInteger("code")==0){
                                EventBus.getDefault().post(new UpdataGroupUser(name));
                                finish();
                            }else{
                                TT.show(UpdataGroupNameActivity.this,""+jsonObject.getString("message"));
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
}
