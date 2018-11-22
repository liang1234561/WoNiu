package com.data.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.data.db.Friend;
import com.data.ui.activity.ChatActivity;
import com.data.util.image.SmartImageView;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.common.ViewHolder;

import java.util.List;

public class MyGroupAdpter extends BaseAdapter {
	protected Context context;
	private List<Friend> grouplist;

	public MyGroupAdpter(Context ctx, List<Friend> grouplist) {
		context = ctx;
		this.grouplist = grouplist;
	}

	@Override
	public int getCount() {
		return grouplist.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.layout_item_mygroup, parent, false);
		}
		final Friend group = grouplist.get(position);
		SmartImageView img_avar = ViewHolder.get(convertView, R.id.img_photo);
		TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
		img_avar.setImageUrl(group.getPhoto());
		txt_name.setText(group.getName());
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ChatActivity.class);
				intent.putExtra(Constants.NAME, group.getName());
				intent.putExtra(Constants.TYPE, ChatActivity.CHATTYPE_GROUP);
				intent.putExtra(Constants.User_ID, group.getFriendId());
				context.startActivity(intent);
			}
		});
		return convertView;
	}
}
