package com.data.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.data.util.image.SmartImageView;
import com.juns.wechat.R;

/**
 * 下载显示大图
 * 
 */
public class ShowBigImage extends BaseActivity {

	private SmartImageView image;
	private String res;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_show_image);
		super.onCreate(savedInstanceState);

		image = (SmartImageView) findViewById(R.id.image);
		res = getIntent().getStringExtra("image");
		image.setImageUrl(res);
		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void initControl() {

	}

	@Override
	protected void initView() {

	}

	@Override
	protected void initData() {

	}

	@Override
	protected void setListener() {

	}
}
