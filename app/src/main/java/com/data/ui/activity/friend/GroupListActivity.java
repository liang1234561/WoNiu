package com.data.ui.activity.friend;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.data.data.AddFriend;
import com.data.data.GlobObject;
import com.data.db.Friend;
import com.data.ui.activity.BaseActivity;
import com.data.ui.activity.SearchActivity;
import com.data.ui.adapter.MyGroupAdpter;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

//群聊列表
public class GroupListActivity extends BaseActivity implements OnClickListener {
	private TextView txt_title;
	private ImageView img_back, img_right;
	private ListView mlistview;
	private List<Friend> friends;
	private MyGroupAdpter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_listview);
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void initControl() {
		txt_title = (TextView) findViewById(R.id.txt_title);
		txt_title.setText("群聊");
		img_back = (ImageView) findViewById(R.id.img_back);
		img_back.setVisibility(View.VISIBLE);
		img_right = (ImageView) findViewById(R.id.img_right);
		img_right.setImageResource(R.drawable.icon_add);
//		img_right.setVisibility(View.VISIBLE);
		mlistview = (ListView) findViewById(R.id.listview);
//		View layout_head = getLayoutInflater().inflate(
//				R.layout.layout_head_search, null);
//		mlistview.addHeaderView(layout_head);
	}

	@Override
	protected void initView() {
		try {
			friends = new ArrayList<>();

			for (int i = 0; i < GlobObject.friendList.size(); i++) {
				if (GlobObject.friendList.get(i).getType() == 2) {
					friends.add(GlobObject.friendList.get(i));
				}
			}

			if (friends != null && friends.size() > 0) {
				adapter = new MyGroupAdpter(this, friends);
				mlistview.setAdapter(adapter);
			} else {
				TextView txt_nodata = (TextView) findViewById(R.id.txt_nochat);
				txt_nodata.setText("暂时没有群聊");
				txt_nodata.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initData() {

	}

	@Override
	protected void setListener() {
		img_back.setOnClickListener(this);
		img_right.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_back:
			Utils.finish(GroupListActivity.this);
			break;
		case R.id.img_right:
			Utils.start_Activity(GroupListActivity.this,
					SearchActivity.class,new BasicNameValuePair(Constants.TYPE, "1"));
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onEventMainThread(AddFriend friend){
		friends.clear();
		for (int i = 0; i < GlobObject.friendList.size(); i++) {
			if (GlobObject.friendList.get(i).getType() == 2) {
				friends.add(GlobObject.friendList.get(i));
			}
		}
		adapter.notifyDataSetChanged();
	}
}
