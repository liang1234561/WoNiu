package com.data.ui.activity.friend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.data.data.AddFriend;
import com.data.db.Friend;
import com.data.operation.BeanMessageHandler;
import com.data.pbprotocol.ChatProtocol;
import com.data.ui.activity.BaseActivity;
import com.data.ui.activity.ChatActivity;
import com.data.ui.view.TT;
import com.data.util.image.SmartImageView;
import com.data.util.net.PbAsyncTcpResponse;
import com.data.util.net.RequestParamTools;
import com.data.util.net.RsProtocolContext;
import com.data.util.net.bean.ProtocolContext;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juns.wechat.App;
import com.juns.wechat.Constants;
import com.juns.wechat.R;
import com.juns.wechat.common.Utils;

import org.litepal.crud.DataSupport;

import java.util.List;

import de.greenrobot.event.EventBus;

//好友详情
public class FriendMsgActivity extends BaseActivity implements OnClickListener {
    private TextView txt_title, tv_name;
    private ImageView img_back, img_right;
    private String Name;
    private long friendId;
    private Button btn_sendmsg;
    private String userId;
    private SmartImageView iv_avatar;
    private Button btn_delete;
    private Friend friend;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_friendmsg);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initControl() {
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_title.setText("详细资料");
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_right = (ImageView) findViewById(R.id.img_right);
        img_right.setImageResource(R.drawable.icon_more);
        img_right.setVisibility(View.VISIBLE);
        btn_sendmsg = (Button) findViewById(R.id.btn_sendmsg);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_sendmsg.setTag("1");
        iv_avatar = (SmartImageView) findViewById(R.id.iv_avatar);
        tv_name = (TextView) findViewById(R.id.tv_name);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        friendId = getIntent().getLongExtra(Constants.User_ID, 0);
        List<Friend> friends = DataSupport.where("friendId = ?", friendId + "").find(Friend.class);
        if (friends.size() != 1) {
            finish();
        } else {
            friend = friends.get(0);
            tv_name.setText(friend.getName());
            iv_avatar.setImageUrl(friend.getPhoto());
        }
    }

    @Override
    protected void setListener() {
        img_back.setOnClickListener(this);
        img_right.setOnClickListener(this);
        btn_sendmsg.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Utils.finish(FriendMsgActivity.this);
                break;
            case R.id.img_right:

                break;
            case R.id.btn_sendmsg:
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(Constants.User_ID, friendId);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in,
                        R.anim.push_left_out);
                break;
            case R.id.btn_delete:
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("type","remove");
                jsonObj.put("id",friend.getFriendId());

                sendPipe("user_friend_service",jsonObj.toJSONString());
                break;
            default:
                break;
        }
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
                        Log.e("abc",response.toString());
                        if(response.getErrorCode() == 0){
                            JSONObject jsonObject = JSONObject.parseObject(response.getPipe().getResponse());
                            if(jsonObject.getInteger("code") == 0){
                                DataSupport.deleteAll(Friend.class,"friendId = ?",friend.getFriendId()+"");
                                EventBus.getDefault().post(new AddFriend());
                                finish();
                            }else{
                                Log.e("abc delete",jsonObject.getString("message"));
                                TT.show(FriendMsgActivity.this,""+jsonObject.getString("message"));
                            }
                        }else {
                            Log.e("abc delete",response.getErrorMessage());
                            TT.show(FriendMsgActivity.this,""+response.getErrorMessage());
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
