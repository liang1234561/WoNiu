package com.data.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.data.data.UpdataGroupUser;
import com.data.db.Friend;
import com.data.db.User;
import com.data.ui.activity.group.AddGroupChatActivity;
import com.data.ui.activity.group.ExitGroupAcyivity;
import com.data.ui.activity.group.RemoveGroupChatActivity;
import com.data.ui.activity.set.UpdataGroupNameActivity;
import com.data.util.image.SmartImageView;
import com.easemob.chat.EMGroup;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.chat.widght.ExpandGridView;
import com.juns.wechat.common.Utils;
import com.juns.wechat.view.BaseActivity;

import org.apache.http.message.BasicNameValuePair;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

//群设置
public class GroupSettingActivity extends BaseActivity implements
        OnClickListener {
    private ImageView img_back;
    private TextView tv_groupname;
    private TextView txt_title;// 标题，成员总数
    int m_total = 0;// 成员总数
    private ExpandGridView gridview;// 成员列表
    // 修改群名称、置顶、、、、
    private RelativeLayout re_change_groupname;
    private RelativeLayout rl_switch_chattotop;
    private RelativeLayout rl_switch_block_groupmsg;
    private RelativeLayout re_clear;

    // 状态变化
    private CheckBox check_top, check_closetip;
    // 删除并退出

    private Button exitBtn;
    private String hxid;
    private String group_name;// 群名称
    boolean is_admin = false;// 是否是管理员
    List<Friend> members = new ArrayList<Friend>();
    String longClickUsername = null;

    private String groupId;
    private EMGroup group;
    private GridAdapter adapter;
    private String id;
    private Friend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_groupsetting);
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("群友设置");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        tv_groupname = (TextView) findViewById(R.id.txt_groupname);
        gridview = (ExpandGridView) findViewById(R.id.gridview);

        re_change_groupname = (RelativeLayout) findViewById(R.id.re_change_groupname);
        rl_switch_chattotop = (RelativeLayout) findViewById(R.id.rl_switch_chattotop);
        rl_switch_block_groupmsg = (RelativeLayout) findViewById(R.id.rl_switch_block_groupmsg);
        re_clear = (RelativeLayout) findViewById(R.id.re_clear);

        exitBtn = (Button) findViewById(R.id.btn_exit_grp);
    }

    @Override
    protected void initView() {
        hxid = Utils.getValue(GroupSettingActivity.this, Constants.User_ID);
    }

    @Override
    protected void initData() {
        id = getIntent().getStringExtra("id");
        List<Friend> friends = DataSupport.where("friendId = ?", id).find(Friend.class);
        if (friends.size() > 0) {
            friend = friends.get(0);
            JSONArray array = JSONObject.parseArray(friend.getMembers());
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = JSONObject.parseObject(array.get(i).toString());
                Friend friend1 = new Friend();
                friend1.setPhoto(object.getString("photo"));
                friend1.setName(object.getString("name"));
                friend1.setFriendId(object.getLong("id"));
                members.add(friend1);
            }
            tv_groupname.setText(friend.getName());

            // 显示群组成员头像和昵称
            showMembers(members);
            User user = DataSupport.findFirst(User.class);


            // 判断是否是群主，是群主有删成员的权限，并显示减号按钮
            if (null != user && friend.getManger_ids() != null && friend.getManger_ids().contains(String.valueOf(user.getUser_id()))) {
                is_admin = true;
            }
        }


    }

    // 显示群成员头像昵称的gridview
    private void showMembers(List<Friend> members) {
        adapter = new GridAdapter(this, members);
        gridview.setAdapter(adapter);

        // 设置OnTouchListener,为了让群主方便地推出删除模》
        gridview.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (adapter.isInDeleteMode) {
                            adapter.isInDeleteMode = false;
                            adapter.notifyDataSetChanged();
                            return true;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void setListener() {
        re_change_groupname.setOnClickListener(this);
        rl_switch_chattotop.setOnClickListener(this);
        rl_switch_block_groupmsg.setOnClickListener(this);
        re_clear.setOnClickListener(this);
        exitBtn.setOnClickListener(this);
        img_back.setOnClickListener(this);
        tv_groupname.setOnClickListener(this);
    }

    public void onEventMainThread(UpdataGroupUser user){
        if(user != null){
            tv_groupname.setText(user.getName());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Utils.finish(GroupSettingActivity.this);
                break;
            case R.id.btn_exit_grp:
                Utils.start_Activity(GroupSettingActivity.this,
                        ExitGroupAcyivity.class, new BasicNameValuePair("id", "" + id));
                break;
            case R.id.re_change_groupname:
                Utils.start_Activity(GroupSettingActivity.this,
                        UpdataGroupNameActivity.class, new BasicNameValuePair("id", "" + id), new BasicNameValuePair("name", friend.getName()));
                break;
            default:
                break;
        }
    }

    // 群组成员gridadapter
    private class GridAdapter extends BaseAdapter {

        public boolean isInDeleteMode;
        private List<Friend> objects;
        Context context;

        public GridAdapter(Context context, List<Friend> objects) {

            this.objects = objects;
            this.context = context;
            isInDeleteMode = false;
        }

        @Override
        public View getView(final int position, View convertView,
                            final ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.item_chatsetting_gridview, null);
            }
            SmartImageView iv_avatar = (SmartImageView) convertView
                    .findViewById(R.id.iv_avatar);
            TextView tv_username = (TextView) convertView
                    .findViewById(R.id.tv_username);
            ImageView badge_delete = (ImageView) convertView
                    .findViewById(R.id.badge_delete);

            // 最后一个item，减人按钮
            if (position == getCount() - 1 && is_admin) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.icon_btn_deleteperson);

                if (isInDeleteMode) {
                    // 正处于删除模式下，隐藏删除按钮
                    convertView.setVisibility(View.GONE);
                } else {

                    convertView.setVisibility(View.VISIBLE);
                }

                iv_avatar.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Utils.start_Activity(GroupSettingActivity.this,
                                RemoveGroupChatActivity.class, new BasicNameValuePair("id", "" + id));
                    }
                });

            } else if ((is_admin && position == getCount() - 2)
                    || (!is_admin && position == getCount() - 1)) { // 添加群组成员按钮
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.jy_drltsz_btn_addperson);
                // 正处于删除模式下,隐藏添加按钮
                if (isInDeleteMode) {
                    convertView.setVisibility(View.GONE);
                } else {
                    convertView.setVisibility(View.VISIBLE);
                }
                iv_avatar.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 进入选人页面
                        Utils.start_Activity(GroupSettingActivity.this,
                                AddGroupChatActivity.class, new BasicNameValuePair("id", "" + id));
                    }
                });
            } else { // 普通item，显示群组成员
                final Friend user = objects.get(position);
                String usernick = user.getName();
                final String useravatar = user.getPhoto();
                tv_username.setText(usernick);
                iv_avatar.setImageUrl(user.getPhoto());// TODO 网络加载头像
                iv_avatar.setTag(useravatar);

            }
            return convertView;
        }

        @Override
        public int getCount() {
            if (is_admin) {
                return objects.size() + 2;
            } else {

                return objects.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return objects.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }
    }
}
