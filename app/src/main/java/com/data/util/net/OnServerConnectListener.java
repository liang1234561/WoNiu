package com.data.util.net;


public interface OnServerConnectListener {
    void onConnectSuccess();
    void onConnectFailed(Throwable cause);
}
