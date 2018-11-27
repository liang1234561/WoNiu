package com.data;

import com.juns.wechat.App;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "BrExceptionHandler";
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static ExceptionHandler instance = new ExceptionHandler();
    private App mContext;

    private ExceptionHandler() {
    }

    public static ExceptionHandler getInstance() {
        return instance;
    }

    //初始化
    public void init(App context) {
        mContext = context;
        //获取系统默认的Exception处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将MobieExceptionHandler设置为本应用的Exception处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    //收到并处理异常消息
    public void uncaughtException(Thread t, Throwable e) {
        if (!handlerException(e) && mDefaultHandler != null) {//消息没有被handlerException处理则交给系统处理
            mDefaultHandler.uncaughtException(t, e);
        } else {//异常处理后重新启动service
            mContext.exit();
        }
    }

    //处理应用中没有捕获的异常
    private boolean handlerException(Throwable e) {
        if (e == null) {
            return false;
        }
        return true;
    }

}