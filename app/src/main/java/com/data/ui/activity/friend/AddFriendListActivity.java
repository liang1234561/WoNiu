package com.data.ui.activity.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.data.data.GlobObject;
import com.data.db.Friend;
import com.data.ui.activity.BaseActivity;
import com.data.util.image.SmartImageView;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;
import com.juns.wechat.common.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class AddFriendListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private TextView txt_title;
    private ImageView img_back;
    private ListView addListView;
    private List<Friend> addFriends;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.addfriendlist);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("添加好友列表");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        addListView = (ListView) findViewById(R.id.lv_addFlist);
        addFriends = new ArrayList<>();
        for (int i = 0; i < GlobObject.friendList.size(); i++) {
            if (GlobObject.friendList.get(i).getType() == 3) {
                addFriends.add(GlobObject.friendList.get(i));
            }
        }
        addListView.setAdapter(new Myadapter(this,addFriends));
        addListView.setOnItemClickListener(this);
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
                Utils.finish(AddFriendListActivity.this);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, AddFriendAcyivity.class);
        Friend friend = addFriends.get(position);
        intent.putExtra("nickname",friend.getName());
        intent.putExtra("username",friend.getName());
        intent.putExtra("add",2);//2 是同意好友，1是加好友（加群），3是群同意进群
        intent.putExtra("id",friend.getFriendId());
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }

    class Myadapter extends BaseAdapter {

        private Context mContext;
        private List<Friend> UserInfos;// 好友信息

        public Myadapter(Context mContext, List<Friend> UserInfos) {
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
            Friend user = UserInfos.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.contact_item, null);
            }
            SmartImageView ivAvatar = ViewHolder.get(convertView,
                    R.id.contactitem_avatar_iv);
            TextView tvNick = ViewHolder.get(convertView, R.id.contactitem_nick);
            ivAvatar.setImageUrl(user.getPhoto());
            tvNick.setText(user.getName());
            return convertView;
        }



    }
}
