package com.gitonway.lee.niftynotification.lib;

/*
 * Copyright 2014 gitonway
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @description：提示View
 * @author samy
 * @date 2015-2-13 下午2:58:15
 */
public class NiftyNotificationViewAdvance {
	private static final String NULL_PARAMETERS_ARE_NOT_ACCEPTED = "Null parameters are not accepted";

	// 提示View的配置
	private Configuration configuration = null;
	// 动画效果
	private final Effects effects;
	// 所在的activity
	private Activity activity;
	/***
	 * 提示View最外层的View
	 */
	private FrameLayout notifyView;
	// 容器View
	private ViewGroup containerView;
	// 内容View
	private ViewGroup contentView;

	// 提示View的单击处理
	private View.OnClickListener onClickListener;

	/***
	 * 
	 * @param activity
	 * @param contentView
	 * @param effects
	 * @param containerView
	 * @param configuration
	 */
	private NiftyNotificationViewAdvance(Activity activity, ViewGroup contentView, Effects effects, ViewGroup containerView, Configuration configuration) {

		if ((activity == null) || (configuration == null)) { throw new IllegalArgumentException(NULL_PARAMETERS_ARE_NOT_ACCEPTED); }
		this.activity = activity;
		this.effects = effects;
		this.containerView = containerView;
		this.configuration = configuration;
		this.contentView = contentView;
	}

	/***
	 *  建立提示View
	 * @param activity 
	 * @param contentView 提示内容View
	 * @param effects 动画效果
	 * @param containerResId 容器ID，一般加到activity的布局中（用代码或xml）
	 * @param configuration 提示View配置
	 * @return
	 */
	public static NiftyNotificationViewAdvance build(Activity activity, ViewGroup contentView, Effects effects, int containerResId, Configuration configuration) {

		return new NiftyNotificationViewAdvance(activity, contentView, effects, (ViewGroup) activity.findViewById(containerResId), configuration);
	}

	/***
	 * 初始化提示View
	 */
	private void initializeNotifyView() {
		this.notifyView = initializeCroutonViewGroup();
		// TODO 修改源代码的位置：显示的布局文件，由外部来提供
		// RelativeLayout contentView = initializeContentView();
		this.notifyView.addView(contentView);
	}

	/****
	 * 生成一个最外层的容器
	 * @return
	 */
	private FrameLayout initializeCroutonViewGroup() {
		FrameLayout notifyView = new FrameLayout(this.activity);
		if (null != onClickListener) {
			notifyView.setOnClickListener(onClickListener);
		}
		// 高度为配置中的高度
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp2px(this.configuration.viewHeight));
		notifyView.setLayoutParams(lp);
		return notifyView;
	}

	/***
	 * 进入动画时间
	 * @return
	 */
	public long getInDuration() {
		return effects.getAnimator().getDuration();
	}

	/***
	 * 退出动画时间
	 * @return
	 */
	public long getOutDuration() {
		return effects.getAnimator().getDuration();
	}

	/***
	 * 显示时间（不包括进入和退出动画时间）
	 * @return
	 */
	public long getDispalyDuration() {
		return this.configuration.dispalyDuration;
	}

	/***
	 * 动画效果
	 * @return
	 */
	public Effects getEffects() {
		return effects;
	}

	/**
	 * 提示View配置
	 * @return
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	Activity getActivity() {
		return activity;
	}

	/***
	 * 是否在显示中
	 * @return
	 */
	public boolean isShowing() {
		return (null != activity) && isNotifyViewNotNull();
	}

	/**
	 * 
	 * @return
	 */
	private boolean isNotifyViewNotNull() {

		return (null != notifyView) && (null != notifyView.getParent());

	}

	/***
	 * 脱离activity
	 */
	void detachActivity() {
		activity = null;
	}

	/***
	 *  脱离容器View
	 */
	void detachViewGroup() {
		containerView = null;
	}

	/***
	 * 取得容器View
	 * @return
	 */
	ViewGroup getViewGroup() {
		return containerView;
	}

	/***
	 * 取得提示View
	 * @return
	 */
	View getView() {
		if (null == this.notifyView) {
			initializeNotifyView();
		}
		return notifyView;
	}

	/***
	 * dp to px
	 * @param dp
	 * @return
	 */
	public int dp2px(float dp) {
		final float scale = activity.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	/*******************Call these methods************************/

	/***
	 * 设置提示View的单击事件
	 * @param onClickListener
	 * @return
	 */
	public NiftyNotificationViewAdvance setOnClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
		return this;
	}

	/***
	 * 显示提示 View
	 */
	public void show() {
		show(false);
	}

	/***
	 * 显示
	 * @param isRepeat 是否重复
	 */
	public void show(boolean isRepeat) {
		ManagerAdvance.getInstance().add(this, isRepeat);
	}

	/***
	 * 隐藏
	 */
	public void hide() {
		ManagerAdvance.getInstance().removeNotify(this);
	}
}
