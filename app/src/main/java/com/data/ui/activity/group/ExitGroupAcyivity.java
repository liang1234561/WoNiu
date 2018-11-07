package com.data.ui.activity.group;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
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

public class ExitGroupAcyivity extends BaseActivity implements View.OnClickListener{
    private TextView txt_title;
    private ImageView img_back;
    private Button but_add;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.exitgroup);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("退群");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        but_add = (Button) findViewById(R.id.btn_add);
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
                        Log.e("abc","自己退群："+response.toString());
                        if(response.getErrorCode() == 0){
                            JSONObject jsonObject = JSONObject.parseObject(response.getPipe().getResponse());
                            if(jsonObject.getInteger("code")==0){
                                finish();
                            }else{
                                TT.show(ExitGroupAcyivity.this,""+jsonObject.getString("message"));
                            }
                        }else {
                            TT.show(ExitGroupAcyivity.this,""+response.getErrorMessage());
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

    private void exitGroup(){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("id",getIntent().getStringExtra("id"));
        jsonObj.put("type","exit");
        sendPipe("user_friend_service",jsonObj.toJSONString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Utils.finish(ExitGroupAcyivity.this);
                break;
            case R.id.btn_add:
                exitGroup();
                break;
            default:
                break;
        }
    }
}
