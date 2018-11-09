package com.data.operation;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.data.data.AddFriend;
import com.data.data.GlobObject;
import com.data.data.NewMessage;
import com.data.data.UpdataUser;
import com.data.data.bean.MsgBean;
import com.data.db.Friend;
import com.data.db.Message;
import com.data.db.User;
import com.data.pbprotocol.ChatProtocol;
import com.data.pbprotocol.ChatProtocol.Response;
import com.data.ui.activity.MainActivity;
import com.data.util.MobileSystemUtil;
import com.data.util.net.NettyClient;
import com.data.util.net.OnServerConnectListener;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.R;

import org.litepal.crud.DataSupport;

import java.net.InetSocketAddress;
import java.util.List;

import de.greenrobot.event.EventBus;

public class BeanMessageHandler extends Handler {
    public static final int INIT = 1;//初始化
    public static final int STARTCONN = 2;
    public static final int STOPCONN = 3;
    public static final int HEAT = 4;
    public static final int FRIENDSYNC = 5;
    public static final int MESSAGESYSNC = 6;
    public static final int USERSYN = 7;

    private Context mContext;
    private BroadcastReceiver brReceiver;

    public static String HOST = "www.tytools.cn";
    public static int PORT = 9135;
    public static NettyClient appClient;
    public static long userId = 0;
    private NotificationManager notifyMgr;
    private int countNotification = 1;

