package com.data.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.juns.wechat.R;


/**
 * Created by lt on 2016/8/6.
 */
public class DialogManager {

    private Dialog mDialog;

    private ImageView mVoice;
    private TextView mLabel;

    private Context mContext;
    private AnimationDrawable animationDrawable;

    public DialogManager(Context context) {
        mContext = context;
    }

    public void showRecordingDialog() {
        mDialog = new Dialog(mContext, R.style.Theme_AudioDialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_main, null);
        mDialog.setContentView(view);

        mVoice = (ImageView) mDialog.findViewById(R.id.id_recorder_dialog_voice);
        mLabel = (TextView) mDialog.findViewById(R.id.id_recorder_dialog_label);

        animationDrawable = (AnimationDrawable) mVoice.getBackground();
        animationDrawable.setOneShot(false);

        mDialog.show();
    }

    public void start() {
        if (mDialog != null && mDialog.isShowing()&&!animationDrawable.isRunning()) {
            animationDrawable.start();
            mLabel.setText("手指上滑，取消发送");
        }
    }

    public void recording() {
        if (mDialog != null && mDialog.isShowing()) {
            animationDrawable.stop();
            mLabel.setText("手指上滑，取消发送");
        }
    }

    public void wantToCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            mLabel.setText("松开手指，取消发送");
        }

    }

    public void toShort() {
        if (mDialog != null && mDialog.isShowing()) {
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            mLabel.setText("录音时间过短");
        }

    }

    public void dimissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }

    }

    /**
     * 通过level更新声音级别图片
     * @param level
     */
    public void updateVoiceLevel(int level) {
        start();
    }
}
