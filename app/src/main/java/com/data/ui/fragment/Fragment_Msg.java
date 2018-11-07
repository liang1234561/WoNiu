package com.data.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.data.data.AddFriend;
import com.data.data.GlobObject;
import com.data.data.NewMessage;
import com.data.data.bean.MsgBean;
import com.data.db.Friend;
import com.data.db.Message;
import com.data.ui.activity.ChatActivity;
import com.data.ui.activity.MainActivity;
import com.data.ui.adapter.NewMsgAdpter;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.common.NetUtil;
import com.juns.wechat.common.Utils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

//消息
public class Fragment_Msg extends Fragment implements OnClickListener,
        OnItemClickListener {
    private Activity ctx;
    private View layout, layout_head;
    public RelativeLayout errorItem;
    public TextView errorText;
    private ListView lvContact;
    private NewMsgAdpter adpter;
    private List<MsgBean> conversationList = new ArrayList<MsgBean>();
    private MainActivity parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        if (layout == null) {
            ctx = this.getActivity();
            parentActivity = (MainActivity) getActivity();
            layout = ctx.getLayoutInflater().inflate(R.layout.fragment_msg,
                    null);
            lvContact = (ListView) layout.findViewById(R.id.listview);
            errorItem = (RelativeLayout) layout
                    .findViewById(R.id.rl_error_item);
            errorText = (TextView) errorItem
                    .findViewById(R.id.tv_connect_errormsg);
            setOnListener();
        } else {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        conversationList.clear();
        initViews();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        conversationList.clear();
        initViews();
    }

    private void initViews() {
        conversationList.addAll(loadMsgList());
        if (conversationList != null && conversationList.size() > 0) {
            layout.findViewById(R.id.txt_nochat).setVisibility(View.GONE);
            adpter = new NewMsgAdpter(getActivity(), conversationList);
            lvContact.setAdapter(adpter);
        } else {
            layout.findViewById(R.id.txt_nochat).setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取所有会话
     *
     * @return +
     */
    private List<MsgBean> loadMsgList() {
        List<MsgBean> list = new ArrayList<MsgBean>();
        // 过滤掉messages seize为0的conversation
        ConcurrentHashMap<Long, MsgBean> map = GlobObject.getMsgMap();
        for (Map.Entry<Long, MsgBean> entry : map.entrySet()) {
            if (entry.getValue() != null)
                list.add(entry.getValue());
        }
        // 排序
        sortConversationByLastChatTime(list);
        return list;
    }

    /**
     * 根据最后一条消息的时间排序
     */
    private void sortConversationByLastChatTime(List<MsgBean> conversationList) {
        Collections.sort(conversationList, new Comparator<MsgBean>() {
            @Override
            public int compare(final MsgBean con1,
                               final MsgBean con2) {

                if(con1.getMessage().getTop_view() == 1){
                    return -1;
                }
                if (con1.getMessage().getMsg_date() == con2.getMessage()
                        .getMsg_date()) {
                    return 0;
                } else if (con1.getMessage().getMsg_date() < con2.getMessage()
                        .getMsg_date()) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });
    }

    private void setOnListener() {
        lvContact.setOnItemClickListener(this);
        errorItem.setOnClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        Hashtable<String, String> ChatRecord = adpter.getChatRecord();
        MsgBean msgBean = conversationList.get(position);
        msgBean.setIsnew(false);
        intent.putExtra(Constants.TYPE, ChatActivity.CHATTYPE_SINGLE);
        intent.putExtra(Constants.User_ID,
                msgBean.getChat_id());
        getActivity().startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_error_item:
                NetUtil.openSetNetWork(getActivity());
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(NewMessage newMessage) {
        Log.e("abc", "消息队列更新");
        conversationList.clear();
        conversationList.addAll(loadMsgList());
        if (conversationList != null && conversationList.size() > 0) {
            adpter.notifyDataSetChanged();
        } else {
            layout.findViewById(R.id.txt_nochat).setVisibility(View.VISIBLE);
        }
    }

    public void onEventMainThread(AddFriend friend) {

        for (int i = 0; i < GlobObject.friendList.size(); i++) {
            Long  id = GlobObject.friendList.get(i).getFriendId();
            GlobObject.friendMap.put(id,GlobObject.friendList.get(i));
            Message messages = null;
            if(id>0){
                messages = DataSupport.where("chat_id = ? or sender_id = ?", id+"", id+"").findLast(Message.class);
            }else{
                messages = DataSupport.where("chat_id = ?", id+"").findLast(Message.class);
            }
            if(messages != null){
                MsgBean a = new MsgBean();
                a.setChat_id(id);
                a.setFriend(GlobObject.friendList.get(i));
                a.setMessage(messages);
                a.setIsnew(false);
                GlobObject.msgMap.put(id,a);
            }
        }
        Log.e("abc", "消息队列朋友更新");

        conversationList.clear();
        conversationList.addAll(loadMsgList());
        if (conversationList != null && conversationList.size() > 0) {
            adpter.notifyDataSetChanged();
        } else {
            layout.findViewById(R.id.txt_nochat).setVisibility(View.VISIBLE);
        }
    }
}
