package com.data.db;

import org.litepal.crud.DataSupport;

public class Friend extends DataSupport{
    private long friendId;//编号（用户id或群id）群id为负数好友id为正数
    private String name;//昵称
    private int type;//1好友2群3好友请求
    private String photo;//头像url
    private String manger_ids;//管理id
    private String members;//群友[Friend]z

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getManger_ids() {
        return manger_ids;
    }

    public void setManger_ids(String manger_ids) {
        this.manger_ids = manger_ids;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "friendId=" + friendId +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", photo='" + photo + '\'' +
                ", manger_ids='" + manger_ids + '\'' +
                ", members='" + members + '\'' +
                '}';
    }
}
