package com.data.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.data.db.Friend;
import com.data.db.Message;
import com.data.db.User;
import com.data.ui.activity.ShowBigImage;
import com.data.util.MediaManager;
import com.data.util.image.SmartImageView;
import com.easemob.util.DateUtils;
import com.juns.wechat.R;
import com.juns.wechat.chat.utils.SmileUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class MessageAdapter extends BaseAdapter {

    private final static String TAG = "msg";

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 3;
    private static final int MESSAGE_TYPE_SENT_VOICE = 4;
    private static final int MESSAGE_TYPE_RECV_VOICE = 5;
    private static final int MESSAGE_TYPE_SENT_FILE = 6;
    private static final int MESSAGE_TYPE_RECV_FILE = 7;


    private static final int TXT = 1;
    private static final int FILE = 3;
    private static final int IMAGE = 2;
    private static final int VOICE = 4;
    private static final int OTHER = 100;
    public static final String IMAGE_DIR = "chat/image/";
    public static final String VOICE_DIR = "chat/audio/";
    public static final String VIDEO_DIR = "chat/video";

    private String username;
    private LayoutInflater inflater;
    private Activity activity;

    // reference to conversation object in chatsdk

    private Context context;

    private Map<String, Timer> timers = new Hashtable<String, Timer>();
    private List<Message> conversation;

    private Friend friend;
    private User user;
    private Map<Long,Friend> map;

    public MessageAdapter(Context context, String username, List<Message> conversation, Friend friend) {
        this.username = username;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (Activity) context;
        this.conversation = conversation;
        this.friend = friend;
        user = DataSupport.findFirst(User.class);
        map = new HashMap<>();
        if(friend!=null&&friend.getFriendId()<0){
            JSONArray array = JSONObject.parseArray(friend.getMembers());
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = JSONObject.parseObject(array.get(i).toString());
                Friend friend1 = new Friend();
                friend1.setPhoto(object.getString("photo"));
                friend1.setName(object.getString("name"));
                friend1.setFriendId(object.getLong("id"));
                map.put(friend1.getFriendId(),friend1);
            }
        }
    }

    // public void setUser(String user) {
    // this.user = user;
    // }

    /**
     * 获取item数
     */
    public int getCount() {
        return conversation.size();
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        notifyDataSetChanged();
    }

    public Message getItem(int position) {
        return conversation.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getViewTypeCount() {
        return 16;
    }

    public int getItemViewType(int position) {
        Message message = conversation.get(position);
        if (message.getType() == TXT) {
            return message.getDirect() == 1 ? MESSAGE_TYPE_RECV_TXT
                    : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getType() == IMAGE) {
            return message.getDirect() == 1 ? MESSAGE_TYPE_RECV_IMAGE
                    : MESSAGE_TYPE_SENT_IMAGE;

        }

        if (message.getType() == VOICE) {
            return message.getDirect() == 1 ? MESSAGE_TYPE_RECV_VOICE
                    : MESSAGE_TYPE_SENT_VOICE;
        }

        if (message.getType() == FILE) {
            return message.getDirect() == 1 ? MESSAGE_TYPE_RECV_FILE
                    : MESSAGE_TYPE_SENT_FILE;
        }

        if (message.getDirect() == 1) {
            return MESSAGE_TYPE_RECV_TXT;
        } else {
            return MESSAGE_TYPE_SENT_TXT;
        }
    }

    private View createViewByMessage(Message message, int position) {
        switch (message.getType()) {
            case IMAGE:
                return message.getDirect() == 1 ? inflater
                        .inflate(R.layout.row_received_picture, null) : inflater
                        .inflate(R.layout.row_sent_picture, null);

            case VOICE:
                return message.getDirect() == 1 ? inflater
                        .inflate(R.layout.row_received_voice, null) : inflater
                        .inflate(R.layout.row_sent_voice, null);
            case FILE:
                return message.getDirect() == 1 ? inflater
                        .inflate(R.layout.row_received_file, null) : inflater
                        .inflate(R.layout.row_sent_file, null);
            default:
                return message.getDirect() == 1 ? inflater
                        .inflate(R.layout.row_received_message, null) : inflater
                        .inflate(R.layout.row_sent_message, null);
        }
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Message message = getItem(position);
        int type = getItemViewType(position);
        ViewHolderRecvTxt viewHolderRecvTxt;
        ViewHolderSendTxt viewHolderSendTxt;
        ViewHolderRecvImage viewHolderRecvImage;
        ViewHolderSendImage viewHolderSendImage;
        ViewHolderRecvFile viewHolderRecvFile;
        ViewHolderSendFile viewHolderSendFile;
        ViewHolderRecvVoice viewHolderRecvVoice;
        ViewHolderSendVoice viewHolderSendVoice;
        if (convertView == null) {
            switch (type) {
                case MESSAGE_TYPE_RECV_TXT:
                    convertView = inflater.inflate(R.layout.row_received_message, null);
                    viewHolderRecvTxt = new ViewHolderRecvTxt();
                    viewHolderRecvTxt.tv = (TextView) convertView
                            .findViewById(R.id.tv_chatcontent);
                    viewHolderRecvTxt.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    viewHolderRecvTxt.head_iv = (SmartImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    handleTextMessageRecv(message, viewHolderRecvTxt);
                    convertView.setTag(viewHolderRecvTxt);
                    setRecvName(viewHolderRecvTxt,message);
                    break;
                case MESSAGE_TYPE_SENT_TXT:
                    convertView = inflater.inflate(R.layout.row_sent_message, null);
                    viewHolderSendTxt = new ViewHolderSendTxt();
                    viewHolderSendTxt.tv = (TextView) convertView
                            .findViewById(R.id.tv_chatcontent);
                    viewHolderSendTxt.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    viewHolderSendTxt.head_iv = (SmartImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    handleTextMessageSent(message, viewHolderSendTxt);
                    convertView.setTag(viewHolderSendTxt);
                    setSendName(viewHolderSendTxt);
                    break;
                case MESSAGE_TYPE_RECV_IMAGE:
                    convertView = inflater.inflate(R.layout.row_received_picture, null);
                    viewHolderRecvImage = new ViewHolderRecvImage();
                    viewHolderRecvImage.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    viewHolderRecvImage.head_iv = (SmartImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    viewHolderRecvImage.siv = ((SmartImageView) convertView
                            .findViewById(R.id.iv_sendPicture));
                    handleImageMessageRecv(message, viewHolderRecvImage);
                    convertView.setTag(viewHolderRecvImage);
                    setRecvName(viewHolderRecvImage,message);
                    break;
                case MESSAGE_TYPE_SENT_IMAGE:
                    convertView = inflater.inflate(R.layout.row_sent_picture, null);
                    viewHolderSendImage = new ViewHolderSendImage();
                    viewHolderSendImage.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    viewHolderSendImage.head_iv = (SmartImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    viewHolderSendImage.siv = ((SmartImageView) convertView
                            .findViewById(R.id.iv_sendPicture));
                    handleImageMessageSent(message, viewHolderSendImage);
                    convertView.setTag(viewHolderSendImage);
                    setSendName(viewHolderSendImage);
                    break;
                case MESSAGE_TYPE_RECV_FILE:
                    convertView = inflater.inflate(R.layout.row_received_file, null);
                    viewHolderRecvFile = new ViewHolderRecvFile();
                    viewHolderRecvFile.tv_file_name = (TextView) convertView
                            .findViewById(R.id.tv_file_name);
                    viewHolderRecvFile.tv_file_size = (TextView) convertView
                            .findViewById(R.id.tv_file_size);
                    viewHolderRecvFile.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    viewHolderRecvFile.head_iv = (SmartImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    handleFileMessageRecv(message, viewHolderRecvFile);
                    convertView.setTag(viewHolderRecvFile);
                    setRecvName(viewHolderRecvFile,message);
                    break;
                case MESSAGE_TYPE_SENT_FILE:
                    convertView = inflater.inflate(R.layout.row_sent_file, null);
                    viewHolderSendFile = new ViewHolderSendFile();
                    viewHolderSendFile.tv_file_name = (TextView) convertView
                            .findViewById(R.id.tv_file_name);
                    viewHolderSendFile.tv_file_size = (TextView) convertView
                            .findViewById(R.id.tv_file_size);
                    viewHolderSendFile.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    viewHolderSendFile.head_iv = (SmartImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    handleFileMessageSent(message, viewHolderSendFile);
                    convertView.setTag(viewHolderSendFile);
                    setSendName(viewHolderSendFile);
                    break;
                case MESSAGE_TYPE_RECV_VOICE:
                    convertView = inflater.inflate(R.layout.row_received_voice, null);
                    viewHolderRecvVoice = new ViewHolderRecvVoice();
                    viewHolderRecvVoice.iv = ((ImageView) convertView
                            .findViewById(R.id.iv_voice));
                    viewHolderRecvVoice.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    viewHolderRecvVoice.head_iv = (SmartImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    viewHolderRecvVoice.tv_length = (TextView) convertView.findViewById(R.id.tv_length);
                    handleVoiceMessageRecv(message, viewHolderRecvVoice);
                    convertView.setTag(viewHolderRecvVoice);
                    setRecvName(viewHolderRecvVoice,message);
                    break;
                case MESSAGE_TYPE_SENT_VOICE:
                    convertView = inflater.inflate(R.layout.row_sent_voice, null);
                    viewHolderSendVoice = new ViewHolderSendVoice();
                    viewHolderSendVoice.iv = ((ImageView) convertView
                            .findViewById(R.id.iv_voice));
                    viewHolderSendVoice.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    viewHolderSendVoice.head_iv = (SmartImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    viewHolderSendVoice.tv_length = (TextView) convertView.findViewById(R.id.tv_length);
                    handleVoiceMessageSent(message, viewHolderSendVoice);
                    convertView.setTag(viewHolderSendVoice);
                    setSendName(viewHolderSendVoice);
                    break;
            }
        } else {
            switch (type) {
                case MESSAGE_TYPE_RECV_TXT:
                    viewHolderRecvTxt = (ViewHolderRecvTxt) convertView.getTag();
                    handleTextMessageRecv(message, viewHolderRecvTxt);
                    setRecvName(viewHolderRecvTxt,message);
                    break;
                case MESSAGE_TYPE_SENT_TXT:
                    viewHolderSendTxt = (ViewHolderSendTxt) convertView.getTag();
                    handleTextMessageSent(message, viewHolderSendTxt);
                    setSendName(viewHolderSendTxt);
                    break;
                case MESSAGE_TYPE_RECV_IMAGE:
                    viewHolderRecvImage = (ViewHolderRecvImage) convertView.getTag();
                    handleImageMessageRecv(message, viewHolderRecvImage);
                    setRecvName(viewHolderRecvImage,message);
                    break;
                case MESSAGE_TYPE_SENT_IMAGE:
                    viewHolderSendImage = (ViewHolderSendImage) convertView.getTag();
                    handleImageMessageSent(message, viewHolderSendImage);
                    setSendName(viewHolderSendImage);
                    break;
                case MESSAGE_TYPE_RECV_FILE:
                    viewHolderRecvFile = (ViewHolderRecvFile) convertView.getTag();
                    handleFileMessageRecv(message, viewHolderRecvFile);
                    setRecvName(viewHolderRecvFile,message);
                    break;
                case MESSAGE_TYPE_SENT_FILE:
                    viewHolderSendFile = (ViewHolderSendFile) convertView.getTag();
                    handleFileMessageSent(message, viewHolderSendFile);
                    setSendName(viewHolderSendFile);
                    break;
                case MESSAGE_TYPE_RECV_VOICE:
                    viewHolderRecvVoice = (ViewHolderRecvVoice) convertView.getTag();
                    handleVoiceMessageRecv(message, viewHolderRecvVoice);
                    setRecvName(viewHolderRecvVoice,message);
                    break;
                case MESSAGE_TYPE_SENT_VOICE:
                    viewHolderSendVoice = (ViewHolderSendVoice) convertView.getTag();
                    handleVoiceMessageSent(message, viewHolderSendVoice);
                    setSendName(viewHolderSendVoice);
                    break;
            }
        }

        TextView timestamp = (TextView) convertView
                .findViewById(R.id.timestamp);

        if (position == 0) {
            timestamp.setText(DateUtils.getTimestampString(new Date(message
                    .getMsg_date())));
            timestamp.setVisibility(View.VISIBLE);
        } else {
            // 两条消息时间离得如果稍长，显示时间
            if (DateUtils.isCloseEnough(message.getMsg_date(), conversation
                    .get(position - 1).getMsg_date())) {
                timestamp.setVisibility(View.GONE);
            } else {
                timestamp.setText(DateUtils.getTimestampString(new Date(message
                        .getMsg_date())));
                timestamp.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    private void setSendName(ViewHolder viewHolder) {
        if (user != null) {
            viewHolder.head_iv.setImageUrl(user.getPhoto());
        }
    }

    private void setRecvName(ViewHolder viewHolder,Message message) {
        if(map.size()>0){
            if(map.containsKey(message.getSender_id())){
                Friend friend = map.get(message.getSender_id());
                viewHolder.head_iv.setImageUrl(friend.getPhoto());
                viewHolder.tv_userId.setText(friend.getName());
            }
        }else{
            if (friend != null) {
                viewHolder.head_iv.setImageUrl(friend.getPhoto());
                viewHolder.tv_userId.setText(friend.getName());
            }
        }
    }

    private void handleTextMessageRecv(Message message, ViewHolderRecvTxt holder) {
        Spannable span = SmileUtils.getSmiledText(context, message.getText());
        // 设置内容
        holder.tv.setText(span, BufferType.SPANNABLE);
    }

    private void handleTextMessageSent(Message message, ViewHolderSendTxt holder) {
        Spannable span = SmileUtils.getSmiledText(context, message.getText());
        // 设置内容
        holder.tv.setText(span, BufferType.SPANNABLE);
    }

    private void handleImageMessageRecv(Message message,
                                        ViewHolderRecvImage holder) {
        try{
            final JSONObject jsonObject = JSONObject.parseObject(message.getText());
            if (jsonObject.get("url") != null) {
                holder.siv.setImageUrl(jsonObject.getString("url"));
                holder.siv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowBigImage.class);
                        intent.putExtra("image",jsonObject.getString("url"));
                        context.startActivity(intent);
                    }
                });
            }
        }catch (Exception e){

        }
    }

    private void handleImageMessageSent(Message message,
                                        ViewHolderSendImage holder) {
        try{
            final JSONObject jsonObject = JSONObject.parseObject(message.getText());
            if (jsonObject.get("url") != null) {
                holder.siv.setImageUrl(jsonObject.getString("url"));
                holder.siv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowBigImage.class);
                        intent.putExtra("image",jsonObject.getString("url"));
                        context.startActivity(intent);
                    }
                });
            }
        }catch (Exception e){

        }
    }


    private void handleVoiceMessageRecv(final Message message,
                                        final ViewHolderRecvVoice holder) {
        try {
            final JSONObject jsonObject = JSONObject.parseObject(message.getText());
            double other = 0;

            if (jsonObject.get("other") != null) {
                other = jsonObject.getDouble("other");
                holder.tv_length.setText(String.format("%.2f", other) + "\"");
            }
            holder.iv.setOnClickListener(new OnClickListener() {
                @SuppressLint("ResourceType")
                @Override
                public void onClick(View v) {
                    final AnimationDrawable voiceAnimation;
                    if (holder.iv != null) {
                        holder.iv.setImageResource(R.drawable.chatfrom_voice_playing);
                    }
                    holder.iv.setImageResource(R.drawable.voice_from_icon);
                    voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
                    voiceAnimation.start();

                    String url = "";
                    if (jsonObject.get("url") != null) {
                        url = jsonObject.getString("url");
                    }

                    MediaManager.playSound(url, new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            holder.iv.setImageResource(R.drawable.chatfrom_voice_playing);

                            voiceAnimation.stop();
                        }
                    });
                }
            });
        }catch (Exception e){

        }
    }

    private void handleVoiceMessageSent(final Message message,
                                        final ViewHolderSendVoice holder) {
        try {
            final JSONObject jsonObject = JSONObject.parseObject(message.getText());
            double other = 0;

            if (jsonObject.get("other") != null) {
                other = jsonObject.getDouble("other");
                holder.tv_length.setText(String.format("%.2f", other) + "\"");
            }
            holder.iv.setOnClickListener(new OnClickListener() {
                @SuppressLint("ResourceType")
                @Override
                public void onClick(View v) {
                    final AnimationDrawable voiceAnimation;
                    if (holder.iv != null) {
                        holder.iv.setImageResource(R.drawable.chatto_voice_playing);
                    }
                    holder.iv.setImageResource(R.drawable.voice_to_icon);
                    voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
                    voiceAnimation.start();

                    String url = "";
                    if (jsonObject.get("url") != null) {
                        url = jsonObject.getString("url");
                    }
                    MediaManager.playSound(url, new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            holder.iv.setImageResource(R.drawable.chatto_voice_playing);
                            voiceAnimation.stop();
                        }
                    });
                }
            });
        } catch (Exception e) {

        }

    }

    private void handleFileMessageRecv(final Message message,
                                       final ViewHolderRecvFile holder) {

        final String filePath = message.getFileUrl();

    }

    private void handleFileMessageSent(final Message message,
                                       final ViewHolderSendFile holder) {

        final String filePath = message.getFileUrl();

    }


    public class ViewHolderSendTxt extends ViewHolder {
        TextView tv;
    }

    public class ViewHolderRecvTxt extends ViewHolder {
        TextView tv;
    }

    public class ViewHolderSendImage extends ViewHolder {
        SmartImageView siv;
    }

    public class ViewHolderRecvImage extends ViewHolder {
        SmartImageView siv;
    }

    public class ViewHolderSendFile extends ViewHolder {
        TextView tv_file_name;
        TextView tv_file_size;
    }

    public class ViewHolderRecvFile extends ViewHolder {
        TextView tv_file_name;
        TextView tv_file_size;
    }

    public class ViewHolderSendVoice extends ViewHolder {
        ImageView iv;
        TextView tv_length;
    }

    public class ViewHolderRecvVoice extends ViewHolder {
        ImageView iv;
        TextView tv_length;
    }

    public class ViewHolder {
        SmartImageView head_iv;
        TextView tv_userId;
    }


}