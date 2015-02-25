package com.zsy.frame.lib.extend.utils;

import java.util.ArrayList;

import android.content.Context;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zsy.frame.lib.extend.config.BaseConstant;
import com.zsy.frame.lib.extend.views.ToastMsg;
import com.zsy.frame.lib.helps.SYPrefer;

/**
 * @description：简单定位工具
 *                     LocationClient 定位SDK的核心类,LocationClient类必须在主线程中声明。需要Context类型的参数。
 *                     Context需要时全进程有效的context,推荐用getApplicationConext获取全进程有效的context
 * @author samy 2014年7月28日 上午11:33:52
 * @date 2014年5月28日 上午10:33:52
 */
public class LocationUtil {
	private static final String TAG = "LocationUtil";

	private Context mContext = null;
	private static LocationUtil self = null;
	private LocationClient mLocationClient = null;
	private MyBDLocationListener mBDLocationListener = null;
	private GeocodeSearch geocoderSearch = null;
	private ArrayList<OnLocationUpdateListener> mListeners = new ArrayList<OnLocationUpdateListener>();
	private ArrayList<OnDetailLocationUpdateListener> mDListeners = new ArrayList<OnDetailLocationUpdateListener>();

	private LocationUtil(Context c) {
		mContext = c;
	}

	public static LocationUtil getInstance(Context c) {
		if (self == null) {
			self = new LocationUtil(c);
		}
		return self;
	}

	public static LocationUtil getInstance(Context c, LocationClient locationClient) {
		if (self == null) {
			self = new LocationUtil(c);
			self.mLocationClient = locationClient;

			initLocationData(locationClient);

			self.mBDLocationListener = self.new MyBDLocationListener();
			self.mLocationClient.registerLocationListener(self.mBDLocationListener);
		}
		return self;
	}

	private static void initLocationData(LocationClient locationClient) {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("gcj02");// 返回的定位结果是百度经纬度，默认值gcj02,db09ll,bd09
		option.setScanSpan(10000);// 设置发起定位请求的间隔时间为10000ms
		option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
		locationClient.setLocOption(option);// 装在定位的属性
	}

	public void Addlistener(OnLocationUpdateListener l) {
		if (mListeners.contains(l) == false) {
			mListeners.add(l);
			if (mListeners.size() == 1) {
				start();
			}
		}
	}

	public void Removelistener(OnLocationUpdateListener l) {
		if (mListeners.contains(l)) {
			mListeners.remove(l);
			if (mListeners.size() <= 0) {
				stop();
			}
		}
	}

	public void Clearlisteners() {
		if (mListeners.size() > 0) {
			stop();
			mListeners.clear();
		}
	}

	public void Addlistener(OnDetailLocationUpdateListener l) {
		if (mDListeners.contains(l) == false) {
			mDListeners.add(l);
			if (mDListeners.size() == 1) {
				start();
			}
		}
	}

	public void Removelistener(OnDetailLocationUpdateListener l) {
		if (mDListeners.contains(l)) {
			mDListeners.remove(l);
			if (mDListeners.size() <= 0) {
				stop();
			}
		}
	}

	public void start() {
		/*
		 * if (null == mLocationClient) { // mLocationClient = new LocationClient(getApplicationContext()); // //实例化定位服务，LocationClient类必须在主线程中声明 mLocationClient = new LocationClient(mContext);
		 * LocationClientOption option = new LocationClientOption(); option.setOpenGps(true);// 打开gps // option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式，有三种 option.setCoorType("gcj02");//
		 * 返回的定位结果是百度经纬度，默认值gcj02,db09ll,bd09 option.setScanSpan(10000);// 设置发起定位请求的间隔时间为10000ms option.setIsNeedAddress(true); // 返回的定位结果包含地址信息 // option.setNeedDeviceDirect(true); //
		 * 返回的定位结果包含手机机头的方向 mLocationClient.setLocOption(option);// 装在定位的属性 }
		 */
		mLocationClient.start();// 启动定位sdk
		if (null != mLocationClient && mLocationClient.isStarted()) {// 发起定位请求
			mLocationClient.requestLocation();
		}
		else {
			// Toast.makeText(mContext, "正在定位，请稍候…", Toast.LENGTH_SHORT).show();
		}

	}

	private void stop() {
		if (null != mLocationClient) {
			mLocationClient.stop();
		}
	}

	private void onLocationChanged(double fLatitude, double fLongitude, String sAddress) {
		for (int i = 0; i < mListeners.size(); i++) {
			// 触发回调方法；
			mListeners.get(i).onLocationChanged(fLatitude, fLongitude, sAddress);
		}
	}

	public void onError(int errCode) {
		for (int i = 0; i < mListeners.size(); i++) {
			mListeners.get(i).onError(errCode);
		}
	}

	private void onLocationChanged(double fLatitude, double fLongitude, RegeocodeAddress sAddress) {
		for (int i = 0; i < mDListeners.size(); i++) {
			mDListeners.get(i).onLocationChanged(fLatitude, fLongitude, sAddress);
		}
	}

