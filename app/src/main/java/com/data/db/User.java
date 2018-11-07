package com.data.db;

import org.litepal.crud.DataSupport;

public class User extends DataSupport {
    private long id;
    private String session_id;
    private long user_id;
    private String name;//帐号
    private String nickname;//昵称
    private String photo;//头像
    private byte[] photobyte;
    private String sound;//消息提示音


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public byte[] getPhotobyte() {
        return photobyte;
    }

    public void setPhotobyte(byte[] photobyte) {
        this.photobyte = photobyte;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
}
