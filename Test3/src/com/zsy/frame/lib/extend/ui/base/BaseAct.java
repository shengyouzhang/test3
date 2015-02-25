package com.zsy.frame.lib.extend.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.testin.agent.TestinAgent;
import com.umeng.analytics.MobclickAgent;
import com.zsy.frame.lib.extend.views.LoadingDialog;
import com.zsy.frame.lib.extend.views.ToastMsg;
import com.zsy.frame.lib.ui.activity.SYBaseAct;

public abstract class BaseAct extends SYBaseAct {
	public static final String LEFT_TITLE = "leftTitle";// 所有左侧显示字符的传递key
	/** 分页加载数据，每页数据量 */
	public static final int PAGE_SIZE = 10;
	/** 当前页，用于分页加载数据 */
	public int CURRENT_PAGE = 1;
	protected LayoutInflater inflater;
	protected Context context;
	public LoadingDialog loadingDialog;
	protected View netErrorView;

	public BaseAct() {
		setHiddenActionBar(true);
		// setBackListener(false);
		// setScreenOrientation(ScreenOrientation.VERTICAL);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		inflater = LayoutInflater.from(context);
//		在应用程序入口Activity中的onCreate()方法中调用如下方法：后期得调整位置
		TestinAgent.init(this);
		super.onCreate(savedInstanceState);
	}

	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	public void showLoadingDialog(String parameter) {
		if (null == loadingDialog) {
			loadingDialog = new LoadingDialog(aty);
			// loadingDialog.setOnCancelListener(this);
		}
		loadingDialog.setTitle(parameter);
		if (!loadingDialog.isShowing()) loadingDialog.show();
	}

	public void dismissLoadingDialog() {
		if (null != loadingDialog) {
			LoadingDialog.dismissDialog(loadingDialog);
		}
	}

	public void showToastMsg(String msg) {
		ToastMsg.showToastMsg(this, msg);
	}

	public void showToastMsg(int strId) {
		ToastMsg.showToastMsg(this, strId);
	}

	/** 校验输入 *************************/
	/* 获取输入内容 */
	public String getInputStr(EditText et) {
		return et.getText().toString().trim();
	}
}
