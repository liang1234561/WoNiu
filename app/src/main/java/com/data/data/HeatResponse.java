package com.data.data;

import com.data.util.net.bean.ProtocolContext;

public class HeatResponse {
    private  ProtocolContext protocolContext;

    public HeatResponse(ProtocolContext protocolContext) {
        this.protocolContext = protocolContext;
    }

    public ProtocolContext getProtocolContext() {
        return protocolContext;
    }

    public void setProtocolContext(ProtocolContext protocolContext) {
        this.protocolContext = protocolContext;
    }
}
