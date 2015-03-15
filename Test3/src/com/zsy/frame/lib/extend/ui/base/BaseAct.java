package com.zsy.frame.lib.extend.ui.base;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.testin.agent.TestinAgent;
import com.umeng.analytics.MobclickAgent;
import com.zsy.frame.lib.extend.views.LoadingDialog;
import com.zsy.frame.lib.extend.views.ToastMsg;
import com.zsy.frame.lib.net.http.volley.Request;
import com.zsy.frame.lib.net.http.volley.Response.ErrorListener;
import com.zsy.frame.lib.net.http.volley.VolleyError;
import com.zsy.frame.lib.net.http.volley.app.VolleyRequestManager;
import com.zsy.frame.lib.net.http.volley.app.samy.ServerFlagError;
import com.zsy.frame.lib.net.http.volley.app.samy.ServerJsonUnParseError;
import com.zsy.frame.lib.ui.activity.SYBaseAct;

public abstract class BaseAct extends SYBaseAct implements ErrorListener {
	/**自己主动控制网络请求的*/
	private Set<Object> tags = new HashSet<Object>();
	
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

	/**
	 * 添加网络请求
	 * 
	 * @param request
	 */
	protected void executeRequest(Request request) {
		VolleyRequestManager.addRequest(request, this);
		tags.add(this);
	}

	/**
	 * 添加网络请求
	 * 
	 * @param request
	 */
	protected void executeRequest(Request request, Object tag) {
		VolleyRequestManager.addRequest(request, tag);
		tags.add(tag);
	}

	/**
	 * 取消网络请求
	 * 
	 * @param tag
	 */
	protected void cancelRequest(Object tag) {
		VolleyRequestManager.cancelAll(tag);
	}

	@Override
	protected void onStop() {
		super.onStop();
		for (Object tag : tags) {
			VolleyRequestManager.cancelAll(tag);
		}
	}
	
	// 默认网络请求异常回调
	@Override
	public void onErrorResponse(VolleyError error) {
		dismissLoadingDialog();
		if (CURRENT_PAGE > 1) {// 加载异常回退到当前页
			CURRENT_PAGE--;
		}
		String msg = "网络异常";
		if (error instanceof ServerFlagError) {
			msg = ((ServerFlagError) error).result.msg;
		} else if (error instanceof ServerJsonUnParseError) {
			try {
				String res = ((ServerJsonUnParseError) error).result;
				JSONObject jsonObject = new JSONObject(res);
				msg = jsonObject.getString("msg");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (error.networkResponse != null
						&& error.networkResponse.data != null)
					msg = new String(error.networkResponse.data, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		showToastMsg(msg);
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
