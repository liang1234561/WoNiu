package com.data.ui.activity.set;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.BaseActivity;
import com.data.util.list.PullToRefreshBase;
import com.data.util.list.PullToRefreshMenuView;
import com.data.util.list.SwipeMenuListView;
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
import java.util.Date;
import java.util.List;

public class QueryListActivity extends BaseActivity implements View.OnClickListener , PullToRefreshBase.OnRefreshListener<SwipeMenuListView>{
    private TextView txt_title;
    private ImageView img_back;
    private List<String> addFriends;
    private static final SimpleDateFormat mLogWsdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private Myadapter myadapter;
    protected int page = 1;
    protected PullToRefreshMenuView refreshMenuView;
    protected SwipeMenuListView swipeMenuListView;


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
        addFriends = new ArrayList<>();
        myadapter = new Myadapter(this, addFriends);
//        sendJson(true);
        refreshMenuView = (PullToRefreshMenuView)findViewById(R.id.pull_base);
        refreshMenuView.setPullLoadEnabled(false);
        refreshMenuView.setScrollLoadEnabled(true);
        refreshMenuView.setOnRefreshListener(this);
        swipeMenuListView = refreshMenuView.getRefreshableView();
        refreshMenuView.onRefreshComplete();

        // 操作ListView左滑时的手势操作，这里用于处理上下左右滑动冲突：开始滑动时则禁止下拉刷新和上拉加载手势操作，结束滑动后恢复上下拉操作
        swipeMenuListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                refreshMenuView.setPullRefreshEnabled(false);
            }

            @Override
            public void onSwipeEnd(int position) {
                refreshMenuView.setPullRefreshEnabled(true);
            }
        });
        swipeMenuListView.setAdapter(myadapter);
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

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView) {
        page = 1;
        sendJson(true);
    }

    @Override
    public void onPushUpToRefresh(PullToRefreshBase<SwipeMenuListView> refreshView) {
        page++;
        sendJson(false);
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
                int type  = jsonObject.getInteger("type");
                if(type  == 1){
                    tvtype.setText("充值");
                }else{
                    tvtype.setText("消费");
                }
            }
            if(jsonObject.get("point") != null){
                tvpoint.setText(jsonObject.getInteger("point")+"");
            }
            if(jsonObject.get("in_date") != null){
                tvdate.setText(mLogWsdf.format(new Date(jsonObject.getLong("in_date"))));
            }
            return convertView;
        }
    }

    private void sendJson(final boolean flag) {
        Log.e("abc","jiazai yici ");
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("page", page);
        jsonObj.put("page_size", 10);
        sendPipe("point_service", jsonObj.toJSONString(), flag);
        showDialog();
    }

    private void sendPipe(String code, String json, final boolean flag) {
        if (!App.isOnline()) {
            return;
        }
        RsProtocolContext protocolContext = RequestParamTools.getPipeRequest(code, json);
        if (protocolContext != null) {
            BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(final ProtocolContext protocol) {
                    QueryListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handlerResponse(protocol,flag);
                        }
                    });
                }

                @Override
                protected void onFailed(int type) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            onResponseFail();
                            dismissDialog();
                        }
                    });

                }
            });
        } else {

        }
    }

    private void handlerResponse(ProtocolContext protocol,final boolean flag){
        dismissDialog();
        ChatProtocol.Response response = null;
        try {
            response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONObject.parseObject(response.getPipe().getResponse());
        Log.e("abc", response.toString());
        if (jsonObject.getInteger("code") == 0) {
            JSONArray array = jsonObject.getJSONArray("response");
            if (flag) {
                addFriends.clear();
            }
            for (int i = 0; i < array.size(); i++) {
                addFriends.add(array.get(i).toString());
            }
            reFreshFinish(array.size());
        } else {
            onResponseFail();
        }
        myadapter.notifyDataSetChanged();
    }

    public void onResponseFail() {
        handlerFailPage();
        refreshMenuView.onRefreshComplete();
    }

    /**
     * 如果页面失败，页码减1
     */
    public void handlerFailPage() {
        if (page > 1) {
            page--;
        }
    }

    @UiThread
    protected void reFreshFinish(long count) {
        refreshMenuView.onRefreshComplete();
        if (count > 0) {
            refreshMenuView.setScrollLoadEnabled_(true);
        }
        if (count < 10) {
            refreshMenuView.setHasMoreData(false);
        }
    }
}
