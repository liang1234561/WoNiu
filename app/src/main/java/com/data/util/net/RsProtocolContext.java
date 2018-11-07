package com.data.util.net;

import com.data.util.CompressUtil;
import com.data.util.net.bean.ProtocolContext;
import com.data.util.net.bean.ProtocolHeader;


import java.io.IOException;

public class RsProtocolContext extends ProtocolContext {
	@Override
	public ProtocolHeader parseHeader(byte[] recvBuffer) {
		RsProtocolHeader header = new RsProtocolHeader();
		byte[] requestBuffer = new byte[1];
		byte[] checkCodeBuffer = new byte[1];
		byte[] gzipBuffer = new byte[1];

		System.arraycopy(recvBuffer, 0, gzipBuffer, 0, 1);
		System.arraycopy(recvBuffer, 1, requestBuffer, 0, 1);
		System.arraycopy(recvBuffer, 2, checkCodeBuffer, 0, 1);

		header.setRequestDirect(requestBuffer[0] & 0xff);
		header.setCheckCode((byte)checkCodeBuffer[0] & 0xff);
		header.setGzip(gzipBuffer[0] & 0xff);
		this.headerInfo = header;
		return header;
	}
	
	public void setBodyBufferRes(byte[] bodyBuffer){
		this.bodyBuffer = bodyBuffer;
	}
}
