package com.zsy.frame.lib.extend.ui.base;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ZoomButtonsController;

import com.zsy.frame.lib.extend.R;
import com.zsy.frame.lib.extend.views.CustomWebView;
import com.zsy.frame.lib.extend.views.CustomWebViewClient;
import com.zsy.frame.lib.extend.views.ToastMsg;
import com.zsy.frame.lib.utils.NetUtil;

/**
 * @description：自定义浏览器;intent通过PAGE_URL把要加载的网络地址传过来和头部title；
 * @author samy
 * @date 2014年10月8日 下午5:33:56
 */
public class BaseWebViewAct extends BaseAct {
	public static final String DEFAULT_PAGE = "http://www.365sji.com/";
	public static final String PAGE_URL = "url";
	public static final String PAGE_TITLE = "title";
	public static final String IS_SHOW_BOTTOM = "is_show_bottom";

	private String loadUrl;
	private String title;
	private Boolean isShowBottom = false;

	private CustomWebView mWebView;
	private ProgressBar web_src_loadProgress;
	private View button_forword, button_back;
	private RelativeLayout webView_parent;
	/** 标题的集合，只涉及增删 */
	private List<String> titleList = new LinkedList<String>();
	// private View loadingParentView;
	// private ProgressBar loadingProgressBar;
	// private TextView loading_tip_tv;
	ProgressBar mProgressBar;
	/** 标题 */
	private TextView tvHeadTitle;

	@Override
	public void setRootView() {
		setContentView(R.layout.base_web_src_view);
	}

	@Override
	protected void initData() {
		super.initData();
		initFromIntent();
	}

	@Override
	protected void initWidget(Bundle savedInstance) {
		super.initWidget(savedInstance);
		// mProgressBar.setMax(100);
		initTitleBar();
		// initLoadingView();
		mProgressBar = (ProgressBar) findViewById(R.id.pb);
		initWebView();
		mWebView.loadUrl(loadUrl);
	}

	@Override
	public void onBackPressed() {
		// onCloseClick();
		if (mWebView.canGoBack()) {
			mWebView.goBack();
			titleList.remove(0);
			tvHeadTitle.setText(titleList.get(0));
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		webView_parent.removeView(mWebView);
		mWebView.setVisibility(View.GONE);
		mWebView.destroy();
		mWebView = null;
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button_back) {
			onBackwordUrlClick();
		}
		else if (v.getId() == R.id.button_forword) {
			onForwordUrlClick();
		}
		else if (v.getId() == R.id.button_reload) {
			onReloadUrlClick();
		}
		else if (v.getId() == R.id.button_close) {
			onCloseClick();
		}
		else if (v.getId() == R.id.left) {
			onCloseClick();
		}
	}

	/**
	 * 初始数据
	 */
	private void initFromIntent() {
		loadUrl = getIntent().getStringExtra(PAGE_URL);
		title = getIntent().getStringExtra(PAGE_TITLE);
		isShowBottom = getIntent().getBooleanExtra(IS_SHOW_BOTTOM, false);
		if (TextUtils.isEmpty(loadUrl)) {
			loadUrl = DEFAULT_PAGE;
		}
		if (!isShowBottom) {
			findViewById(R.id.ll_webNavi_root).setVisibility(View.GONE);
		}
	}

	private void initTitleBar() {
		tvHeadTitle = (TextView) findViewById(R.id.title);
		tvHeadTitle.setText(title);
		TextView leftHeadIb = (TextView) findViewById(R.id.left);
		leftHeadIb.setVisibility(View.VISIBLE);
		findViewById(R.id.right).setVisibility(View.GONE);

		button_forword = findViewById(R.id.button_forword);
		button_back = findViewById(R.id.button_back);
		button_forword.setEnabled(false);

		ImageButton button_back = (ImageButton) findViewById(R.id.button_back);
		ImageButton button_forword = (ImageButton) findViewById(R.id.button_forword);
		ImageButton button_reload = (ImageButton) findViewById(R.id.button_reload);
		ImageButton button_close = (ImageButton) findViewById(R.id.button_close);
		leftHeadIb.setOnClickListener(this);
		button_back.setOnClickListener(this);
		button_forword.setOnClickListener(this);
		button_reload.setOnClickListener(this);
		button_close.setOnClickListener(this);
	}

	// private void initLoadingView() {
	// loadingParentView = findViewById(R.id.ll_data_loading);
	// loading_tip_tv = (TextView) loadingParentView.findViewById(R.id.loading_tip_txt);
	// loadingProgressBar = (ProgressBar) loadingParentView.findViewById(R.id.loading_progress_bar);
	// }

