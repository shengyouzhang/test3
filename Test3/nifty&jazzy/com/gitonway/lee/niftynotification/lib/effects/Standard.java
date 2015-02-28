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

import com.nineoldandroids.animation.ObjectAnimator;

/**
 * @description：进入:下滑《》退出：上滑
 * @author samy
 * @date 2015-2-13 下午2:55:30
 */
public class Standard extends BaseEffect {
	@Override
	protected void setInAnimation(View view) {
		// 进入:下滑
		getAnimatorSet().playTogether(ObjectAnimator.ofFloat(view, "translationY", -view.getHeight(), 0).setDuration(mDuration)

		);
	}

	@Override
	protected void setOutAnimation(View view) {
		// 退出：上滑
		getAnimatorSet().playTogether(ObjectAnimator.ofFloat(view, "translationY", 0, -view.getHeight()).setDuration(mDuration)

		);
	}

	@Override
	protected long getAnimDuration(long duration) {
		return duration;
	}
}
