package com.data.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.data.data.AddFriend;
import com.data.data.GlobObject;
import com.data.db.Friend;
import com.data.ui.activity.friend.AddFriendListActivity;
import com.data.ui.activity.friend.FriendMsgActivity;
import com.data.ui.activity.friend.GroupListActivity;
import com.data.ui.adapter.ContactAdapter;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;
import com.juns.wechat.view.activity.PublishUserListActivity;
import com.data.ui.activity.SearchActivity;
import com.juns.wechat.widght.SideBar;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

//通讯录

public class Fragment_Friends extends Fragment implements OnClickListener,
        OnItemClickListener {
    private Activity ctx;
    private View layout, layout_head;
    private ListView lvContact;
    private SideBar indexBar;
    private TextView mDialogText;
    private WindowManager mWindowManager;
    private List<Friend> friends;
    private List<Friend> addFriends;
    private ContactAdapter adapter;
    private TextView add_friend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        if (layout == null) {
            ctx = this.getActivity();
            layout = ctx.getLayoutInflater().inflate(R.layout.fragment_friends,
                    null);
            mWindowManager = (WindowManager) ctx
                    .getSystemService(Context.WINDOW_SERVICE);
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
        lvContact = (ListView) layout.findViewById(R.id.lvContact);

        mDialogText = (TextView) LayoutInflater.from(getActivity()).inflate(
                R.layout.list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);
        indexBar = (SideBar) layout.findViewById(R.id.sideBar);
        indexBar.setListView(lvContact);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mWindowManager.addView(mDialogText, lp);
        indexBar.setTextView(mDialogText);
        layout_head = ctx.getLayoutInflater().inflate(
                R.layout.layout_head_friend, null);
        add_friend = (TextView)layout_head.findViewById(R.id.tv_addfriend);
        lvContact.addHeaderView(layout_head);

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mWindowManager.removeView(mDialogText);
        super.onDestroy();
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        initData();
    }

    private void initData() {
        friends = new ArrayList<>();
        addFriends = new ArrayList<>();
        for (int i = 0; i < GlobObject.friendList.size(); i++) {
            if (GlobObject.friendList.get(i).getType() == 1) {
                friends.add(GlobObject.friendList.get(i));
            } else if (GlobObject.friendList.get(i).getType() == 3) {
                addFriends.add(GlobObject.friendList.get(i));
            }
        }
        adapter = new ContactAdapter(getActivity(),
                friends);
        lvContact.setAdapter(adapter);
        if(addFriends.size()>0){
            add_friend.setVisibility(View.VISIBLE);
            add_friend.setText("+"+addFriends.size());
        }
    }

    private void setOnListener() {
        lvContact.setOnItemClickListener(this);
        layout_head.findViewById(R.id.layout_addfriend)
                .setOnClickListener(this);
        layout_head.findViewById(R.id.layout_search).setOnClickListener(this);
        layout_head.findViewById(R.id.layout_group).setOnClickListener(this);
        layout_head.findViewById(R.id.layout_public).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_search:// 搜索好友及公众号
                Utils.start_Activity(getActivity(), SearchActivity.class,new BasicNameValuePair(Constants.TYPE, "0"));
                break;
            case R.id.layout_addfriend:// 添加好友
                add_friend.setVisibility(View.GONE);
                Utils.start_Activity(getActivity(), AddFriendListActivity.class);
                break;
            case R.id.layout_group:// 群聊
                Utils.start_Activity(getActivity(), GroupListActivity.class);
                break;
            case R.id.layout_public:// 公众号
                Utils.start_Activity(getActivity(), PublishUserListActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Log.e("abc",arg2+"");
        Friend user = friends.get(arg2 - 1);
        if (user != null) {
            Intent intent = new Intent(getActivity(), FriendMsgActivity.class);
            intent.putExtra(Constants.User_ID, user.getFriendId());
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(R.anim.push_left_in,
                    R.anim.push_left_out);
        }

    }

    public void onEventMainThread(AddFriend friend) {
        friends.clear();
        addFriends.clear();
        for (int i = 0; i < GlobObject.friendList.size(); i++) {
            if (GlobObject.friendList.get(i).getType() == 1) {
                friends.add(GlobObject.friendList.get(i));
            } else if (GlobObject.friendList.get(i).getType() == 3) {
                addFriends.add(GlobObject.friendList.get(i));
            }
        }
        adapter.notifyDataSetChanged();
        if(addFriends.size()>0){
            add_friend.setVisibility(View.VISIBLE);

            add_friend.setText("+"+addFriends.size());
        }else{
            add_friend.setVisibility(View.GONE);
        }
    }
}
