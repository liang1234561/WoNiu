package com.data.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.data.data.UserSyn;
import com.data.db.User;
import com.data.operation.BeanMessageHandler;
import com.data.operation.PreferenceHelper;
import com.data.pbprotocol.ChatProtocol;
import com.data.util.CalendarEx;
import com.data.util.MobileSystemUtil;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;
import com.juns.wechat.view.activity.WebViewActivity;

import org.apache.http.message.BasicNameValuePair;
import org.litepal.crud.DataSupport;

import de.greenrobot.event.EventBus;

//登陆
public class LoginActivity extends BaseActivity implements OnClickListener {
    private TextView txt_title;
    private ImageView img_back;
    private Button btn_login, btn_register;
    private EditText et_usertel, et_password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("登陆");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_register = (Button) findViewById(R.id.btn_qtlogin);
        et_usertel = (EditText) findViewById(R.id.et_usertel);
        et_password = (EditText) findViewById(R.id.et_password);
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
        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        findViewById(R.id.tv_wenti).setOnClickListener(this);
        et_usertel.addTextChangedListener(new TextChange());
        et_password.addTextChangedListener(new TextChange());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Utils.finish(LoginActivity.this);
                break;
            case R.id.tv_wenti:
                Utils.start_Activity(LoginActivity.this, WebViewActivity.class,
                        new BasicNameValuePair(Constants.Title, "帮助"),
                        new BasicNameValuePair(Constants.URL,
                                "http://weixin.qq.com/"));
                break;
            case R.id.btn_qtlogin:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;
            case R.id.btn_login:
                getLogin();
                break;
            default:
                break;
        }
    }

    private void getLogin() {
        String userName = et_usertel.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        showDialog();
        getLogin(userName, password);
    }

    private void getLogin(final String userName, final String password) {
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            if (!App.isOnline()) {
                return;
            }
            RsProtocolContext protocolContext = RequestParamTools.getLoginRequest(userName, password, MobileSystemUtil.getVersion(LoginActivity.this), PreferenceHelper.getClientID(this));
            if (protocolContext != null) {
                BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                    @Override
                    protected void onSuccess(ProtocolContext protocol) {
                        try {
                            ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
                            if (response.getErrorCode() == 0) {
                                ChatProtocol.LoginResponse loginResponse = response.getLogin();
                                Log.i("JerryZhu", loginResponse.toString());
                                DataSupport.deleteAll(User.class);
                                User user = new User();
                                if (loginResponse.hasSessionId()) {
                                    user.setSession_id(loginResponse.getSessionId());
                                }
                                if (loginResponse.hasUserId()) {
                                    user.setUser_id(loginResponse.getUserId());
                                    PreferenceHelper.setLong("USERID",loginResponse.getUserId(),LoginActivity.this);
                                    BeanMessageHandler.userId = loginResponse.getUserId();
                                }else{
                                    BeanMessageHandler.userId = 0;
                                }
                                user.save();
                                EventBus.getDefault().post(new UserSyn());
                                CalendarEx.updateServerTime(loginResponse.getServerTime());
                                if (loginResponse.hasNewVersion()) {
                                    PreferenceHelper.setVersionUrl(loginResponse.getDownloadUrl(), LoginActivity.this);
                                }
                                Utils.putBooleanValue(LoginActivity.this,
                                        Constants.LoginState, true);
                                dismissDialog();
                                Intent intent = new Intent(LoginActivity.this,
                                        MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.push_up_in,
                                        R.anim.push_up_out);
                                finish();
                            } else {
                                dismissDialog();
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        super.onSuccess(protocol);
                    }

                    @Override
                    protected void onFailed(int type) {
                        dismissDialog();
                        Utils.showLongToast(LoginActivity.this, "连接服务器异常");
                    }
                });
            } else {
                dismissDialog();
            }
        } else {
            dismissDialog();
            Utils.showLongToast(LoginActivity.this, "请填写账号或密码！");
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
            boolean Sign2 = et_usertel.getText().length() > 0;
            boolean Sign3 = et_password.getText().length() > 0;
            if (Sign2 & Sign3) {
                btn_login.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.btn_bg_green));
                btn_login.setEnabled(true);
            } else {
                btn_login.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.btn_enable_green));
                btn_login.setTextColor(0xFFD0EFC6);
                btn_login.setEnabled(false);
            }
        }
    }

}
