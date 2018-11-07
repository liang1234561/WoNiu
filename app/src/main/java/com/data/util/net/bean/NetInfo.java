package com.data.util.net.bean;


import com.data.util.net.PbAsyncTcpResponse;

public class NetInfo {
	private int code;//通信的随机码
	private ProtocolContext protocol;//发送的数据
	private PbAsyncTcpResponse pbAsyncTcpResponse;//处理返回数据

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public ProtocolContext getProtocol() {
		return protocol;
	}
	public void setProtocol(ProtocolContext protocol) {
		this.protocol = protocol;
	}
	public PbAsyncTcpResponse getPbAsyncTcpResponse() {
		return pbAsyncTcpResponse;
	}
	public void setPbAsyncTcpResponse(PbAsyncTcpResponse pbAsyncTcpResponse) {
		this.pbAsyncTcpResponse = pbAsyncTcpResponse;
	}
}
