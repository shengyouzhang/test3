/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package cn.sharesdk.onekeyshare.theme.classic;

import java.util.ArrayList;

import android.content.res.Configuration;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import cn.sharesdk.onekeyshare.PlatformListFakeActivity;

import com.zsy.frame.lib.extend.R;

public class PlatformListPage extends PlatformListFakeActivity implements View.OnClickListener {
	private boolean finishing;

	private LinearLayout pageOutLl;
	/** 页面 */
	private FrameLayout pageFl;
	/** 宫格列表 */
	private PlatformGridView gridview;
	/** 取消按钮 */
	private Button btnCancel;
	/** 滑上来的动画 */
	private Animation animShow;
	/** 滑下去的动画 */
	private Animation animHide;
	private View bgView;

	public void onCreate() {
		super.onCreate();

		finishing = false;
		initPageView();
		initAnim();
		activity.setContentView(pageOutLl);

		// set the data for platform gridview
		gridview.setData(shareParamsMap, silent);
		gridview.setHiddenPlatforms(hiddenPlatforms);
		gridview.setCustomerLogos(customerLogos);
		gridview.setParent(this);
		btnCancel.setOnClickListener(this);

		// display gridviews
		pageFl.clearAnimation();
		pageFl.startAnimation(animShow);
	}

	private void initPageView() {
		int dp_10 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 10);
		int dp_5 = cn.sharesdk.framework.utils.R.dipToPx(getContext(), 5);

		pageOutLl = new LinearLayout(getContext());
		FrameLayout.LayoutParams pageOutLlSet = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		pageOutLl.setLayoutParams(pageOutLlSet);
		pageOutLl.setOrientation(LinearLayout.VERTICAL);
		pageOutLl.setGravity(Gravity.BOTTOM);
		pageOutLl.setBackgroundResource(R.drawable.ssdk_pic_bg_transparent);

		pageFl = new FrameLayout(getContext());

		// 宫格列表的容器，为了“下对齐”，在外部包含了一个FrameLayout
		LinearLayout gridLl = new LinearLayout(getContext()) {
			public boolean onTouchEvent(MotionEvent event) {
				return true;
			}
		};
		gridLl.setOrientation(LinearLayout.VERTICAL);

		pageFl.addView(gridLl);
		pageOutLl.addView(pageFl);

		// 宫格列表
		gridview = new PlatformGridView(getContext());
		gridview.setEditPageBackground(bgView);
		gridview.setBackgroundResource(R.drawable.base_pic_all_round_corner_white);
		LinearLayout.LayoutParams lpWg = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lpWg.setMargins(dp_10, dp_5, dp_10, dp_5);
		gridview.setLayoutParams(lpWg);
		gridview.setGravity(Gravity.CENTER);
		gridLl.addView(gridview);

		// 取消按钮
		btnCancel = new Button(getContext());
		// btnCancel.setTextColor(0xff666666);等同
		btnCancel.setTextColor(Color.parseColor("#666666"));
		btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		btnCancel.setText(getContext().getString(R.string.cancel));
		btnCancel.setPadding(0, 0, 0, 0);
		btnCancel.setBackgroundResource(R.drawable.base_pic_round_corner_gray_selector);
		LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, cn.sharesdk.framework.utils.R.dipToPx(getContext(), 45));
		lpBtn.setMargins(dp_10, dp_5, dp_10, dp_10);
		btnCancel.setLayoutParams(lpBtn);
		btnCancel.setGravity(Gravity.CENTER);
		gridLl.addView(btnCancel);
	}

	private void initAnim() {
		animShow = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
		animShow.setDuration(300);

		animHide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
		animHide.setDuration(300);
	}

	public void onConfigurationChanged(Configuration newConfig) {
		if (gridview != null) {
			gridview.onConfigurationChanged();
		}
	}

	public boolean onFinish() {
		if (finishing) { return super.onFinish(); }

		if (animHide == null) {
			finishing = true;
			return false;
		}

		finishing = true;
		animHide.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {

			}

			public void onAnimationRepeat(Animation animation) {

			}

			public void onAnimationEnd(Animation animation) {
				pageOutLl.setVisibility(View.GONE);
				finish();
			}
		});
		pageOutLl.clearAnimation();
		pageOutLl.startAnimation(animHide);
		// 中断finish操作
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.equals(pageFl) || v.equals(btnCancel)) {
			setCanceled(true);
			finish();
		}
	}

	public void onPlatformIconClick(View v, ArrayList<Object> platforms) {
		onShareButtonClick(v, platforms);
	}
}
