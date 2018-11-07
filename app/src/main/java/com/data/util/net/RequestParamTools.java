package com.data.util.net;


import com.data.db.User;
import com.data.pbprotocol.ChatProtocol;
import com.data.pbprotocol.ChatProtocol.Request;
import com.data.util.AssistUtil;
import com.data.util.CRC8;
import com.data.util.Des3;
import com.google.protobuf.ByteString;

import org.litepal.crud.DataSupport;


public class RequestParamTools {
    public static User getUser() {
        return DataSupport.findFirst(User.class);
    }

    /**
     * 已经登录成功后的协议需要的userID,SessionId,AgentID
     *
     * @param request
     */
    private static void setRequest(Request.Builder request) {
        User user = getUser();
        //用户id
        request.setUserId(user.getUser_id());
        //token
        request.setSessionId(user.getSession_id());
        //设备的唯一id
    }

    /**
     * 设置请求头内容
     *
     * @param request
     * @return
     * @throws Exception
     */
    private static RsProtocolContext getProtocolContext(Request.Builder request, int level) throws Exception {
        RsProtocolContext requestContex = new RsProtocolContext();
        RsProtocolHeader header = new RsProtocolHeader();
        int randomNum = AssistUtil.getRandom();
        byte[] desRequestByte = Des3.encode(request.build().toByteArray(), randomNum);
        header.setGzip(0);
        header.setRequestDirect(level);
        header.setCheckCode(CRC8.calcCrc8(desRequestByte));
        header.setEncodeNum(randomNum);

        requestContex.setHeader(header);//处理后的header
        requestContex.setBodyBuffer(request.build().toByteArray());//加密后的内容
        return requestContex;
    }

    /**
     * 注册请求数据
     *

     * @param verifyCode
     * @return
     */
    public static RsProtocolContext getHeartbeat(String verifyCode) {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.HeartbeatRequest.Builder heat = ChatProtocol.HeartbeatRequest.newBuilder();
            heat.setCurrentVersion(verifyCode);
            request.setHeartbeat(heat);
            setRequest(request);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }


    public static RsProtocolContext getRegistRequest(String username,String password,String code,long key) {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.RegistRequest.Builder build = ChatProtocol.RegistRequest.newBuilder();
            build.setUsername(username);
            build.setPassword(password);
            build.setCode(code);
            build.setKey(String.valueOf(key));
            request.setRegistRequest(build);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }

    public static RsProtocolContext getLoginRequest(String username,String password,String version,String deviceId) {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.LoginRequest.Builder build = ChatProtocol.LoginRequest.newBuilder();
            build.setUsername(username);
            build.setPassword(password);
            build.setCurrentVersion(version);
            build.setDeviceId(deviceId);
            request.setLogin(build);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }

    public static RsProtocolContext getLogoutRequest() {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.LogoutRequest.Builder build = ChatProtocol.LogoutRequest.newBuilder();
            request.setLogout(build);
            setRequest(request);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }

    public static RsProtocolContext getPipeRequest(String code,String json) {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.PipeRequest.Builder build = ChatProtocol.PipeRequest.newBuilder();
            build.setCode(code);
            build.setRequest(json);
            request.setPipe(build);
            setRequest(request);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }

    public static RsProtocolContext getFriendSyncRequest() {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.FriendSyncRequest.Builder build = ChatProtocol.FriendSyncRequest.newBuilder();
            request.setFriendSync(build);
            setRequest(request);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }
    public static RsProtocolContext getMessageSyncRequest(long lastId) {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.MessageSyncRequest.Builder build = ChatProtocol.MessageSyncRequest.newBuilder();
            build.setLastId(lastId);
            request.setMessageSync(build);
            setRequest(request);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }

    public static RsProtocolContext getUserSyncRequest() {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.UserSyncRequest.Builder build = ChatProtocol.UserSyncRequest.newBuilder();
            request.setUserSync(build);
            setRequest(request);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }

    public static RsProtocolContext getFileUploadRequest(String type,byte[] content,int fileType) {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.FileUploadRequest.Builder build = ChatProtocol.FileUploadRequest.newBuilder();
            build.setContent(ByteString.copyFrom(content));
            build.setFileSuff(type);
            build.setType(type);
            request.setFileUpload(build);
            setRequest(request);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }

    public static RsProtocolContext getCodeRequest(String phone) {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.FileUploadRequest.Builder build = ChatProtocol.FileUploadRequest.newBuilder();

            request.setFileUpload(build);
            setRequest(request);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }

    public static RsProtocolContext getSendMessageRequest(long chatid,String text,int type) {
        try {
            Request.Builder request = Request.newBuilder();
            ChatProtocol.SendMessageRequest.Builder build = ChatProtocol.SendMessageRequest.newBuilder();
            build.setChatId(chatid);
            build.setText(text);
            build.setType(type);
            request.setSendMessage(build);
            setRequest(request);
            return getProtocolContext(request, 22);
        } catch (Exception e) {

        }
        return null;
    }
}
