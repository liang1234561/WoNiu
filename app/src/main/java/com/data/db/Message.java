package com.data.db;

import org.litepal.crud.DataSupport;

public class Message extends DataSupport {
    private long messageId;//消息编号
    private String text;//消息内容，如果为复合消息结构为json具体根据type判断
    private int type;//1文本（包含emoji）2图片3文件4语音消息 100 通知类消息（文本通知）---其中文本消息为原始内容，其他类型为json对象｛url,type,other｝url 为文件、图片、语音等实际地址
    private int top_view;//置顶标记1为置顶
    private int rev_flag;//撤销标记1为撤销
    private long msg_date;//消息时间
    private long chat_id;//聊天对象id（用户id或群id）群id为负数好友id为正数
    private long sender_id;//发送人
    private int direct;//是否是自己发送  1是发送方 2是自己发的
    private String fileUrl;//文件url
    private String fileType;//
    private String voicelength;

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getVoicelength() {
        return voicelength;
    }

    public void setVoicelength(String voicelength) {
        this.voicelength = voicelength;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getDirect() {
        return direct;
    }

    public void setDirect(int direct) {
        this.direct = direct;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTop_view() {
        return top_view;
    }

    public void setTop_view(int top_view) {
        this.top_view = top_view;
    }

    public int getRev_flag() {
        return rev_flag;
    }

    public void setRev_flag(int rev_flag) {
        this.rev_flag = rev_flag;
    }

    public long getMsg_date() {
        return msg_date;
    }

    public void setMsg_date(long msg_date) {
        this.msg_date = msg_date;
    }

    public long getChat_id() {
        return chat_id;
    }

    public void setChat_id(long chat_id) {
        this.chat_id = chat_id;
    }

    public long getSender_id() {
        return sender_id;
    }

    public void setSender_id(long sender_id) {
        this.sender_id = sender_id;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", text='" + text + '\'' +
                ", type=" + type +
                ", top_view=" + top_view +
                ", rev_flag=" + rev_flag +
                ", msg_date=" + msg_date +
                ", chat_id=" + chat_id +
                ", sender_id=" + sender_id +
                ", direct=" + direct +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileType='" + fileType + '\'' +
                ", voicelength='" + voicelength + '\'' +
                '}';
    }
}
