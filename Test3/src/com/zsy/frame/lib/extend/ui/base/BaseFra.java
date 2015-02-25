package com.zsy.frame.lib.extend.ui.base;

import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.ErrorListener;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.view.View;

import com.zsy.frame.lib.extend.R;
import com.zsy.frame.lib.extend.views.LoadingDialog;
import com.zsy.frame.lib.extend.views.ToastMsg;
import com.zsy.frame.lib.ui.fragment.SYBaseFra;

public abstract class BaseFra extends SYBaseFra implements ErrorListener, OnCancelListener {
	/**
	 * 嵌套viewpager时，第一次显示回调
	 * 
	 * @author fanxing 创建于 Dec 3, 2014
	 */
	public interface OnInitShowListener {
		void onInitShow();
	}

	protected View netErrorView;

	/** 分页加载数据，每页数据量 */
	protected static final int PAGE_SIZE = 10;
	/** 当前页，用于分页加载数据 */
	protected int CURRENT_PAGE = 1;

	private LoadingDialog loadingDialog;

	public void showLoadingDialog(String parameter) {
		if (null == loadingDialog) {
			loadingDialog = new LoadingDialog(getActivity());
			loadingDialog.setOnCancelListener(this);
		}
		loadingDialog.setTitle(parameter);
		if (!loadingDialog.isShowing()) loadingDialog.show();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
	}

	public void dismissLoadingDialog() {
		if (null != loadingDialog) {
			LoadingDialog.dismissDialog(loadingDialog);
		}
	}

	public void showToastMsg(String msg) {
		ToastMsg.showToastMsg(getActivity(), msg);
	}

	public void showToastMsg(int strId) {
		ToastMsg.showToastMsg(getActivity(), strId);
	}

	private Set<Object> tags = new HashSet<Object>();

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		getActivity().overridePendingTransition(R.anim.base_activity_right_in, R.anim.base_activity_right_out);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		getActivity().overridePendingTransition(R.anim.base_activity_right_in, R.anim.base_activity_right_out);
	}
}
