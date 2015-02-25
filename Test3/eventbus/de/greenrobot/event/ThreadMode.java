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

/**
 * Each event handler method has a thread mode, which determines in which thread the method is to be called by EventBus.
 * EventBus takes care of threading independently from the posting thread.
 * 
 * @see EventBus#register(Object)
 * @author Markus
 */
public enum ThreadMode {
	/**
	 * Subscriber will be called in the same thread, which is posting the event. This is the default. Event delivery
	 * implies the least overhead because it avoids thread switching completely. Thus this is the recommended mode for
	 * simple tasks that are known to complete is a very short time without requiring the main thread. Event handlers
	 * using this mode must return quickly to avoid blocking the posting thread, which may be the main thread.
	 */
	/**
	 * onEvent:如果使用onEvent作为订阅函数，那么该事件在哪个线程发布出来的，onEvent就会在这个线程中运行，也就是说发布事件和接收事件线程在同一个线程。使用这个方法时，在onEvent方法中不能执行耗时操作，如果执行耗时操作容易导致事件分发延迟。
	 */
	PostThread,

	/**
	 * Subscriber will be called in Android's main thread (sometimes referred to as UI thread). If the posting thread is
	 * the main thread, event handler methods will be called directly. Event handlers using this mode must return
	 * quickly to avoid blocking the main thread.
	 */
	/**
	 * onEventMainThread:如果使用onEventMainThread作为订阅函数，那么不论事件是在哪个线程中发布出来的，onEventMainThread都会在UI线程中执行，接收事件就会在UI线程中运行，这个在Android中是非常有用的，因为在Android中只能在UI线程中更新UI，所以在onEvnetMainThread方法中是不能执行耗时操作的。
	 */
	MainThread,

	/**
	 * Subscriber will be called in a background thread. If posting thread is not the main thread, event handler methods
	 * will be called directly in the posting thread. If the posting thread is the main thread, EventBus uses a single
	 * background thread, that will deliver all its events sequentially. Event handlers using this mode should try to
	 * return quickly to avoid blocking the background thread.
	 */
	/**
	 * onEvnetBackground:如果使用onEventBackgrond作为订阅函数，那么如果事件是在UI线程中发布出来的，那么onEventBackground就会在子线程中运行，如果事件本来就是子线程中发布出来的，那么onEventBackground函数直接在该子线程中执行。
	 */
	BackgroundThread,

	/**
	 * Event handler methods are called in a separate thread. This is always independent from the posting thread and the
	 * main thread. Posting events never wait for event handler methods using this mode. Event handler methods should
	 * use this mode if their execution might take some time, e.g. for network access. Avoid triggering a large number
	 * of long running asynchronous handler methods at the same time to limit the number of concurrent threads. EventBus
	 * uses a thread pool to efficiently reuse threads from completed asynchronous event handler notifications.
	 */
	/**
	 * onEventAsync：使用这个函数作为订阅函数，那么无论事件在哪个线程发布，都会创建新的子线程在执行onEventAsync.
	 */
	Async
}