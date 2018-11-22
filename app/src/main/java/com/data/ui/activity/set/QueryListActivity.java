package com.data.ui.activity.set;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.BaseActivity;
import com.data.util.list.CustomListView;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;
import com.juns.wechat.common.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class QueryListActivity extends BaseActivity implements View.OnClickListener {
    private TextView txt_title;
    private ImageView img_back;
    private CustomListView addListView;
    private List<String> addFriends;
    private int offset = 1;
    private static final SimpleDateFormat mLogWsdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_listview_query);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("记录明细");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        addListView = (CustomListView) findViewById(R.id.clv_query);
        addFriends = new ArrayList<>();
        addListView.setAdapter(new Myadapter(this, addFriends));
        addListView.setOnRefreshListner(new CustomListView.OnRefreshListner() {
            @Override
            public void onRefresh() {
                sendJson(true);
            }
        });

        addListView.setOnFootLoadingListener(new CustomListView.OnFootLoadingListener() {
            @Override
            public void onFootLoading() {
                sendJson(false);
            }
        });
        sendJson(true);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Utils.finish(QueryListActivity.this);
                break;
            default:
                break;
        }
    }


    class Myadapter extends BaseAdapter {

        private Context mContext;
        private List<String> UserInfos;// 好友信息

        public Myadapter(Context mContext, List<String> UserInfos) {
            this.mContext = mContext;
            this.UserInfos = UserInfos;
        }

        @Override
        public int getCount() {
            return UserInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return UserInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String user = UserInfos.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.point_query, null);
            }
            TextView tvtype = ViewHolder.get(convertView, R.id.tv_type);
            TextView tvpoint = ViewHolder.get(convertView, R.id.tv_point);
            TextView tvdate = ViewHolder.get(convertView, R.id.tv_date);
            JSONObject jsonObject = (JSONObject) JSONObject.parse(user);
            if(jsonObject.get("type") != null){
                tvtype.setText(jsonObject.getString("type"));
            }
            if(jsonObject.get("point") != null){
                tvpoint.setText(jsonObject.getString("point"));
            }
            if(jsonObject.get("in_date") != null){
                tvdate.setText(jsonObject.getString("in_date"));
            }
            return convertView;
        }
    }

    private void sendJson(boolean flag) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("page", offset);
        jsonObj.put("page_size", 10);
        sendPipe("point_service", jsonObj.toJSONString(), flag);
    }

    private void sendPipe(String code, String json, final boolean flag) {
        if (!App.isOnline()) {
            return;
        }
        RsProtocolContext protocolContext = RequestParamTools.getPipeRequest(code, json);
        if (protocolContext != null) {
            BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
                        Log.e("abc",response.toString());
                        if (response.getErrorCode() == 0) {
                            if(flag){
                                offset = 1;
                            }else {
                                offset++;
                            }
                        }
                        addListView.onRefreshComplete();
                        addListView.onFootLoadingComplete();
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                    super.onSuccess(protocol);
                }

                @Override
                protected void onFailed(int type) {
                    addListView.onRefreshComplete();
                    addListView.onFootLoadingComplete();
                }
            });
        } else {

        }
    }
}
