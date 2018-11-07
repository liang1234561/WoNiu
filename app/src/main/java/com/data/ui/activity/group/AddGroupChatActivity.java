package com.data.ui.activity.group;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.data.data.GlobObject;
import com.data.db.Friend;
import com.data.db.User;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.BaseActivity;
import com.data.ui.view.TT;
import com.data.util.image.SmartImageView;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.easemob.chat.EMGroup;
import com.easemob.util.DensityUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.chat.ChatActivity;
import com.juns.wechat.common.PingYinUtil;
import com.juns.wechat.common.PinyinComparator;
import com.juns.wechat.common.Utils;
import com.juns.wechat.common.ViewHolder;
import com.juns.wechat.widght.SideBar;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddGroupChatActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener {
	private ImageView iv_search, img_back;
	private TextView tv_header, txt_title, txt_right;;
	private ListView listView;
	private EditText et_search;
	private SideBar indexBar;
	private TextView mDialogText;
	private WindowManager mWindowManager;
	/** 是否为一个新建的群组 */
	protected boolean isCreatingNewGroup;
	/** 是否为单选 */
	private boolean isSignleChecked;
	private ContactAdapter contactAdapter;
	/** group中一开始就有的成员 */
	private List<Long> exitingMembers = new ArrayList<Long>();
	private List<Friend> alluserList;// 好友列表
	// 可滑动的显示选中用户的View
	private LinearLayout menuLinerLayout;

	// 选中用户总数,右上角显示
	int total = 0;
	private String userId = null;
	private String groupId = null;
	private String groupname;
	// 添加的列表
	private List<Long> addList = new ArrayList<Long>();
	private String hxid;
	private EMGroup group;
	private String id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_chatroom);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWindowManager.removeView(mDialogText);
	}

	@Override
	protected void initControl() {
		txt_title = (TextView) findViewById(R.id.txt_title);
		txt_title.setText("添加群友");
		txt_right = (TextView) this.findViewById(R.id.txt_right);
		txt_right.setText("确定");
		txt_right.setTextColor(0xFF45C01A);
		img_back = (ImageView) findViewById(R.id.img_back);
		img_back.setVisibility(View.VISIBLE);
		menuLinerLayout = (LinearLayout) this
				.findViewById(R.id.linearLayoutMenu);
		et_search = (EditText) this.findViewById(R.id.et_search);
		listView = (ListView) findViewById(R.id.list);
		iv_search = (ImageView) this.findViewById(R.id.iv_search);
		mDialogText = (TextView) LayoutInflater.from(this).inflate(
				R.layout.list_position, null);
		mDialogText.setVisibility(View.INVISIBLE);
		indexBar = (SideBar) findViewById(R.id.sideBar);
		indexBar.setListView(listView);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
						| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		mWindowManager.addView(mDialogText, lp);
		indexBar.setTextView(mDialogText);
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View headerView = layoutInflater.inflate(R.layout.item_chatroom_header,
				null);
		tv_header = (TextView) headerView.findViewById(R.id.tv_header);
		listView.addHeaderView(headerView);
		listView.setOnItemClickListener(this);
	}

	@Override
	protected void initView() {

	}

	@Override
	protected void initData() {
		alluserList = new ArrayList<Friend>();

		Map<Long,String> map = new HashMap<>();
		id = getIntent().getStringExtra("id");
		List<Friend> friends = DataSupport.where("friendId = ?", id).find(Friend.class);
		if(friends.size()>0){
			Friend friend = friends.get(0);
			JSONArray array = JSONObject.parseArray(friend.getMembers());
			for (int i = 0; i < array.size(); i++) {
				JSONObject object = JSONObject.parseObject(array.get(i).toString());
                map.put(object.getLong("id"),"");
			}
		}

        for (int i = 0; i < GlobObject.friendList.size(); i++) {
            if (GlobObject.friendList.get(i).getType() == 1) {
                if(!map.containsKey(GlobObject.friendList.get(i).getFriendId())){
                    alluserList.add(GlobObject.friendList.get(i));
                }
            }
        }

		contactAdapter = new ContactAdapter(AddGroupChatActivity.this,
				alluserList);
		listView.setAdapter(contactAdapter);
	}

	@Override
	protected void setListener() {
		img_back.setOnClickListener(this);
		tv_header.setOnClickListener(this);
		txt_right.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_back:
			Utils.finish(AddGroupChatActivity.this);
			break;
		case R.id.tv_header:
			// TODO 打开群列表
			break;
		case R.id.txt_right:// 确定按钮
			save();
			break;
		default:
			break;
		}
	}

	/**
	 * 确认选择的members
	 * 
	 */
	public void save() {
		if (addList.size() == 0) {
			Utils.showLongToast(AddGroupChatActivity.this, "请选择用户");
			return;
		}
		// 如果只有一个用户说明只是单聊,并且不是从群组加人
		if (addList.size() == 1 && isCreatingNewGroup) {
			Long userId = addList.get(0);
			Intent intent = new Intent(AddGroupChatActivity.this,
					ChatActivity.class);
			intent.putExtra(Constants.User_ID, userId);
			startActivity(intent);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		} else {
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					creatNewGroup(addList);// 创建群组
				}
			});
		}
	}

	class ContactAdapter extends BaseAdapter implements SectionIndexer {
		private Context mContext;
		private boolean[] isCheckedArray;
		private Bitmap[] bitmaps;
		private List<Friend> list = new ArrayList<Friend>();

		@SuppressWarnings("unchecked")
		public ContactAdapter(Context mContext, List<Friend> users) {
			this.mContext = mContext;
			this.list = users;
			bitmaps = new Bitmap[list.size()];
			isCheckedArray = new boolean[list.size()];
			// 排序(实现了中英文混排)
			Collections.sort(list, new PinyinComparator());
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final Friend user = list.get(position);
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.contact_item, null);
			}
			SmartImageView ivAvatar = ViewHolder.get(convertView,
					R.id.contactitem_avatar_iv);
			TextView tvCatalog = ViewHolder.get(convertView,
					R.id.contactitem_catalog);
			TextView tvNick = ViewHolder
					.get(convertView, R.id.contactitem_nick);
			final CheckBox checkBox = ViewHolder
					.get(convertView, R.id.checkbox);
			checkBox.setVisibility(View.VISIBLE);
			String catalog = PingYinUtil.converterToFirstSpell(
					user.getName()).substring(0, 1);
			if (position == 0) {
				tvCatalog.setVisibility(View.VISIBLE);
				tvCatalog.setText(catalog);
			} else {
				Friend Nextuser = list.get(position - 1);
				String lastCatalog = PingYinUtil.converterToFirstSpell(
						Nextuser.getName()).substring(0, 1);
				if (catalog.equals(lastCatalog)) {
					tvCatalog.setVisibility(View.GONE);
				} else {
					tvCatalog.setVisibility(View.VISIBLE);
					tvCatalog.setText(catalog);
				}
			}
			ivAvatar.setImageUrl(user.getPhoto());
			tvNick.setText(user.getName());
			if (exitingMembers != null
					&& exitingMembers.contains(user.getFriendId())) {
				checkBox.setChecked(true);
			} else {
				checkBox.setChecked(false);
			}
			if (addList != null && addList.contains(user.getFriendId())) {
				checkBox.setChecked(true);
				isCheckedArray[position] = true;
			}
			if (checkBox != null) {
				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// 群组中原来的成员一直设为选中状态
						if (exitingMembers.contains(user.getFriendId())) {
							isChecked = true;
							checkBox.setChecked(true);
						}
						isCheckedArray[position] = isChecked;
						// 如果是单选模式
						if (isSignleChecked && isChecked) {
							for (int i = 0; i < isCheckedArray.length; i++) {
								if (i != position) {
									isCheckedArray[i] = false;
								}
							}
							contactAdapter.notifyDataSetChanged();
						}

						if (isChecked) {
							// 选中用户显示在滑动栏显示
							showCheckImage(null, list.get(position));
						} else {
							// 用户显示在滑动栏删除
							deleteImage(list.get(position));
						}
					}
				});
				// 群组中原来的成员一直设为选中状态
				if (exitingMembers.contains(user.getFriendId())) {
					checkBox.setChecked(true);
					isCheckedArray[position] = true;
				} else {
					checkBox.setChecked(isCheckedArray[position]);
				}

			}
			return convertView;
		}

		@Override
		public int getPositionForSection(int section) {
			for (int i = 0; i < list.size(); i++) {
				Friend user = list.get(i);
				String l = PingYinUtil
						.converterToFirstSpell(user.getName()).substring(0,
								1);
				char firstChar = l.toUpperCase().charAt(0);
				if (firstChar == section) {
					return i;
				}
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			return null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
		checkBox.toggle();
	}

	// 即时显示被选中用户的头像和昵称。
	private void showCheckImage(Bitmap bitmap, Friend glufineid) {
		if (exitingMembers.contains(glufineid.getName()) && groupId != null) {
			return;
		}
		if (addList.contains(glufineid.getFriendId())) {
			return;
		}
		total++;

		final ImageView imageView = new ImageView(this);
		LayoutParams lp = new LayoutParams(
				DensityUtil.dip2px(this, 40), DensityUtil.dip2px(this, 40));
		lp.setMargins(0, 0, DensityUtil.dip2px(this, 5), 0);
		imageView.setLayoutParams(lp);

		// 设置id，方便后面删除
		imageView.setTag(glufineid);
		if (bitmap == null) {
			imageView.setImageResource(R.drawable.head);
		} else {
			imageView.setImageBitmap(bitmap);
		}

		menuLinerLayout.addView(imageView);
		txt_right.setText("确定(" + total + ")");
		if (total > 0) {
			if (iv_search.getVisibility() == View.VISIBLE) {
				iv_search.setVisibility(View.GONE);
			}
		}
		addList.add(glufineid.getFriendId());
	}

	private void deleteImage(Friend glufineid) {
		View view = (View) menuLinerLayout.findViewWithTag(glufineid);

		menuLinerLayout.removeView(view);
		total--;
		txt_right.setText("确定(" + total + ")");
		addList.remove(glufineid.getFriendId());
		if (total < 1) {
			if (iv_search.getVisibility() == View.GONE) {
				iv_search.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 创建新群组
	 * 
	 * @param newmembers
	 */
	String groupName = "";
	String manber = "";

	private void creatNewGroup(List<Long> members) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id",Long.parseLong(id));
		jsonObject.put("type","add");
		jsonObject.put("members",members);
		Log.e("abc ","发起群聊:"+jsonObject.toString());
		sendPipe("user_friend_service",jsonObject.toJSONString());
	}

	private void sendPipe(String code,String json){
		if (!App.isOnline()) {
			return;
		}
		RsProtocolContext protocolContext = RequestParamTools.getPipeRequest(code,json);
		if (protocolContext != null) {
			BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
				@Override
				protected void onSuccess(ProtocolContext protocol) {
					try {
						ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
						Log.e("abc","发起群聊 response"+response.toString());
						if(response.getErrorCode() == 0){
							JSONObject jsonObject = JSONObject.parseObject(response.getPipe().getResponse());
							if(jsonObject.getInteger("code")==0){
								finish();
							}else{
								TT.show(AddGroupChatActivity.this,""+jsonObject.getString("message"));
							}
						}else {
							TT.show(AddGroupChatActivity.this,""+response.getErrorMessage());
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
}
