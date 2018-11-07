package com.data.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.alibaba.fastjson.JSONObject;
import com.data.data.bean.MsgBean;
import com.data.db.Friend;
import com.data.db.Message;
import com.juns.wechat.bean.User;

public class GlobObject {
    public static String CONFIG_SERVER;
    public static String REPORT_SERVER;
    public static JSONObject PARAM = new JSONObject();

    public static Vector friendlists = new Vector();

    public static ConcurrentLinkedQueue<Message> FILE_POOL = new ConcurrentLinkedQueue<Message>();

    public static Long reportTime = System.currentTimeMillis();

    public static Long configTime = System.currentTimeMillis();

    public static List<Friend> friendList = new ArrayList<Friend>();// 好友信息
    public static ConcurrentHashMap<Long, Friend> friendMap = new ConcurrentHashMap<Long, Friend>();//根据好友id储存内容
    public static ConcurrentHashMap<Long, MsgBean> msgMap = new ConcurrentHashMap<Long, MsgBean>();

    public static ConcurrentHashMap<Long, MsgBean> getMsgMap() {
        return msgMap;
    }

    public static void setMsgMap(ConcurrentHashMap<Long, MsgBean> msgMap) {
        GlobObject.msgMap = msgMap;
    }

    public synchronized static void clear(){
        friendList.clear();
        friendMap.clear();
        msgMap.clear();
    }
}
