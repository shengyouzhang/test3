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

import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * EventBus is a central publish/subscribe event system for Android. Events are posted ({@link #post(Object)}) to the
 * bus, which delivers it to subscribers that have a matching handler method for the event type. To receive events,
 * subscribers must register themselves to the bus using {@link #register(Object)}. Once registered,
 * subscribers receive events until {@link #unregister(Object)} is called. By convention, event handling methods must
 * be named "onEvent", be public, return nothing (void), and have exactly one parameter (the event).
 * 总结一下：register会把当前类中匹配的方法，存入一个map，而post会根据实参去map查找进行反射调用。分析这么久，一句话就说完了~~
 * 其实不用发布者，订阅者，事件，总线这几个词或许更好理解;
 * 以后大家问了EventBus，可以说，
 * 就是在一个单例内部维持着一个map对象存储了一堆的方法；post无非就是根据参数去查找方法，进行反射调用。
 * 主要功能是替代Intent,Handler,BroadCast在Fragment，Activity，Service，线程之间传递消息.优点是开销小，代码更优雅。以及将发送者和接收者解耦。
 */
public class EventBus {

	/** Log tag, apps may override it. */
	public static String TAG = "Event";

	static volatile EventBus defaultInstance;

	private static final EventBusBuilder DEFAULT_BUILDER = new EventBusBuilder();
	private static final Map<Class<?>, List<Class<?>>> eventTypesCache = new HashMap<Class<?>, List<Class<?>>>();
	/** EventType -> List<Subscription>，事件到订阅对象之间的映射 */
	private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
	/** Subscriber -> List<EventType>，订阅源到它订阅的的所有事件类型的映射 */
	private final Map<Object, List<Class<?>>> typesBySubscriber;
	/** stickEvent事件，后面会看到 */
	private final Map<Class<?>, Object> stickyEvents;
	/** EventType -> List<? extends EventType>，事件到它的父事件列表的映射。即缓存一个类的所有父类 */
	private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
		@Override
		protected PostingThreadState initialValue() {
			return new PostingThreadState();
		}
	};

	private final HandlerPoster mainThreadPoster;
	private final BackgroundPoster backgroundPoster;
	private final AsyncPoster asyncPoster;
	private final SubscriberMethodFinder subscriberMethodFinder;
	private final ExecutorService executorService;

	private final boolean throwSubscriberException;
	private final boolean logSubscriberExceptions;
	private final boolean logNoSubscriberMessages;
	private final boolean sendSubscriberExceptionEvent;
	private final boolean sendNoSubscriberEvent;
	private final boolean eventInheritance;

	/** Convenience singleton for apps using a process-wide EventBus instance. */
	public static EventBus getDefault() {
		if (defaultInstance == null) {
			synchronized (EventBus.class) {
				if (defaultInstance == null) {
					defaultInstance = new EventBus();
				}
			}
		}
		return defaultInstance;
	}

	public static EventBusBuilder builder() {
		return new EventBusBuilder();
	}

	/** For unit test primarily. */
	public static void clearCaches() {
		SubscriberMethodFinder.clearCaches();
		eventTypesCache.clear();
	}

	/**
	 * Creates a new EventBus instance; each instance is a separate scope in which events are delivered. To use a
	 * central bus, consider {@link #getDefault()}.
	 */
	public EventBus() {
		this(DEFAULT_BUILDER);
	}

	EventBus(EventBusBuilder builder) {
		subscriptionsByEventType = new HashMap<Class<?>, CopyOnWriteArrayList<Subscription>>();
		typesBySubscriber = new HashMap<Object, List<Class<?>>>();
		stickyEvents = new ConcurrentHashMap<Class<?>, Object>();
		mainThreadPoster = new HandlerPoster(this, Looper.getMainLooper(), 10);
		backgroundPoster = new BackgroundPoster(this);
		asyncPoster = new AsyncPoster(this);
		subscriberMethodFinder = new SubscriberMethodFinder(builder.skipMethodVerificationForClasses);
		logSubscriberExceptions = builder.logSubscriberExceptions;
		logNoSubscriberMessages = builder.logNoSubscriberMessages;
		sendSubscriberExceptionEvent = builder.sendSubscriberExceptionEvent;
		sendNoSubscriberEvent = builder.sendNoSubscriberEvent;
		throwSubscriberException = builder.throwSubscriberException;
		eventInheritance = builder.eventInheritance;
		executorService = builder.executorService;
	}

	/**
	 * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Object)} once they
	 * are no longer interested in receiving events.
	 * <p/>
	 * Subscribers have event handling methods that are identified by their name, typically called "onEvent". Event handling methods must have exactly one parameter, the event. If the event handling method is to be called in a specific thread, a modifier is appended to the method name. Valid modifiers match one of the {@link ThreadMode} enums. For example, if a method is to be called in the UI/main thread by EventBus, it would be called "onEventMainThread".
	 */
	public void register(Object subscriber) {
		register(subscriber, false, 0);
	}

	/**
	 * Like {@link #register(Object)} with an additional subscriber priority to influence the order of event delivery.
	 * Within the same delivery thread ({@link ThreadMode}), higher priority subscribers will receive events before
	 * others with a lower priority. The default priority is 0. Note: the priority does *NOT* affect the order of
	 * delivery among subscribers with different {@link ThreadMode}s!
	 */
	public void register(Object subscriber, int priority) {
		register(subscriber, false, priority);
	}

	/**
	 * Like {@link #register(Object)}, but also triggers delivery of the most recent sticky event (posted with {@link #postSticky(Object)}) to the given subscriber.
	 */
	public void registerSticky(Object subscriber) {
		register(subscriber, true, 0);
	}

	/**
	 * Like {@link #register(Object, int)}, but also triggers delivery of the most recent sticky event (posted with {@link #postSticky(Object)}) to the given subscriber.
	 */
	public void registerSticky(Object subscriber, int priority) {
		register(subscriber, true, priority);
	}

	/**
	 * @description：
	 *               你只要记得一件事：扫描了所有的方法，把匹配的方法最终保存在subscriptionsByEventType（Map，key：eventType ； value：CopyOnWriteArrayList<Subscription> ）中；
	 *               eventType是我们方法参数的Class，Subscription中则保存着subscriber, subscriberMethod（method, threadMode, eventType）, priority；包含了执行改方法所需的一切。
	 *               register完毕，知道了EventBus如何存储我们的方法了
	 *               大致流程就是这样的，我们总结一下：
	 *               1、找到被注册者中所有的订阅方法。
	 *               2、依次遍历订阅方法，找到EventBus中eventType对应的订阅列表，然后根据当前订阅者和订阅方法创建一个新的订阅加入到订阅列表
	 *               3、找到EvnetBus中subscriber订阅的事件列表，将eventType加入到这个事件列表。
	 * @author samy
	 * @date 2014年11月25日 上午10:23:54
	 */
	private synchronized void register(Object subscriber, boolean sticky, int priority) {
		List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriber.getClass());
		for (SubscriberMethod subscriberMethod : subscriberMethods) {
			// for循环扫描到的方法，然后去调用suscribe方法
			subscribe(subscriber, subscriberMethod, sticky, priority);
		}
	}

	/**
	 * @description：　第一个参数就是订阅源，第二个参数就是用到指定方法名约定的，默认为*onEvent*开头，说默认是其实是可以通过参数修改的，但前面说了，方法已被废弃，最好不要用。第三个参数表示是否是*Sticky Event*，第4个参数是优先级
	 * @author samy
	 * @date 2014年11月24日 下午5:43:12
	 */
	// Must be called in synchronized block
	private void subscribe(Object subscriber, SubscriberMethod subscriberMethod, boolean sticky, int priority) {
		// 根据subscriberMethod.eventType，去subscriptionsByEventType去查找一个CopyOnWriteArrayList<Subscription> ，如果没有则创建。
		// 从订阅方法中拿到订阅事件的类型
		Class<?> eventType = subscriberMethod.eventType;
		// subscriptionsByEventType是个Map，key：eventType ； value：CopyOnWriteArrayList<Subscription> ；这个Map其实就是EventBus存储方法的地方，一定要记住！
		// 通过订阅事件类型，找到所有的订阅（Subscription）,订阅中包含了订阅者，订阅方法
		CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
		// 创建一个新的订阅; 顺便把我们的传入的参数封装成了一个；
		Subscription newSubscription = new Subscription(subscriber, subscriberMethod, priority);
		if (subscriptions == null) {
			// 如果该事件目前没有订阅列表，那么创建并加入该订阅
			subscriptions = new CopyOnWriteArrayList<Subscription>();
			subscriptionsByEventType.put(eventType, subscriptions);
		}
		else {
			if (subscriptions.contains(newSubscription)) { throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event " + eventType); }
		}

		// 实际上，就是添加newSubscription；并且是按照优先级添加的。可以看到，优先级越高，会插到在当前List的前面。
		// Starting with EventBus 2.2 we enforced methods to be public (might change with annotations again)
		// subscriberMethod.method.setAccessible(true);
		// 根据优先级插入订阅
		int size = subscriptions.size();
		for (int i = 0; i <= size; i++) {
			if (i == size || newSubscription.priority > subscriptions.get(i).priority) {
				subscriptions.add(i, newSubscription);
				break;
			}
		}
		// 根据subscriber存储它所有的eventType ； 依然是map；key：subscriber ，value：List<eventType> ;知道就行，非核心代码，主要用于isRegister的判断。
		// 将这个订阅事件加入到订阅者的订阅事件列表中
		List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
		if (subscribedEvents == null) {
			subscribedEvents = new ArrayList<Class<?>>();
			typesBySubscriber.put(subscriber, subscribedEvents);
		}
		subscribedEvents.add(eventType);

		/**
		 * 　而这个事件是什么时候存起来的呢，同`register`与`registerSticky`一样，和`post`一起的还有一个`postSticky`函数
		 * 通过`registerSticky`可以注册Stick事件处理函数，前面我们知道了，无论是`register`还是`registerSticky`最后都会调用`Subscribe`函数
		 * 当通过`postSticky`发送一个事件时，这个类型的事件的最后一次事件会被缓存起来，当有订阅者通过`registerSticky`注册时，会把之前缓存起来的这个事件直接发送给它。
		 */
		if (sticky) {
			Object stickyEvent;
			synchronized (stickyEvents) {
				stickyEvent = stickyEvents.get(eventType);
			}
			if (stickyEvent != null) {
				// If the subscriber is trying to abort the event, it will fail (event is not tracked in posting state)
				// --> Strange corner case, which we don't take care of here.
				postToSubscription(newSubscription, stickyEvent, Looper.getMainLooper() == Looper.myLooper());
			}
		}
	}

	public synchronized boolean isRegistered(Object subscriber) {
		return typesBySubscriber.containsKey(subscriber);
	}

	/** Only updates subscriptionsByEventType, not typesBySubscriber! Caller must update typesBySubscriber. */
	private void unubscribeByEventType(Object subscriber, Class<?> eventType) {
		List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
		if (subscriptions != null) {
			int size = subscriptions.size();
			for (int i = 0; i < size; i++) {
				Subscription subscription = subscriptions.get(i);
				if (subscription.subscriber == subscriber) {
					subscription.active = false;
					subscriptions.remove(i);
					i--;
					size--;
				}
			}
		}
	}

	/** Unregisters the given subscriber from all event classes. */
	public synchronized void unregister(Object subscriber) {
		List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
		if (subscribedTypes != null) {
			for (Class<?> eventType : subscribedTypes) {
				unubscribeByEventType(subscriber, eventType);
			}
			typesBySubscriber.remove(subscriber);
		}
		else {
			Log.w(TAG, "Subscriber to unregister was not registered before: " + subscriber.getClass());
		}
	}

	/**
	 * register时，把方法存在subscriptionsByEventType；那么post肯定会去subscriptionsByEventType去取方法，然后调用
	 * 可以看到，当我们Post一个事件时，这个事件的父事件（事件类的父类的事件）也会被Post，所以如果有个事件订阅者接收Object类型的事件，那么它就可以接收到所有的事件。
	 */
	/** Posts the given event to the event bus. */
	public void post(Object event) {
		// currentPostingThreadState是一个ThreadLocal类型的，里面存储了PostingThreadState；PostingThreadState包含了一个eventQueue和一些标志位。
		// 这个EventBus中只有一个，差不多是个单例吧，具体不用细究
		PostingThreadState postingState = currentPostingThreadState.get();
		List<Object> eventQueue = postingState.eventQueue;
		// 将事件放入队列
		eventQueue.add(event);

		// 有个判断，就是防止该问题的，isPosting=true了，就不会往下走了。
		if (!postingState.isPosting) {
			// 判断当前是否是UI线程
			postingState.isMainThread = Looper.getMainLooper() == Looper.myLooper();
			postingState.isPosting = true;
			if (postingState.canceled) { throw new EventBusException("Internal error. Abort state was not reset"); }
			try {
				while (!eventQueue.isEmpty()) {
					// 遍历队列中的所有的event，调用postSingleEvent（eventQueue.remove(0), postingState）方法。
					// 分发事件
					postSingleEvent(eventQueue.remove(0), postingState);
				}
			}
			finally {
				postingState.isPosting = false;
				postingState.isMainThread = false;
			}
		}
	}

	/**
	 * Called from a subscriber's event handling method, further event delivery will be canceled. Subsequent
	 * subscribers
	 * won't receive the event. Events are usually canceled by higher priority subscribers (see {@link #register(Object, int)}). Canceling is restricted to event handling methods running in posting thread {@link ThreadMode#PostThread}.
	 */
	public void cancelEventDelivery(Object event) {
		PostingThreadState postingState = currentPostingThreadState.get();
		if (!postingState.isPosting) {
			throw new EventBusException("This method may only be called from inside event handling methods on the posting thread");
		}
		else if (event == null) {
			throw new EventBusException("Event may not be null");
		}
		else if (postingState.event != event) {
			throw new EventBusException("Only the currently handled event may be aborted");
		}
		else if (postingState.subscription.subscriberMethod.threadMode != ThreadMode.PostThread) { throw new EventBusException(" event handlers may only abort the incoming event"); }

		postingState.canceled = true;
	}

	/**
	 * Posts the given event to the event bus and holds on to the event (because it is sticky). The most recent sticky
	 * event of an event's type is kept in memory for future access. This can be {@link #registerSticky(Object)} or {@link #getStickyEvent(Class)}.
	 * 和post功能类似，但是会把方法存储到stickyEvents中去；
	 */
	public void postSticky(Object event) {
		synchronized (stickyEvents) {
			stickyEvents.put(event.getClass(), event);
		}
		// Should be posted after it is putted, in case the subscriber wants to remove immediately
		post(event);
	}

	/**
	 * Gets the most recent sticky event for the given type.
	 * 
	 * @see #postSticky(Object)
	 */
	public <T> T getStickyEvent(Class<T> eventType) {
		synchronized (stickyEvents) {
			return eventType.cast(stickyEvents.get(eventType));
		}
	}

	/**
	 * Remove and gets the recent sticky event for the given event type.
	 * 
	 * @see #postSticky(Object)
	 */
	public <T> T removeStickyEvent(Class<T> eventType) {
		synchronized (stickyEvents) {
			return eventType.cast(stickyEvents.remove(eventType));
		}
	}

	/**
	 * Removes the sticky event if it equals to the given event.
	 * 
	 * @return true if the events matched and the sticky event was removed.
	 */
	public boolean removeStickyEvent(Object event) {
		synchronized (stickyEvents) {
			Class<?> eventType = event.getClass();
			Object existingEvent = stickyEvents.get(eventType);
			if (event.equals(existingEvent)) {
				stickyEvents.remove(eventType);
				return true;
			}
			else {
				return false;
			}
		}
	}

	/**
	 * Removes all sticky events.
	 */
	public void removeAllStickyEvents() {
		synchronized (stickyEvents) {
			stickyEvents.clear();
		}
	}

	public boolean hasSubscriberForEvent(Class<?> eventClass) {
		List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
		if (eventTypes != null) {
			int countTypes = eventTypes.size();
			for (int h = 0; h < countTypes; h++) {
				Class<?> clazz = eventTypes.get(h);
				CopyOnWriteArrayList<Subscription> subscriptions;
				synchronized (this) {
					subscriptions = subscriptionsByEventType.get(clazz);
				}
				if (subscriptions != null && !subscriptions.isEmpty()) { return true; }
			}
		}
		return false;
	}

	/**
	 * @description：遍历队列中的所有的event，调用postSingleEvent（eventQueue.remove(0), postingState）方法。
	 * @author samy
	 * @date 2014年11月25日 上午10:30:24
	 */
	private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
		// 根据event的Class，去得到一个List<Class<?>>；其实就是得到event当前对象的Class，以及父类和接口的Class类型；主要用于匹配，比如你传入Dog extends Dog，他会把Animal也装到该List中。
		// 找到eventClass对应的事件，包含父类对应的事件和接口对应的事件
		Class<?> eventClass = event.getClass();
		boolean subscriptionFound = false;
		if (eventInheritance) {
			List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
			int countTypes = eventTypes.size();
			// 遍历所有的Class，到subscriptionsByEventType去查找subscriptions；
			for (int h = 0; h < countTypes; h++) {
				Class<?> clazz = eventTypes.get(h);
				// 找到订阅事件对应的订阅，这个是通过register加入的（还记得吗....）
				subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
			}
		}
		else {
			subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
		}
		// 如果没有订阅发现，那么会Post一个NoSubscriberEvent事件
		if (!subscriptionFound) {
			if (logNoSubscriberMessages) {
				Log.d(TAG, "No subscribers registered for event " + eventClass);
			}
			if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class && eventClass != SubscriberExceptionEvent.class) {
				post(new NoSubscriberEvent(this, event));
			}
		}
	}

	/**
	 * @description：
	 *               遍历每个subscription,依次去调用postToSubscription(subscription, event, postingState.isMainThread);
	 *               这个方法就是去反射执行方法了，大家还记得在register，if(sticky)时，也会去执行这个方法。
	 * @author samy
	 * @date 2014年11月25日 上午10:39:46
	 */
	private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
		CopyOnWriteArrayList<Subscription> subscriptions;
		synchronized (this) {
			subscriptions = subscriptionsByEventType.get(eventClass);
		}
		if (subscriptions != null && !subscriptions.isEmpty()) {
			for (Subscription subscription : subscriptions) {
				postingState.event = event;
				postingState.subscription = subscription;
				boolean aborted = false;
				try {
					// 对每个订阅调用该方法
					postToSubscription(subscription, event, postingState.isMainThread);
					aborted = postingState.canceled;
				}
				finally {
					postingState.event = null;
					postingState.subscription = null;
					postingState.canceled = false;
				}
				if (aborted) {
					break;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @description：
	 *               第一步根据threadMode去判断应该在哪个线程去执行该方法
	 *               第一个参数就是传入的订阅，第二个参数就是对于的分发事件，第三个参数非常关键：是否在主线程
	 * @author samy
	 * @date 2014年11月25日 上午10:42:16
	 */
	private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
		switch (subscription.subscriberMethod.threadMode) {
			case PostThread:// 如果是PostThread，直接执行
				invokeSubscriber(subscription, event);
				break;
			case MainThread:// 首先去判断当前如果是UI线程，则直接调用；否则： mainThreadPoster.enqueue(subscription, event);把当前的方法加入到队列，然后直接通过handler去发送一个消息，在handler的handleMessage中，去执行我们的方法。说白了就是通过Handler去发送消息，然后执行的。
				if (isMainThread) {
					invokeSubscriber(subscription, event);
				}
				else {
					mainThreadPoster.enqueue(subscription, event);
				}
				break;
			case BackgroundThread:// 如果当前非UI线程，则直接调用；如果是UI线程，则将任务加入到后台的一个队列，最终由Eventbus中的一个线程池去调用
				if (isMainThread) {
					backgroundPoster.enqueue(subscription, event);
				}
				else {
					invokeSubscriber(subscription, event);
				}
				break;
			case Async:// 将任务加入到后台的一个队列，最终由Eventbus中的一个线程池去调用；线程池与BackgroundThread用的是同一个。
				asyncPoster.enqueue(subscription, event);
				break;
			/**
			 * 这么说BackgroundThread和Async有什么区别呢？
			 * BackgroundThread中的任务，一个接着一个去调用，中间使用了一个布尔型变量handlerActive进行的控制。
			 * Async则会动态控制并发。
			 */
			default:
				throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
		}
	}

	/** Looks up all Class objects including super classes and interfaces. Should also work for interfaces. */
	private List<Class<?>> lookupAllEventTypes(Class<?> eventClass) {
		synchronized (eventTypesCache) {
			List<Class<?>> eventTypes = eventTypesCache.get(eventClass);
			if (eventTypes == null) {
				eventTypes = new ArrayList<Class<?>>();
				Class<?> clazz = eventClass;
				while (clazz != null) {
					eventTypes.add(clazz);
					addInterfaces(eventTypes, clazz.getInterfaces());
					clazz = clazz.getSuperclass();
				}
				eventTypesCache.put(eventClass, eventTypes);
			}
			return eventTypes;
		}
	}

	/** Recurses through super interfaces. */
	static void addInterfaces(List<Class<?>> eventTypes, Class<?>[] interfaces) {
		for (Class<?> interfaceClass : interfaces) {
			if (!eventTypes.contains(interfaceClass)) {
				eventTypes.add(interfaceClass);
				addInterfaces(eventTypes, interfaceClass.getInterfaces());
			}
		}
	}

	/**
	 * Invokes the subscriber if the subscriptions is still active. Skipping subscriptions prevents race conditions
	 * between {@link #unregister(Object)} and event delivery. Otherwise the event might be delivered after the
	 * subscriber unregistered. This is particularly important for main thread delivery and registrations bound to the
	 * live cycle of an Activity or Fragment.
	 */
	void invokeSubscriber(PendingPost pendingPost) {
		Object event = pendingPost.event;
		Subscription subscription = pendingPost.subscription;
		PendingPost.releasePendingPost(pendingPost);
		if (subscription.active) {
			invokeSubscriber(subscription, event);
		}
	}

	/**
	 * @description：直接反射调用；也就是说在当前的线程直接调用该方法；
	 * @author samy
	 * @date 2014年11月25日 上午10:41:39
	 */
	void invokeSubscriber(Subscription subscription, Object event) {
		try {
			subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
		}
		catch (InvocationTargetException e) {
			handleSubscriberException(subscription, event, e.getCause());
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException("Unexpected exception", e);
		}
	}

	private void handleSubscriberException(Subscription subscription, Object event, Throwable cause) {
		if (event instanceof SubscriberExceptionEvent) {
			if (logSubscriberExceptions) {
				// Don't send another SubscriberExceptionEvent to avoid infinite event recursion, just log
				Log.e(TAG, "SubscriberExceptionEvent subscriber " + subscription.subscriber.getClass() + " threw an exception", cause);
				SubscriberExceptionEvent exEvent = (SubscriberExceptionEvent) event;
				Log.e(TAG, "Initial event " + exEvent.causingEvent + " caused exception in " + exEvent.causingSubscriber, exEvent.throwable);
			}
		}
		else {
			if (throwSubscriberException) { throw new EventBusException("Invoking subscriber failed", cause); }
			if (logSubscriberExceptions) {
				Log.e(TAG, "Could not dispatch event: " + event.getClass() + " to subscribing class " + subscription.subscriber.getClass(), cause);
			}
			if (sendSubscriberExceptionEvent) {
				SubscriberExceptionEvent exEvent = new SubscriberExceptionEvent(this, cause, event, subscription.subscriber);
				post(exEvent);
			}
		}
	}

	/** For ThreadLocal, much faster to set (and get multiple values). */
	final static class PostingThreadState {
		final List<Object> eventQueue = new ArrayList<Object>();
		boolean isPosting;
		boolean isMainThread;
		Subscription subscription;
		Object event;
		boolean canceled;
	}

	ExecutorService getExecutorService() {
		return executorService;
	}

	// Just an idea: we could provide a callback to post() to be notified, an alternative would be events, of course...
	/* public */interface PostCallback {
		void onPostCompleted(List<SubscriberExceptionEvent> exceptionEvents);
	}

}