	private void initWebView() {
		web_src_loadProgress = (ProgressBar) findViewById(R.id.web_src_loadProgress);
		webView_parent = (RelativeLayout) findViewById(R.id.webView_parent);
		mWebView = (CustomWebView) findViewById(R.id.webView);
		mWebView.setCanTouchZoom(false);
		hideBuiltInZoomControls(mWebView);

		mWebView.setDownloadListener(new DownloadListener() {// 下载
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		mWebView.setWebViewClient(new CustomWebViewClient(this));
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String mTitle) {
				super.onReceivedTitle(view, mTitle);
				titleList.add(0, mTitle);
				tvHeadTitle.setText(mTitle);
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				mProgressBar.setProgress(newProgress);
				if (newProgress == 100) {
					mProgressBar.setVisibility(View.GONE);
				}
				super.onProgressChanged(view, newProgress);// 进度条
				// if (newProgress > 90) {
				// hideLoading();
				// }
				Log.i("", "the webview onProgressChanged newProgress" + newProgress);
				web_src_loadProgress.setProgress(newProgress);
			}

		});
	}

	public void onPageStarted(WebView view, String url) {
		updataUI(view);
		showLoadingProgress();
	}

	public void onPageFinished(WebView view, String url) {
		updataUI(view);
		hideLoadingProgress();
		// hideLoading();
	}

	public void shouldOverrideUrlLoading(WebView view, String url) {
		updataUI(view);
	}

	// /**
	// * 显示加载等待页
	// */
	// private void showLoading() {
	// if (loadingParentView.getVisibility() == View.GONE) loadingParentView.setVisibility(View.VISIBLE);
	// if (!loadingProgressBar.isShown()) loadingProgressBar.setVisibility(View.VISIBLE);
	// }

	// /**
	// * 隐藏加载等待页
	// */
	// private void hideLoading() {
	// if (loadingParentView.getVisibility() == View.VISIBLE) loadingParentView.setVisibility(View.GONE);
	// }

	/**
	 * 显示进度
	 */
	private void showLoadingProgress() {
		if (mProgressBar.getVisibility() == View.GONE) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 隐藏进度
	 */
	private void hideLoadingProgress() {
		if (mProgressBar.getVisibility() == View.VISIBLE) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	private void updataUI(WebView view) {
		button_back.setEnabled(view.canGoBack());
		button_forword.setEnabled(view.canGoForward());
	}

	public void onReceivedError() {
		// showLoading();
		// loading_tip_tv.setText(R.string.loading_errors_hint);
		hideLoadingProgress();
	}

	/**
	 * 关闭按钮
	 */
	public void onCloseClick() {
		mWebView.stopLoading();
		if (mWebView.canGoBack()) {
			mWebView.goBack();
			titleList.remove(0);
			tvHeadTitle.setText(titleList.get(0));
		}
		else {
			finish();
			overridePendingTransition(R.anim.base_activity_right_in, R.anim.base_activity_right_out);
		}

	}

	/**
	 * 刷新按钮
	 */
	public void onReloadUrlClick() {
		mProgressBar.setVisibility(View.VISIBLE);
		mProgressBar.setProgress(0);
		if (NetUtil.isNetworkAvailable(this)) {
			mWebView.reload();
			// if (loadingParentView.getVisibility() == View.VISIBLE) {
			// loading_tip_tv.setText(R.string.loading_progress_hint);
			// }
		}
		else {
			ToastMsg.showToastMsg(this, "当前网络不可用");
		}
	}

	/**
	 * @description：后退按钮
	 * @author samy
	 * @date 2014年8月14日 下午2:19:07
	 */
	public void onBackwordUrlClick() {
		mWebView.stopLoading();
		if (mWebView.canGoBack()) {
			mWebView.goBack();
			titleList.remove(0);
			tvHeadTitle.setText(titleList.get(0));
		}
	}

	/**
	 * 前进按钮
	 */
	public void onForwordUrlClick() {
		mWebView.stopLoading();
		if (mWebView.canGoForward()) {
			mWebView.goForward();
		}
	}

	private void hideBuiltInZoomControls(WebView view) {
		if (Build.VERSION.SDK_INT < 11) {
			try {
				Field field = WebView.class.getDeclaredField("mZoomButtonsController");
				field.setAccessible(true);
				ZoomButtonsController zoomCtrl = new ZoomButtonsController(view);
				zoomCtrl.getZoomControls().setVisibility(View.GONE);
				field.set(view, zoomCtrl);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				WebSettings settings = view.getSettings();
				Method method = WebSettings.class.getMethod("setDisplayZoomControls", boolean.class);
				method.invoke(settings, false);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
