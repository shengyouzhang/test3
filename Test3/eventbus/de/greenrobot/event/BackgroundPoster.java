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

import android.util.Log;

/**
 * Posts events in background.
 * BackgroundPoster`其实就是一个Runnable对象，当`enqueue`时，如果这个Runnable对象当前没被执行，就将`BackgroundPoster`加入EventBus中的一个线程池中，
 * 当`BackgroundPoster`被执行时，会依次取出队列中的事件进行派发。
 * 当长时间无事件时`BackgroundPoster`所属的线程被会销毁，下次再Post事件时再创建新的线程。
 * 
 * @author Markus
 */
final class BackgroundPoster implements Runnable {

	private final PendingPostQueue queue;
	private final EventBus eventBus;

	private volatile boolean executorRunning;

	BackgroundPoster(EventBus eventBus) {
		this.eventBus = eventBus;
		queue = new PendingPostQueue();
	}

	public void enqueue(Subscription subscription, Object event) {
		PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
		synchronized (this) {
			queue.enqueue(pendingPost);
			if (!executorRunning) {
				executorRunning = true;
				eventBus.getExecutorService().execute(this);
			}
		}
	}

	@Override
	public void run() {
		try {
			try {
				while (true) {
					PendingPost pendingPost = queue.poll(1000);
					if (pendingPost == null) {
						synchronized (this) {
							// Check again, this time in synchronized
							pendingPost = queue.poll();
							if (pendingPost == null) {
								executorRunning = false;
								return;
							}
						}
					}
					eventBus.invokeSubscriber(pendingPost);
				}
			}
			catch (InterruptedException e) {
				Log.w("Event", Thread.currentThread().getName() + " was interruppted", e);
			}
		}
		finally {
			executorRunning = false;
		}
	}

}
