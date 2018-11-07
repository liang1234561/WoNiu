package com.data.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.view.CircleDialog;
import com.data.ui.view.TT;
import com.data.util.image.SmartImageView;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.MainActivity;
import com.data.MyApplication;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;


//注册
public class RegisterActivity extends BaseActivity implements OnClickListener {
    private TextView txt_title;
    private ImageView img_back;
    private Button btn_register ;
    private SmartImageView btn_send;
    private EditText et_usertel, et_password, et_code;
    private MyCount mc;
    private long key ;
//http://127.0.0.1/source/common/image.jsp?key=1234
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_register);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("注册");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        btn_send = (SmartImageView) findViewById(R.id.btn_send);
        btn_register = (Button) findViewById(R.id.btn_register);
        et_usertel = (EditText) findViewById(R.id.et_usertel);
        et_password = (EditText) findViewById(R.id.et_password);
        et_code = (EditText) findViewById(R.id.et_code);
        key = System.currentTimeMillis();
        btn_send.setImageUrl("http://chat.tytools.cn/source/common/image.jsp?key="+key);
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
        btn_send.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        et_password.addTextChangedListener(new TextChange());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Utils.finish(RegisterActivity.this);
                break;
            case R.id.btn_send:
                key = System.currentTimeMillis();
                btn_send.setImageUrl("http://chat.tytools.cn/source/common/image.jsp?key="+key);
                break;
            case R.id.btn_register:
                getRegister();
                break;
            default:
                break;
        }
    }

    private void getRegister() {
        final String name = et_usertel.getText().toString();
        final String pwd = et_password.getText().toString();
        String code = et_code.getText().toString();
        if (TextUtils.isEmpty(code)) {
            Utils.showLongToast(RegisterActivity.this, "请填写手机号码，并获取验证码！");
            return;
        }
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)
                || TextUtils.isEmpty(code)) {
            Utils.showLongToast(RegisterActivity.this, "请填写核心信息！");
            return;
        }
        CircleDialog.getInstance().showDialog(RegisterActivity.this);

        if (!App.isOnline()) {
            return;
        }
        RsProtocolContext protocolContext = RequestParamTools.getRegistRequest(name, pwd, code ,key);
        if (protocolContext != null) {
            BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
                        if(response.getErrorCode() == 0){
                            TT.show(RegisterActivity.this,"注册成功");
                            finish(RegisterActivity.this);
                        }else {
                            TT.show(RegisterActivity.this,response.getErrorMessage());
                        }
                        CircleDialog.getInstance().dismissDialog();
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                    super.onSuccess(protocol);
                }

                @Override
                protected void onFailed(int type) {
                    dismissDialog();
                }
            });
        } else {

        }
    }


    private void getCode() {
        String phone = et_usertel.getText().toString();
        if (!App.isOnline()) {
            return;
        }
        RsProtocolContext protocolContext = RequestParamTools.getCodeRequest(phone);
        if (protocolContext != null) {
            BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
                        dismissDialog();
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                    super.onSuccess(protocol);
                }

                @Override
                protected void onFailed(int type) {
                    dismissDialog();
                    btn_register.setEnabled(true);
                    btn_send.setEnabled(true);
                }
            });
        } else {

        }
    }


    // EditText监听器
    class TextChange implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {
            boolean Sign1 = et_code.getText().length() > 0;
            boolean Sign2 = et_usertel.getText().length() > 0;
            boolean Sign3 = et_password.getText().length() > 0;

            if (Sign1 & Sign2 & Sign3) {
                btn_register.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.btn_bg_green));
                btn_register.setTextColor(0xFFFFFFFF);
                btn_register.setEnabled(true);
            } else {
                btn_register.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.btn_enable_green));
                btn_register.setTextColor(0xFFD0EFC6);
                btn_register.setEnabled(false);
            }
        }
    }

    /* 定义一个倒计时的内部类 */
    private class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            btn_send.setEnabled(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btn_send.setEnabled(false);
        }
    }

    private void initUserList() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
        finish();
    }
}
