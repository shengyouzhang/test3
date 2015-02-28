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

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * @description：显示提示View的管理器
 * @author samy
 * @date 2015-2-13 下午2:54:35
 */
public class ManagerAdvance extends Handler {

	// 消息类型
	private static final class Messages {
		private Messages() {
		}

		public static final int DISPLAY_NOTIFICATION = 0x20140813;
		public static final int ADD_TO_VIEW = 0x20140814;
		public static final int REMOVE_NOTIFICATION = 0x20140815;
		public static final int REMOVE_NOTIFICATION_VIEW = 0x20140816;
	}

	// 单例
	private static ManagerAdvance INSTANCE;
	private final Queue<NiftyNotificationViewAdvance> notifyQueue;

	private ManagerAdvance() {
		notifyQueue = new LinkedBlockingQueue<NiftyNotificationViewAdvance>();
	}

	public static synchronized ManagerAdvance getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new ManagerAdvance();
		}

		return INSTANCE;
	}

	/***
	 * 加到队列中
	 * @param view
	 * @param isRepeat
	 */
	void add(NiftyNotificationViewAdvance view, boolean isRepeat) {
		Log.e("noti", "add view start....");
		if (notifyQueue.size() < 1 || isRepeat) {
			Log.e("noti", "add view start....start display view");
			notifyQueue.add(view);
			displayNotify();
		}

	}

	/****
	 * 计算总共显示时间
	 * @param notify
	 * @return
	 */
	private long calculateCroutonDuration(NiftyNotificationViewAdvance notify) {
		long notifyDuration = notify.getDispalyDuration();
		notifyDuration += notify.getEffects().getAnimator().getDuration();
		return notifyDuration;
	}

	/***
	 * 发送消息
	 * @param view
	 * @param messageId
	 */
	private void sendMessage(NiftyNotificationViewAdvance view, final int messageId) {
		final Message message = obtainMessage(messageId);
		message.obj = view;
		sendMessage(message);
	}

	/***
	 * 发送延时消息
	 * @param view
	 * @param messageId
	 * @param delay
	 */
	private void sendMessageDelayed(NiftyNotificationViewAdvance view, final int messageId, final long delay) {
		Message message = obtainMessage(messageId);
		message.obj = view;
		sendMessageDelayed(message, delay);
	}

	@Override
	public void handleMessage(Message msg) {
		final NiftyNotificationViewAdvance notify = (NiftyNotificationViewAdvance) msg.obj;
		if (null == notify) { return; }
		switch (msg.what) {
			case Messages.DISPLAY_NOTIFICATION: {
				displayNotify();
				break;
			}

			case Messages.ADD_TO_VIEW: {
				addNotifyToView(notify);
				break;
			}

			case Messages.REMOVE_NOTIFICATION: {
				removeNotify(notify);
				break;
			}

			case Messages.REMOVE_NOTIFICATION_VIEW: {
				removeNotifyView(notify);
				break;
			}

			default: {
				super.handleMessage(msg);
				break;
			}
		}

		super.handleMessage(msg);
	}

	/***
	 * 显示提示
	 */
	private void displayNotify() {
		if (notifyQueue.isEmpty()) { return; }
		final NiftyNotificationViewAdvance currentNotify = notifyQueue.peek();
		if (null == currentNotify.getActivity()) {
			// activity为null，删除这个提示
			notifyQueue.poll();
		}

		if (!currentNotify.isShowing()) {
			Log.e("notify", "current notify is not showing....1");
			// 没有正在显示（没有加到activity的内容View中）
			sendMessage(currentNotify, Messages.ADD_TO_VIEW);
		}
		else {
			Log.e("notify", "current notify is showing....2");
			// 之前已经加到activity的内容View中，过一段时间，显示提示
			sendMessageDelayed(currentNotify, Messages.DISPLAY_NOTIFICATION, calculateCroutonDuration(currentNotify));
		}
	}

	/***
	 * 在activity界面中加入提示View,并显示<br><br>
	 * 在activity界面中加入提示View（如果之前没有加入），
	 * 加入完成后，开始进入动画，并发送删除提示的消息
	 * @param notify
	 */
	private void addNotifyToView(final NiftyNotificationViewAdvance notify) {
		// 之前已经加到activity的内容View中
		if (notify.isShowing()) { return; }
		final View notifyView = notify.getView();

		/****
		 * TODO 解决点击 notifyView，后面遮挡的 activity内容view的控件事件也响应的情况
		 */
		if (!notifyView.isClickable()) {
			notifyView.setClickable(true);
		}

		// 如果notifyView，没有加到activity所在的界面中，则加入
		if (null == notifyView.getParent()) {
			ViewGroup.LayoutParams params = notifyView.getLayoutParams();
			if (null == params) {
				params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			}
			if (null != notify.getViewGroup()) {
				// 容器View不为空
				if (notify.getViewGroup() instanceof FrameLayout) {
					notify.getViewGroup().addView(notifyView, params);
				}
				else {
					notify.getViewGroup().addView(notifyView, 0, params);
				}

			}
			else {
				// 容器View为空
				Activity activity = notify.getActivity();
				if (null == activity || activity.isFinishing()) { return; }
				activity.addContentView(notifyView, params);
			}
		}
		notifyView.requestLayout();

		ViewTreeObserver observer = notifyView.getViewTreeObserver();
		if (null != observer) {
			observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				@TargetApi(16)
				public void onGlobalLayout() {
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
						notifyView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
					else {
						notifyView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}

					// 开始：进入动画
					notify.getEffects().getAnimator().setDuration(notify.getConfiguration().animDuration).in(notify.getView());
					// 过一段时间，删除提示
					sendMessageDelayed(notify, Messages.REMOVE_NOTIFICATION, notify.getDispalyDuration() + notify.getInDuration());
				}
			});
		}

	}

	/***
	 * 删除提示：开始退出动画，发送删除提示View的消息和显示下一个提示的消息
	 * @param notify
	 */
	protected void removeNotify(NiftyNotificationViewAdvance notify) {
		View notifyView = notify.getView();
		ViewGroup notifyParentView = (ViewGroup) notifyView.getParent();
		if (null != notifyParentView) {
			// 退出动画
			notify.getEffects().getAnimator().setDuration(notify.getConfiguration().animDuration).out(notify.getView());
			// 发送删除提示View的消息
			sendMessageDelayed(notify, Messages.REMOVE_NOTIFICATION_VIEW, notify.getOutDuration());
			// 显示下一个提示的消息
			sendMessageDelayed(notify, Messages.DISPLAY_NOTIFICATION, notify.getOutDuration());
		}
	}

	/****
	 * 从Activity中删除提示View
	 * @param notify
	 */
	protected void removeNotifyView(NiftyNotificationViewAdvance notify) {
		View notifyView = notify.getView();
		ViewGroup notifyParentView = (ViewGroup) notifyView.getParent();
		if (null != notifyParentView) {
			// 从队列中删除
			NiftyNotificationViewAdvance removed = notifyQueue.poll();
			// 从Activity中删除提示View
			notifyParentView.removeView(notifyView);
			if (null != removed) {
				// 清空引用
				removed.detachActivity();
				removed.detachViewGroup();
			}
		}
	}

	/****
	 * 清空队列中所有显示的view
	 */
	public void clearQueue() {
		if (notifyQueue != null) {
			notifyQueue.clear();
		}
	}

	/****
	 * 清空队列中指定activity的显示View
	 */
	public void clear(Activity act) {
		if (act == null) { return; }
		if (notifyQueue != null) {
			for (Iterator<NiftyNotificationViewAdvance> iterator = notifyQueue.iterator(); iterator.hasNext();) {
				NiftyNotificationViewAdvance view = iterator.next();
				if (view != null) {
					Activity viewAct = view.getActivity();
					if (viewAct == act) {
						Log.e("TEST", "Activity=" + act + " is remove notify view");
						iterator.remove();
					}
				}
			}
		}
	}

}
