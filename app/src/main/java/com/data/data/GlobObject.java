package com.data.data;

import com.data.data.bean.MsgBean;
import com.data.db.Friend;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GlobObject {
    public static List<Friend> friendList = new ArrayList<Friend>();// 好友信息
    public static ConcurrentHashMap<Long, Friend> friendMap = new ConcurrentHashMap<Long, Friend>();//根据好友id储存内容
    public static ConcurrentHashMap<Long, MsgBean> msgMap = new ConcurrentHashMap<Long, MsgBean>();

    public static ConcurrentHashMap<Long, MsgBean> getMsgMap() {
        return msgMap;
    }

    public static void setMsgMap(ConcurrentHashMap<Long, MsgBean> msgMap) {
        GlobObject.msgMap = msgMap;
    }

    public synchronized static void clear() {
        friendList.clear();
        friendMap.clear();
        msgMap.clear();
    }
}
