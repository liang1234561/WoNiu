package com.data.util.net.bean;

public  abstract class ProtocolHeader {
	protected int contentLength;//pb长度
	protected short sequenceID = -1;//随机码
	
	/**
	 * 头部20个字节序列化
	 * @return 序列号字节
	 */
	public abstract byte[] getHeaderByte();
	
	public int getContentLength() {
		return contentLength;
	}
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}
	public short getSequenceID() {
		return sequenceID;
	}
	public void setSequenceID(short sequenceID) {
		this.sequenceID = sequenceID;
	}
}
