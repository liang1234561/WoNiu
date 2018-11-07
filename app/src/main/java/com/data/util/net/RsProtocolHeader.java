package com.data.util.net;

import com.data.util.NumberUtil;
import com.data.util.net.bean.ProtocolHeader;

/**
 * 描述与服务器通信的协议头信息
 */

public class RsProtocolHeader extends ProtocolHeader {

    private int requestDirect;//优先级别
    private int checkCode;//效验码
    private int gzip;//是否压缩
    private int encodeNum;//加密密钥

    public void setEncodeNum(int encodeNum) {
        this.encodeNum = encodeNum;
    }

    public void setCheckCode(int checkCode) {
        this.checkCode = checkCode;
    }

    public int getGzip() {
        return gzip;
    }

    public void setGzip(int gzip) {
        this.gzip = gzip;
    }

    public void setRequestDirect(int requestDirect) {
        this.requestDirect = requestDirect;
    }

    public int getLength() {
        return 20;
    }

    /**
     * 将协议头信息序列化为byte字节流
     */
    public byte[] getHeaderByte() {
        byte[] headByte = new byte[20];
        byte[] bodyLength = NumberUtil.intToByteArray(contentLength);
        byte[] sequenceFlag = NumberUtil.shortToByteArray(sequenceID);
        byte[] gzipFlag = new byte[]{(byte) (gzip & 0xff)};
        byte[] requestFlag = new byte[]{(byte) (requestDirect & 0xff)};
        byte[] checkCodeFlag = new byte[]{(byte) (checkCode & 0xff)};
        byte[] encodeLength = NumberUtil.intToByteArray(encodeNum);

        System.arraycopy(bodyLength, 0, headByte, 0, 4);
        System.arraycopy(sequenceFlag, 0, headByte, 4, 2);
        System.arraycopy(gzipFlag, 0, headByte, 6, 1);
        System.arraycopy(requestFlag, 0, headByte, 7, 1);
        System.arraycopy(checkCodeFlag, 0, headByte, 8, 1);
        System.arraycopy(encodeLength, 0, headByte, 9, 4);
        return headByte;
    }

}
