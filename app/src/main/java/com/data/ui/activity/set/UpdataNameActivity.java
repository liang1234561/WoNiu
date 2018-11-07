package com.data.ui.activity.set;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.data.data.UpdataUser;
import com.data.db.User;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.BaseActivity;
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

public class UpdataNameActivity extends BaseActivity implements View.OnClickListener{

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
        txt_title.setText("修改用户名");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);

        txt_right = (TextView)findViewById(R.id.txt_right);
        txt_right.setText("完成");

        et_username =(EditText)findViewById(R.id.et_username);
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
                Utils.finish(UpdataNameActivity.this);
                break;
            case R.id.txt_right:
                if(et_username.getText().toString().length()>0){
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("type","update");
                    jsonObj.put("nickname",et_username.getText().toString());

                    sendPipe("user_service",jsonObj.toJSONString());
                }else{
                    TT.show(this,"用户名不能为空");
                }
                break;
            default:
                break;
        }
    }

    private void sendPipe(String code,String json){
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
                        if (response.getErrorCode() == 0) {
                            User user = DataSupport.findFirst(User.class);
                            user.setNickname(et_username.getText().toString());
                            user.update(user.getId());

                            Intent intent = new Intent();
                            intent.putExtra("username", et_username.getText().toString()); //将计算的值回传回去
                            setResult(3, intent);
                            finish(UpdataNameActivity.this);
                            EventBus.getDefault().post(new UpdataUser());
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
