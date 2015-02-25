package com.zsy.frame.lib.extend;

import android.os.Handler;
import android.widget.ImageView;

import com.baidu.location.LocationClient;
import com.zsy.frame.lib.SYApp;
import com.zsy.frame.lib.extend.utils.LocationUtil;
import com.zsy.frame.lib.extend.utils.LocationUtil.OnLocationUpdateListener;
import com.zsy.frame.lib.net.http.volley.app.VolleyRequestManager;
import com.zsy.frame.lib.net.http.volley.toolbox.ImageLoader.ImageListener;

public class BaseApp extends SYApp {

	public LocationClient mLocationClient;
	private boolean isLocateSuccess = false;

	OnLocationUpdateListener mOnLocationUpdateListener = new OnLocationUpdateListener() {
		@Override
		public void onLocationChanged(double fLatitude, double fLongitude, String sAddress) {
			isLocateSuccess = true;
			LocationUtil.getInstance(BaseApp.getInstance()).Removelistener(mOnLocationUpdateListener);// 停止接收定位信息
		}

		@Override
		public void onError(int errCode) {
		}
	};

	@Override
	protected void initBaseLib() {
		// 定位;后期处理方式，监听是否有定位包处理显示调用这个方法
//		mLocationClient = new LocationClient(BaseApp.getInstance());
//		requestLocationUpdates();
	}

	private void requestLocationUpdates() {
		LocationUtil.getInstance(this, mLocationClient).Addlistener(mOnLocationUpdateListener);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isLocateSuccess == false) {
					LocationUtil.getInstance(BaseApp.getInstance()).Removelistener(mOnLocationUpdateListener);// 停止接收定位信息
				}
			}
		}, 10000);
	}

	/**
	 * 重载使用volley框架加载图片
	 * @param url
	 * @param imageListener
	 */
	public static void loadImg(ImageView view, String url) {
		ImageListener imageListener = VolleyRequestManager.getImageLoader().getImageListener(view, R.drawable.ic_launcher, R.drawable.ic_launcher);
		loadImg(view, url, imageListener);
	}
}