	/**
	 * 百度定位返回errCode值：
	 * 61 ： GPS定位结果
	 * 62 ： 扫描整合定位依据失败。此时定位结果无效。
	 * 63 ： 网络异常，没有成功向服务器发起请求。此时定位结果无效。
	 * 65 ： 定位缓存的结果。
	 * 66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果
	 * 67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果
	 * 68 ： 网络连接失败时，查找本地离线定位时对应的返回结果
	 * 161： 表示网络定位结果
	 * 162~167： 服务端定位失败。
	 * 高德错误码：
	 * 0 正常
	 * 21 IO 操作异常
	 * 22 连接异常
	 * 23 连接超时
	 * 24 无效的参数
	 * 25 空指针异常
	 * 26 url 异常
	 * 27 未知主机
	 * 28 服务器连接失败
	 * 29 协议解析错误
	 * 30 http 连接失败
	 * 31 未知的错误
	 * 32 key 鉴权失败
	 * 
	 * @author fanxing
	 */
	private class MyBDLocationListener implements BDLocationListener {// 注册监听函数
		@Override
		public void onReceiveLocation(BDLocation location) {
			StringBuffer sb = new StringBuffer(256);
			sb.append("定位时间: ");
			sb.append(location.getTime());
			sb.append("\n代码: ");
			sb.append(location.getLocType());
			sb.append("\n纬度: ");
			sb.append(location.getLatitude());
			sb.append("\n经度: ");
			sb.append(location.getLongitude());
			sb.append("\n半径: ");
			location.describeContents();
			sb.append(location.getRadius());

			int errCode = location.getLocType();
			if (errCode == BDLocation.TypeGpsLocation || errCode == BDLocation.TypeNetWorkLocation) {
				if (location.getLocType() == BDLocation.TypeGpsLocation) { // 如果是GPS定位，则显示速度和卫星个数
					sb.append("\n速度: ");
					sb.append(location.getSpeed());
					sb.append("\n卫星个数: ");
					sb.append(location.getSatelliteNumber());
				}
				else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 如果是网络定位，则显示详细地址
					sb.append("\n详细地址: ");
					sb.append(location.getAddrStr());
					sb.append("\n运行商: ");
					sb.append(location.getOperators());
				}
				// Toast.makeText(mContext, "--百度定位的详细信息：" + sb.toString(), Toast.LENGTH_SHORT).show();

				final double lat = location.getLatitude();
				final double lng = location.getLongitude();
				SYPrefer.getInstance().setString(BaseConstant.CURRENT_LATITUDE, lat + "");
				SYPrefer.getInstance().setString(BaseConstant.CURRENT_LONGITUDE, lng + "");

				// 解析地址并显示 高德geocoder；是耗时的
				RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(lat, lng), 1, GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
				if (geocoderSearch == null) {
					geocoderSearch = new GeocodeSearch(mContext);
					geocoderSearch.setOnGeocodeSearchListener(new OnGeocodeSearchListener() {
						@Override
						public void onRegeocodeSearched(RegeocodeResult result, int rCode) {// 逆地理编码回调
							if (rCode == 0) {
								if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
									RegeocodeAddress regeocodeAddress = result.getRegeocodeAddress();
									String addr = regeocodeAddress.getFormatAddress();
									addr = addr.replace(" ", "").replace("　", "");// 半角和全角空格
									String province = regeocodeAddress.getProvince();// 省
									String city = regeocodeAddress.getCity();// 市
									String district = regeocodeAddress.getDistrict();// 区

									SYPrefer.getInstance().setString(BaseConstant.ADDRESS_LOCATION, addr);

									// Toast.makeText(mContext, "-- 高德onRegeocodeSearched->" + addr, Toast.LENGTH_SHORT)
									// .show();
									onLocationChanged(lat, lng, addr);
									onLocationChanged(lat, lng, result.getRegeocodeAddress());
									return;
								}
								else {
									onError(-9999);
								}
							}
							else {
								ToastMsg.showToastMsg(mContext, "高德返回错位定位码：" + rCode);
								onError(rCode);
							}
						}

						@Override
						public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
						}
					});
				}
				geocoderSearch.getFromLocationAsyn(query);
			}
			else {
				onError(errCode);
				ToastMsg.showToastMsg(mContext, "百度返回错位定位码：" + errCode);
			}
		}
	}

	/**
	 * @description：自定义一个LocationListener
	 * @date 2014年7月28日 上午11:37:52
	 */
	public static interface OnLocationUpdateListener {
		// gcj02坐标；sAddress详细地址; // 位置发生改变时调用
		public void onLocationChanged(double fLatitude, double fLongitude, String sAddress);

		public void onError(int errCode);
	}

	/**
	 * @description：灵活回调接口
	 * @author samy
	 * @date 2014年8月27日 下午4:46:37
	 */
	public static interface OnDetailLocationUpdateListener {
		public void onLocationChanged(double fLatitude, double fLongitude, RegeocodeAddress address);
	}
}
