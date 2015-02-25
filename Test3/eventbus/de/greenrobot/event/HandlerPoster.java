/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
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
package de.greenrobot.event;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

/**
 * @description：　`mainThreadPoster`是一个`HandlerPoster`对象，`HandlerPoster`继承自`Handler`，构造函数中接收一个`Looper`对象，当向`HandlerPoster` enqueue事件时，会像`BackgroundPoster`一样把这个事件加入队列中， 只是如果当前没在派发消息就向自身发送Message
 * @author samy
 * @date 2014年11月24日 下午6:40:25
 */
final class HandlerPoster extends Handler {

	private final PendingPostQueue queue;
	private final int maxMillisInsideHandleMessage;
	private final EventBus eventBus;
	private boolean handlerActive;

	HandlerPoster(EventBus eventBus, Looper looper, int maxMillisInsideHandleMessage) {
		super(looper);
		this.eventBus = eventBus;
		this.maxMillisInsideHandleMessage = maxMillisInsideHandleMessage;
		queue = new PendingPostQueue();
	}

	void enqueue(Subscription subscription, Object event) {
		PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
		synchronized (this) {
			queue.enqueue(pendingPost);
			if (!handlerActive) {
				handlerActive = true;
				if (!sendMessage(obtainMessage())) { throw new EventBusException("Could not send handler message"); }
			}
		}
	}

	@Override
	public void handleMessage(Message msg) {
		boolean rescheduled = false;
		try {
			long started = SystemClock.uptimeMillis();
			while (true) {
				PendingPost pendingPost = queue.poll();
				if (pendingPost == null) {
					synchronized (this) {
						// Check again, this time in synchronized
						pendingPost = queue.poll();
						if (pendingPost == null) {
							handlerActive = false;
							return;
						}
					}
				}
				eventBus.invokeSubscriber(pendingPost);
				long timeInMethod = SystemClock.uptimeMillis() - started;
				if (timeInMethod >= maxMillisInsideHandleMessage) {
					if (!sendMessage(obtainMessage())) { throw new EventBusException("Could not send handler message"); }
					rescheduled = true;
					return;
				}
			}
		}
		finally {
			handlerActive = rescheduled;
		}
	}
}