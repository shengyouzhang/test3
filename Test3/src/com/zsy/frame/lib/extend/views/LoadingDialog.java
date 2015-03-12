package com.zsy.frame.lib.extend.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsy.frame.lib.extend.R;

/**
 * @description：自定义加载对话框
 * @author samy
 * @date 2015-1-23 上午12:26:35
 */
public class LoadingDialog extends Dialog {
	private static final int CHANGE_TITLE_WHAT = 1;
	private static final int CHNAGE_TITLE_DELAYMILLIS = 300;
	private static final int MAX_SUFFIX_NUMBER = 3;
	private static final char SUFFIX = '.';

	private ImageView iv_route;
	private TextView detail_tv;
	private TextView tv_point;
	private RotateAnimation mAnim;

	private boolean isShowIcon = false;

	private Handler handler = new Handler() {
		private int num = 0;

		/**
		 * 省略号显示
		 */
		public void handleMessage(android.os.Message msg) {
			if (msg.what == CHANGE_TITLE_WHAT) {
				StringBuilder builder = new StringBuilder();
				if (num >= MAX_SUFFIX_NUMBER) {
					num = 0;
				}
				num++;
				for (int i = 0; i < num; i++) {
					builder.append(SUFFIX);
				}
				tv_point.setText(builder.toString());
				if (isShowing()) {
					handler.sendEmptyMessageDelayed(CHANGE_TITLE_WHAT, CHNAGE_TITLE_DELAYMILLIS);
				}
				else {
					num = 0;
				}
			}
		};
	};

	public LoadingDialog(Context context) {
		super(context, R.style.Dialog_bocop);
		init();
	}

	public LoadingDialog(Context context, boolean isShowIcon) {
		super(context, R.style.Dialog_bocop);
		this.isShowIcon = isShowIcon;
		init();
	}

	private void init() {
		setContentView(R.layout.base_common_dialog_loading_layout);
		// 是否显示加载的Icon
		findViewById(R.id.app_loading_ic_tv).setVisibility(isShowIcon ? View.VISIBLE : View.GONE);
		iv_route = (ImageView) findViewById(R.id.route_iv);
		detail_tv = (TextView) findViewById(R.id.detail_tv);
		tv_point = (TextView) findViewById(R.id.point_tv);
		initAnim();
		getWindow().setWindowAnimations(R.anim.base_alpha_in);
	}

	/**
	 *  RotateAnimation类：旋转变化动画类
	RotateAnimation类是Android系统中的旋转变化动画类，用于控制View对象的旋转动作，该类继承于Animation类。RotateAnimation类中的很多方法都与Animation类一致，该类中最常用的方法便是RotateAnimation构造方法。
	【基本语法】public RotateAnimation (float fromDegrees, float toDegrees, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue)
	参数说明
	fromDegrees：旋转的开始角度。
	toDegrees：旋转的结束角度。
	pivotXType：X轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
	pivotXValue：X坐标的伸缩值。
	pivotYType：Y轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
	pivotYValue：Y坐标的伸缩值。
	
	
	Android RotateAnimation详解
	其他构造器的旋转也可参考这副图。
	RotateAnimation旋转坐标系为以旋转点为坐标系(0,0)点。x轴为0度，顺时针方向旋转一定的角度。
	1.RotateAnimation(fromDegrees, toDegrees) [默认以View左上角顶点为旋转点]。
	X轴顺时针转动到fromDegrees为旋转的起始点，
	X轴顺时针转动到toDegrees为旋转的起始点。
	如fromDegrees=0，toDegrees=90；为左上角顶点为旋转点。0度为起始点，90度为终点。进行旋转，旋转了90度
	如fromDegrees=60，toDegrees=90；为左上角顶点为旋转点。60度为起始点，90度为终点。进行旋转，旋转了90-60=30度

	2.RotateAnimation(float fromDegrees, float toDegrees, float pivotX, float pivotY)
	(pivotX,pivotY)为旋转点。pivotX为距离左侧的偏移量，pivotY为距离顶部的偏移量。即为相对于View左上角(0,0)的坐标点。
	如View width=100px,height=100px
	RotateAnimation(0,10,100,100);则以右下角顶点为旋转点，从原始位置顺时针旋转10度
	RotateAnimation(0,90,50,50);则以View的中心点为旋转点，旋转90度

	3.RotateAnimation(fromDegrees, toDegrees, pivotXType, pivotXValue, pivotYType, pivotYValue)
	pivotXType, pivotXValue, pivotYType, pivotYValue  旋转点类型及其值。
	Animation.ABSOLUTE为绝对值 其他为百分比。这个和平移动画的一样，不了解可以去那看
	如RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 按中心点旋转90度
	效果和2例中的RotateAnimation(0,90,50,50);则以View的中心点为旋转点，旋转90度 。效果一样

	new RotateAnimation(0, 180, centerX,centerY);
	第一个参数表示动画的起始角度，第二个参数表示动画的结束角度，第三个表示动画的旋转中心x轴，第四个表示动画旋转中心y轴。
	rotateAnimation.setDuration(1000 * 20);
	表动画持续20s。
	rotateAnimation.setFillAfter(true);
	ture表示动画结束后停留在动画的最后位置，false表示动画结束后回到初始位置，默认为false。
	mView.startAnimation(rotateAnimation);
	表示在mView中启动动画。 
	
	 * @description：
	 * @author samy
	 * @date 2015-3-12 下午4:54:23
	 */
	private void initAnim() {
		// mAnim = new RotateAnimation(360, 0, Animation.RESTART, 0.5f, Animation.RESTART, 0.5f);
		mAnim = new RotateAnimation(0, 360, Animation.RESTART, 0.5f, Animation.RESTART, 0.5f);
		mAnim.setDuration(2000);
		mAnim.setRepeatCount(Animation.INFINITE);
		// mAnim.setFillAfter(true);
		mAnim.setRepeatMode(Animation.RESTART);
		mAnim.setStartTime(Animation.START_ON_FIRST_FRAME);
	}

	@Override
	public void show() {
		iv_route.startAnimation(mAnim);
		handler.sendEmptyMessage(CHANGE_TITLE_WHAT);
		super.show();
	}

	@Override
	public void dismiss() {
		mAnim.cancel();
		super.dismiss();
	}

	@Override
	public void setTitle(CharSequence title) {
		if (TextUtils.isEmpty(title)) {
			detail_tv.setText("正在加载");
		}
		else {
			detail_tv.setText(title);
		}
	}

	@Override
	public void setTitle(int titleId) {
		setTitle(getContext().getString(titleId));
	}

	public static void dismissDialog(LoadingDialog loadingDialog) {
		if (null == loadingDialog) { return; }
		loadingDialog.dismiss();
	}
}
