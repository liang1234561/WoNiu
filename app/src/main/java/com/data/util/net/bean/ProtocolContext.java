package com.data.util.net.bean;


public abstract class ProtocolContext {
    protected ProtocolHeader headerInfo = null;//请求头长度20
    protected byte[] bodyBuffer = null;//发送内容序列化

    /**
     * 将收到的字节流转为ProtocoHeader
     */
    public abstract ProtocolHeader parseHeader(byte[] headerBuffer);

    /**
     * 设置返回数据处理，可处理压缩，校对之类自定义处理
     * @param bodyBuffer
     */
    public abstract void setBodyBufferRes(byte[] bodyBuffer);

    public void setHeader(ProtocolHeader header) {
        headerInfo = header;
    }

    public ProtocolHeader getHeaderInfo() {
        return headerInfo;
    }

    public short getSequenceID() {
        return headerInfo.getSequenceID();
    }

    public void setSequenceID(short sequenceID) {
        headerInfo.setSequenceID(sequenceID);
    }
    
    public byte[] getBodyBuffer() {
        return bodyBuffer;
    }

    public void setBodyBuffer(byte[] bodyBuffer) {
        this.bodyBuffer = bodyBuffer;
        headerInfo.setContentLength(bodyBuffer.length);
    }

    /**
     * 将需要发送的内容序列化为byte字节流
     */
    public byte[] getSendBuffer() {
        byte[] sendBuffer = new byte[20 + headerInfo.getContentLength()];
        System.arraycopy(headerInfo.getHeaderByte(), 0, sendBuffer, 0, 20);
        System.arraycopy(bodyBuffer, 0, sendBuffer, 20, headerInfo.getContentLength());
        return sendBuffer;
    }
}
