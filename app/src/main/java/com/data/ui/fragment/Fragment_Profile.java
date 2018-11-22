package com.data.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.data.data.GlobObject;
import com.data.data.NewMessage;
import com.data.data.OutLogin;
import com.data.data.UpdataUser;
import com.data.db.Friend;
import com.data.db.Message;
import com.data.db.User;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.LoginActivity;
import com.data.ui.activity.MainActivity;
import com.data.ui.activity.set.QueryListActivity;
import com.data.ui.activity.set.UpdataImageActivity;
import com.data.ui.view.TT;
import com.data.util.MobileSystemUtil;
import com.data.util.image.SmartImageView;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;
import com.juns.wechat.view.activity.PublicActivity;
import com.juns.wechat.view.activity.SettingActivity;

import org.apache.http.message.BasicNameValuePair;
import org.litepal.crud.DataSupport;

import de.greenrobot.event.EventBus;

//我
public class Fragment_Profile extends Fragment implements OnClickListener {
    private Activity ctx;
    private View layout;
    private TextView tvname, tv_accout;
    private SmartImageView smartImageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (layout == null) {
            ctx = this.getActivity();
            layout = ctx.getLayoutInflater().inflate(R.layout.fragment_profile,
                    null);
            initViews();
            initData();
            setOnListener();
        } else {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        return layout;
    }

    private void initViews() {
        tvname = (TextView) layout.findViewById(R.id.tvname);
        tv_accout = (TextView) layout.findViewById(R.id.tvmsg);
        smartImageView = (SmartImageView) layout.findViewById(R.id.head);
    }

    private void setOnListener() {
        layout.findViewById(R.id.view_user).setOnClickListener(this);
        layout.findViewById(R.id.txt_album).setOnClickListener(this);
        layout.findViewById(R.id.txt_collect).setOnClickListener(this);
        layout.findViewById(R.id.txt_money).setOnClickListener(this);
        layout.findViewById(R.id.txt_card).setOnClickListener(this);
        layout.findViewById(R.id.txt_smail).setOnClickListener(this);
        layout.findViewById(R.id.txt_setting).setOnClickListener(this);
        layout.findViewById(R.id.btn_outlogin).setOnClickListener(this);
        layout.findViewById(R.id.btn_update).setOnClickListener(this);
        layout.findViewById(R.id.btn_query).setOnClickListener(this);
    }

    private void initData() {
        User user = DataSupport.findFirst(User.class);
        if(user != null){
            if(user.getNickname() != null){
                tvname.setText(user.getNickname());
            }
            if(user.getName()!=null){
                tv_accout.setText(user.getName());
            }
            if(user.getPhoto() != null){
                smartImageView.setImageUrl(user.getPhoto());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_user:
                Utils.start_Activity(getActivity(), UpdataImageActivity.class);
                break;
            case R.id.btn_outlogin:// 退出
                logoutRequest();
                break;
            case R.id.btn_update:// 查看版本
                ((MainActivity)getActivity()).get("http://chat.tytools.cn/common/checkVersion.json?type=1&version="+ MobileSystemUtil.getVersion(getContext()),true);
                break;
            case R.id.btn_query:// 查点数
                Utils.start_Activity(getActivity(), QueryListActivity.class);
                break;
            case R.id.txt_card:// 相册
                Utils.start_Activity(getActivity(), PublicActivity.class,
                        new BasicNameValuePair(Constants.NAME,
                                getString(R.string.card_bag)));
                break;
            case R.id.txt_smail:// 表情
                Utils.start_Activity(getActivity(), PublicActivity.class,
                        new BasicNameValuePair(Constants.NAME,
                                getString(R.string.expression)));
                break;
            case R.id.txt_setting:// 设置
                Utils.start_Activity(getActivity(), SettingActivity.class);
                break;
            default:
                break;
        }
    }

    private void logoutRequest() {
        if (!App.isOnline()) {
            return;
        }
        RsProtocolContext protocolContext = RequestParamTools.getLogoutRequest();
        if (protocolContext != null) {
            BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
                        Log.e("abc", response.toString());
                        if (response.getErrorCode() == 0 || response.getErrorCode() == 300) {
                            logout();
                        } else {
                            TT.show(getActivity(), "" + response.getErrorMessage());
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

    private void logout(){
        DataSupport.deleteAll(Friend.class);
        DataSupport.deleteAll(Message.class);
        DataSupport.deleteAll(User.class);
        GlobObject.clear();
        Utils.putBooleanValue(getActivity(),
                Constants.LoginState, false);
        Utils.start_Activity(getActivity(), LoginActivity.class);
    }

    public void onEventMainThread(UpdataUser updataUser){
        initData();
    }

    public void onEventMainThread(OutLogin outLogin){
        logout();
    }

}