    public BeanMessageHandler(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        userId =  PreferenceHelper.getLong("USERID",context);
        notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public synchronized boolean waitQuit() throws InterruptedException {
//        unregisterReceiver();
        wait();
        return true;
    }

    private void unregisterReceiver() {
        if (brReceiver != null) {
            mContext.unregisterReceiver(brReceiver);
            brReceiver = null;
        }
    }

    @Override
    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case INIT:
                init(mContext);
//                initReceiver();
                break;
            case STARTCONN:
                try {
                    if (appClient != null) {
                        appClient.connect();
                    } else {
                        getAppClient();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case STOPCONN:
                break;
            case HEAT:
                sendHeartBeat();
                break;
            case FRIENDSYNC:
                handleNewFriend();
                break;
            case MESSAGESYSNC:
                handleNewMessage();
                break;
            case USERSYN:
                handleNewFriend();
                sendEmptyMessageDelayed(MESSAGESYSNC,4000);
                getUserSyncRequest();
                break;
            default:
                break;
        }
        super.handleMessage(msg);
    }

    /**
     * 发送心跳
     */
    private void sendHeartBeat() {
        if (!App.isOnline()) {
            return;
        }
        removeMessages(HEAT);
        RsProtocolContext protocolContext = RequestParamTools.getHeartbeat(MobileSystemUtil.getVersion(mContext));
        if (protocolContext != null) {
            getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    handleHeatResponse(protocol);
                    super.onSuccess(protocol);
                }

                @Override
                protected void onFailed(int type) {

                }
            });
        } else {

        }
        sendEmptyMessageDelayed(HEAT, 60 * 1000);
    }

    public void handleHeatResponse(ProtocolContext protocol){
        try {
            userId =  PreferenceHelper.getLong("USERID",mContext);
            Response response = Response.parseFrom(protocol.getBodyBuffer());
            Log.e("abc","sendHeartBeat:"+response.toString());
            if(response.getErrorCode() == 0) {
                if(response.getHeartbeat()!=null){
                    ChatProtocol.HeartbeatResponse heartbeatResponse = response.getHeartbeat();
                    if(heartbeatResponse.getFriendChanged() != 0){
                        sendEmptyMessage(FRIENDSYNC);
                    }
                    if(heartbeatResponse.getMessageChanged() != 0){
                        sendEmptyMessage(MESSAGESYSNC);
                    }

                }
            }else{
                Log.e("abc","sendHeartBeat  getErrorMessage:"+response.getErrorMessage().toString());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public static NettyClient getAppClient() {
        if (appClient == null) {
            appClient = new NettyClient(new InetSocketAddress(HOST, PORT), 30 * 1000, new OnServerConnectListener() {
                @Override
                public void onConnectSuccess() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }

                @Override
                public void onConnectFailed(Throwable cause) {

                }
            });
            try {
                appClient.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return appClient;
    }

    private void init(Context context) {
        Log.e("JerryZhu", "init");
        appClient = new NettyClient(new InetSocketAddress(HOST, PORT), 30 * 1000, new OnServerConnectListener() {
            @Override
            public void onConnectSuccess() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        sendEmptyMessageDelayed(HEAT, 60 * 1000);
                        Log.e("abc", "onConnectSuccess: =============== ");
                    }
                });
            }

            @Override
            public void onConnectFailed(Throwable cause) {
                Log.e("abc", "onConnectFailed: =============== ");
                removeMessages(STARTCONN);
                sendEmptyMessageDelayed(STARTCONN, 10 * 1000);
            }
        });
        sendEmptyMessage(STARTCONN);
    }

    /**
     * 初始化
     */
    private void initReceiver() {
        unregisterReceiver();// Service可能被系统kill service重启后先注销之前的接收器
        brReceiver = new BrBroadcastReceiver(this);
        IntentFilter brfilter = new IntentFilter();
        brfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(brReceiver, brfilter);
    }

    private void handleNewFriend(){
        if (!App.isOnline()) {
            return;
        }

        RsProtocolContext protocolContext = RequestParamTools.getFriendSyncRequest();
        if(protocolContext == null){

        }else{
            getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        Response response = Response.parseFrom(protocol.getBodyBuffer());
                        Log.e("abc","friendSync:"+response.toString());
                        ChatProtocol.FriendSyncResponse friendSync = response.getFriendSync();
                        int type = friendSync.getSyncType();
                        if(type == 1){
                            List<ChatProtocol.Friend> friendList = friendSync.getFriendsList();
                            DataSupport.deleteAll(Friend.class);
                            for (int i =0 ;i<friendList.size();i++){
                                Friend friend = new Friend();
                                friend.setFriendId(friendList.get(i).getId());
                                friend.setManger_ids(friendList.get(i).getMangerIds());
                                friend.setMembers(friendList.get(i).getMembers());
                                friend.setName(friendList.get(i).getName());
                                friend.setPhoto(friendList.get(i).getPhoto());
                                friend.setType(friendList.get(i).getType());
                                friend.save();
                            }
                        }else{
                            List<ChatProtocol.Friend> rfriendList = friendSync.getRemoveFriendsList();
                            for (int i =0 ;i<rfriendList.size();i++){
                                DataSupport.deleteAll(Friend.class,"friendId = ?",rfriendList.get(i).getId()+"");
                            }
                            List<ChatProtocol.Friend> friendList = friendSync.getFriendsList();
                            for (int i =0 ;i<friendList.size();i++){
                                Friend friend = new Friend();
                                friend.setFriendId(friendList.get(i).getId());
                                friend.setManger_ids(friendList.get(i).getMangerIds());
                                friend.setMembers(friendList.get(i).getMembers());
                                friend.setName(friendList.get(i).getName());
                                friend.setPhoto(friendList.get(i).getPhoto());
                                friend.setType(friendList.get(i).getType());
                                int a = friend.updateAll("friendId = ?",""+friend.getFriendId());
                                Log.e("abc","保存结果："+a);
                                if(a == 0){
                                 friend.save();
                                }
                                //
                                //DataSupport.updateAll(News.class, values, "title = ?", "今日iPhone6发布");
                            }
                        }
                        GlobObject.friendList = DataSupport.findAll(Friend.class);
                        EventBus.getDefault().post(new AddFriend());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    private void handleNewMessage() {
        if (!App.isOnline()) {
            return;
        }
        if(userId == 0){
            User user = DataSupport.findFirst(User.class);
            if (user != null){
                userId = user.getUser_id();
            }else {
                return;
            }
        }
        removeMessages(MESSAGESYSNC);
        long id = DataSupport.max(Message.class,"messageId",long.class);
        RsProtocolContext protocolContext = RequestParamTools.getMessageSyncRequest(id);
        if (protocolContext == null) {

        } else {
            getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        Response response = Response.parseFrom(protocol.getBodyBuffer());
                        Log.e("abc",userId+"messageSyncResponse:"+response.toString());
                        ChatProtocol.MessageSyncResponse messageSyncResponse = response.getMessageSync();
                        List<ChatProtocol.Message> messageList = messageSyncResponse.getMessagesList();
                        if(messageList.size()>0){
                            for (int i =0 ;i<messageList.size();i++){
                                Message message = new Message();
                                MsgBean a = new MsgBean();
                                long chatid = messageList.get(i).getChatId();
                                message.setMessageId(messageList.get(i).getId());
                                message.setChat_id(messageList.get(i).getChatId());
                                message.setMsg_date(messageList.get(i).getMsgDate());
                                message.setRev_flag(messageList.get(i).getRevFlag());
                                message.setSender_id(messageList.get(i).getSenderId());
                                message.setText(messageList.get(i).getText());
                                message.setType(messageList.get(i).getType());
                                message.setTop_view(messageList.get(i).getTopView());
                                if(messageList.get(i).getSenderId() == userId){
                                    message.setDirect(2);
                                    a.setIsnew(false);
                                }else{
                                    message.setDirect(1);
                                    messageNotify(message);
                                    a.setIsnew(true);
                                }
                                message.saveOrUpdate("messageId = ?",""+message.getMessageId());
                                a.setMessage(message);

                                if(messageList.get(i).getSenderId() == userId){
                                    a.setChat_id(chatid);
                                    if(GlobObject.friendMap.get(chatid)!=null){
                                        a.setFriend(GlobObject.friendMap.get(chatid));
                                        GlobObject.msgMap.put(chatid,a);
                                    }
                                }else{
                                    a.setChat_id(messageList.get(i).getSenderId());
                                    if(GlobObject.friendMap.get(messageList.get(i).getSenderId())!=null){
                                        a.setFriend(GlobObject.friendMap.get(messageList.get(i).getSenderId()));
                                        GlobObject.msgMap.put(messageList.get(i).getSenderId(),a);
                                    }
                                }
                            }
                            EventBus.getDefault().post(new NewMessage());
                        }

                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void getUserSyncRequest(){
        if (!App.isOnline()) {
            return;
        }
        RsProtocolContext protocolContext = RequestParamTools.getUserSyncRequest();
        if (protocolContext != null) {
            BeanMessageHandler.getAppClient().sendRequest(protocolContext, new PbAsyncTcpResponse() {
                @Override
                protected void onSuccess(ProtocolContext protocol) {
                    try {
                        ChatProtocol.Response response = ChatProtocol.Response.parseFrom(protocol.getBodyBuffer());
                        Log.e("abc",response.toString());
                        if(response.getErrorCode() == 0){
                            ChatProtocol.UserSyncResponse userSyncResponse = response.getUserSync();
                            User user = DataSupport.findFirst(User.class);
                            if(userSyncResponse.hasName()){
                                user.setNickname(userSyncResponse.getName());
                            }
                            if(userSyncResponse.hasPhoto()){
                                user.setPhoto(userSyncResponse.getPhoto());
                            }
                            if(userSyncResponse.hasSound()){
                                user.setSound(userSyncResponse.getSound());
                            }

                            if(userSyncResponse.hasUsername()){
                                user.setName(userSyncResponse.getUsername());
                            }
                            user.save();
                            EventBus.getDefault().post(new UpdataUser());
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


    private void messageNotify(Message info) {
        if (notifyMgr != null) {
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext, countNotification++, intent, 0);
            //实例化NotificationCompat.Builde并设置相关属性
            String titleName = "";
            titleName = "计算器";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                    //设置小图标
                    .setSmallIcon(R.drawable.icon)
                    //设置通知标题
                    .setContentTitle(titleName)
                    //设置通知内容
                    .setContentText(info.getText())
                    .setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.beep))
                    .setContentIntent(contentIntent);
            //设置通知时间，默认为系统发出通知的时间，通常不用设置
//	                .setWhen(System.currentTimeMillis());
            //通过builder.build()方法生成Notification对象,并发送通知,id=1
            android.app.Notification notification = builder.build();
            notification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
            notifyMgr.notify(countNotification++, notification);
        }
    }

}
