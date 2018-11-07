package com.data.data.bean;

import com.data.db.Friend;
import com.data.db.Message;

import java.io.Serializable;

public class MsgBean implements Serializable {
    private long chat_id;//聊天对象id（用户id或群id）群id为负数好友id为正数
    private Message message;
    private Friend friend;
    private boolean isnew;

    public boolean isIsnew() {
        return isnew;
    }

    public void setIsnew(boolean isnew) {
        this.isnew = isnew;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Friend getFriend() {
        return friend;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
    }

    public long getChat_id() {
        return chat_id;
    }

    public void setChat_id(long chat_id) {
        this.chat_id = chat_id;
    }

    @Override
    public String toString() {
        return "MsgBean{" +
                "chat_id=" + chat_id +
                ", message=" + message.toString() +
                ", friend=" + friend.toString() +
                ", isnew=" + isnew +
                '}';
    }
}
