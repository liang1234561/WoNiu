package com.data.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.data.data.bean.MsgBean;
import com.data.util.image.SmartImageView;
import com.easemob.util.DateUtils;
import com.juns.wechat.R;
import com.juns.wechat.bean.PublicMsgInfo;
import com.juns.wechat.chat.utils.SmileUtils;
import com.juns.wechat.common.UserUtils;
import com.juns.wechat.common.ViewHolder;
import com.juns.wechat.dialog.WarnTipDialog;
import com.juns.wechat.net.NetClient;
import com.juns.wechat.widght.swipe.SwipeLayout;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class NewMsgAdpter extends BaseAdapter {
	protected Context context;
	private List<MsgBean> conversationList;
	private WarnTipDialog Tipdialog;

	private Hashtable<String, String> ChatRecord = new Hashtable<String, String>();
	public PublicMsgInfo PublicMsg = null;

	public NewMsgAdpter(Context ctx, List<MsgBean> objects) {
		context = ctx;
		conversationList = objects;

	}

	public void setPublicMsg(PublicMsgInfo Msg) {
		PublicMsg = Msg;
	}

	public PublicMsgInfo getPublicMsg() {
		return PublicMsg;
	}

	public Hashtable<String, String> getChatRecord() {
		return ChatRecord;
	}

	@Override
	public int getCount() {
		return conversationList.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.layout_item_msg, parent, false);
		}
		SmartImageView img_avar = ViewHolder.get(convertView,
				R.id.contactitem_avatar_iv);
		TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
		TextView txt_state = ViewHolder.get(convertView, R.id.txt_state);
		TextView txt_del = ViewHolder.get(convertView, R.id.txt_del);
		TextView txt_content = ViewHolder.get(convertView, R.id.txt_content);
		TextView txt_time = ViewHolder.get(convertView, R.id.txt_time);
		TextView unreadLabel = ViewHolder.get(convertView,
				R.id.unread_msg_number);
		SwipeLayout swipe = ViewHolder.get(convertView, R.id.swipe);
		swipe.setSwipeEnabled(false);
		// 获取与此用户/群组的会话
		MsgBean conversation = conversationList.get(position);
		Log.e("abc","conversation:"+conversation.toString());
		// 获取用户username或者群组groupid
		if(conversation.getFriend()== null || conversation.getMessage() == null){
			return convertView;
		}
		txt_name.setText(conversation.getFriend().getName());
		if (conversation.getFriend().getPhoto() == null) {
			img_avar.setImageResource(R.drawable.webdefault);
		}else{
			img_avar.setImageUrl(conversation.getFriend().getPhoto());
		}

		if(conversation.isIsnew()){
			unreadLabel.setVisibility(View.VISIBLE);
		}else{
			unreadLabel.setVisibility(View.INVISIBLE);
		}

		if (conversation.getMessage().getText() != null) {
			// 把最后一条消息的内容作为item的message内容
			txt_content.setText(
					SmileUtils.getSmiledText(context,
							getMessageDigest(conversation, context)),
					BufferType.SPANNABLE);
			txt_time.setText(DateUtils.getTimestampString(new Date(
					conversation.getMessage().getMsg_date())));
		}

		txt_del.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Tipdialog = new WarnTipDialog((Activity) context,
						"您确定要删除该聊天吗？");
				Tipdialog.setBtnOkLinstener(onclick);
				Tipdialog.show();
			}
		});
		return convertView;
	}

	private DialogInterface.OnClickListener onclick = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
//			EMConversation conversation = conversationList.get(deleteID);
//			EMChatManager.getInstance().deleteConversation(
//					conversation.getUserName());
//			// Utils.showLongToast((Activity) context, "删除成功");
//			conversationList.remove(deleteID);
//			notifyDataSetChanged();
//			Tipdialog.dismiss();
		}
	};

	/**
	 * 根据消息内容和消息类型获取消息内容提示
	 *
	 * @param message
	 * @param context
	 * @return
	 */
	private String getMessageDigest(MsgBean message, Context context) {
		String digest = "";
		switch (message.getMessage().getType()) {//1文本（包含emoji）2图片3文件4语音消息 100 通知类消息
			case 1: // 图片消息
				digest = message.getMessage().getText();
				break;
			case 2:// 语音消息
				digest = getStrng(context, R.string.picture);
				break;
			case 3: // 视频消息
				digest = getStrng(context, R.string.file);
				break;
			case 4: // 文本消息
				digest = getStrng(context, R.string.voice_msg);
				break;
			case 100: // 普通文件消息
				digest = getStrng(context, R.string.file);
				break;
			default:
				System.err.println("error, unknow type");
				return "";
		}
		return digest;
	}

	String getStrng(Context context, int resId) {
		return context.getResources().getString(resId);
	}
}
