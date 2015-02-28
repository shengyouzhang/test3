package com.gitonway.lee.niftynotification.lib.effects;

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

import android.view.View;

import com.gitonway.lee.niftynotification.lib.Configuration;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.view.ViewHelper;

/**
 * @description：动画效果基础类
 * @author samy
 * @date 2015-2-13 下午2:57:35
 */
public abstract class BaseEffect {

	// 动画时间
	public static final int DURATION;

	static {
		DURATION = Configuration.ANIM_DURATION;
	}

	// 动画时间
	public long mDuration = DURATION;

	private AnimatorSet mAnimatorSet;
	// 代码块
	{
		mAnimatorSet = new AnimatorSet();
	}

	/***
	 * 设置进入动画
	 * @param view
	 */
	protected abstract void setInAnimation(View view);

	/***
	 * 设置退出动画
	 * @param view
	 */
	protected abstract void setOutAnimation(View view);

	/***
	 * 得到动画时间
	 * @param duration
	 * @return
	 */
	protected abstract long getAnimDuration(long duration);

	/***
	 * 执行进入动画
	 * @param view
	 */
	public void in(View view) {
		reset(view);
		setInAnimation(view);
		mAnimatorSet.start();
	}

	/***
	 * 执行退出动画
	 * @param view
	 */
	public void out(View view) {
		reset(view);
		setOutAnimation(view);
		mAnimatorSet.start();
	}

	/***
	 * 重置View
	 * @param view
	 */
	public void reset(View view) {
		// 设置动画的中心轴为: view的中心
		ViewHelper.setPivotX(view, view.getWidth() / 2.0f);
		ViewHelper.setPivotY(view, view.getHeight() / 2.0f);
	}

	/***
	 * 设置动画时间
	 * @param duration
	 * @return
	 */
	public BaseEffect setDuration(long duration) {
		this.mDuration = duration;
		return this;
	}

	/***
	 * 动画时间
	 * @return
	 */
	public long getDuration() {
		return getAnimDuration(mDuration);
	}

	/***
	 * 动画集合
	 * @return
	 */
	public AnimatorSet getAnimatorSet() {
		return mAnimatorSet;
	}

}
