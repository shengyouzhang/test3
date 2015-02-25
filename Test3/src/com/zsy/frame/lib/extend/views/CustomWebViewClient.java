package com.zsy.frame.lib.extend.views;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zsy.frame.lib.extend.ui.base.BaseWebViewAct;

/**
 * Convenient extension of WebViewClient.
 */
public class CustomWebViewClient extends WebViewClient {

	private BaseWebViewAct mMainActivity;

	public CustomWebViewClient(BaseWebViewAct mainActivity) {
		super();
		mMainActivity = mainActivity;
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		((CustomWebView) view).notifyPageFinished();
		mMainActivity.onPageFinished(view, url);

		super.onPageFinished(view, url);
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		((CustomWebView) view).notifyPageStarted();
		mMainActivity.onPageStarted(view, url);
		super.onPageStarted(view, url, favicon);
	}

	@TargetApi(8)
	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		super.onReceivedError(view, errorCode, description, failingUrl);
		view.stopLoading();
		view.clearView();
		mMainActivity.onReceivedError();
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		((CustomWebView) view).resetLoadedUrl();
		if (url != null && !url.startsWith("about:blank")) // 控制加载url
			view.loadUrl(url);
		mMainActivity.shouldOverrideUrlLoading(view, url);
		return false;
	}

	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		handler.proceed();
	}
}
