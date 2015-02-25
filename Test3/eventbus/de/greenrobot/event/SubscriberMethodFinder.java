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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SubscriberMethodFinder {
	private static final String ON_EVENT_METHOD_NAME = "onEvent";

	/*
	 * In newer class files, compilers may add methods. Those are called bridge or synthetic methods.
	 * EventBus must ignore both. There modifiers are not public but defined in the Java class file format:
	 * http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.6-200-A.1
	 */
	private static final int BRIDGE = 0x40;
	private static final int SYNTHETIC = 0x1000;

	private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;
	private static final Map<String, List<SubscriberMethod>> methodCache = new HashMap<String, List<SubscriberMethod>>();

	private final Map<Class<?>, Class<?>> skipMethodVerificationForClasses;

	SubscriberMethodFinder(List<Class<?>> skipMethodVerificationForClassesList) {
		skipMethodVerificationForClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
		if (skipMethodVerificationForClassesList != null) {
			for (Class<?> clazz : skipMethodVerificationForClassesList) {
				skipMethodVerificationForClasses.put(clazz, clazz);
			}
		}
	}

	List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
		// 通过订阅者类名+"."+"onEvent"创建一个key
		String key = subscriberClass.getName();
		List<SubscriberMethod> subscriberMethods;
		synchronized (methodCache) {
			// 判断是否有缓存，有缓存直接返回缓存
			subscriberMethods = methodCache.get(key);
		}
		// 第一次进来subscriberMethods肯定是Null
		if (subscriberMethods != null) { return subscriberMethods; }
		subscriberMethods = new ArrayList<SubscriberMethod>();
		Class<?> clazz = subscriberClass;
		HashSet<String> eventTypesFound = new HashSet<String>();
		StringBuilder methodKeyBuilder = new StringBuilder();
		while (clazz != null) {
			String name = clazz.getName();
			// 过滤掉系统类
			if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
				// Skip system classes, this just degrades performance
				break;
			}

			// 通过反射，获取到订阅者的所有方法
			// Starting with EventBus 2.2 we enforced methods to be public (might change with annotations again)
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				// 只找以onEvent开头的方法 判断了是否以onEvent开头，是否是public且非static和abstract方法，是否是一个参数。如果都符合，才进入封装的部分
				// 判断订阅者是否是public的,并且是否有修饰符，看来订阅者只能是public的，并且不能被final，static等修饰
				if (methodName.startsWith(ON_EVENT_METHOD_NAME)) {
					int modifiers = method.getModifiers();
					if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
						// 获得订阅函数的参数
						Class<?>[] parameterTypes = method.getParameterTypes();
						// 看了参数的个数只能是1个
						if (parameterTypes.length == 1) {
							// 获取onEvent后面的部分
							String modifierString = methodName.substring(ON_EVENT_METHOD_NAME.length());
							ThreadMode threadMode;
							// 根据方法的后缀;记录线程模型为PostThread,意义就是发布事件和接收事件在同一个线程执行，详细可以参考我对于四个订阅函数不同点分析
							if (modifierString.length() == 0) { // 订阅函数为onEvnet
								threadMode = ThreadMode.PostThread;
							}
							else if (modifierString.equals("MainThread")) {// 对应onEventMainThread
								threadMode = ThreadMode.MainThread;
							}
							else if (modifierString.equals("BackgroundThread")) {// 对应onEventBackgrondThread
								threadMode = ThreadMode.BackgroundThread;
							}
							else if (modifierString.equals("Async")) { // 对应onEventAsync
								threadMode = ThreadMode.Async;
							}
							else {
								if (skipMethodVerificationForClasses.containsKey(clazz)) {
									continue;
								}
								else {
									throw new EventBusException("Illegal onEvent method, check for typos: " + method);
								}
							}
							// 获取参数类型，其实就是接收事件的类型
							Class<?> eventType = parameterTypes[0];
							methodKeyBuilder.setLength(0);
							methodKeyBuilder.append(methodName);
							methodKeyBuilder.append('>').append(eventType.getName());
							String methodKey = methodKeyBuilder.toString();
							if (eventTypesFound.add(methodKey)) {
								// Only add if not already found in a sub class
								// 封装一个订阅方法对象，这个对象包含Method对象，threadMode对象，eventType对象
								subscriberMethods.add(new SubscriberMethod(method, threadMode, eventType));
							}
						}
					}
					else if (!skipMethodVerificationForClasses.containsKey(clazz)) {
						Log.d(EventBus.TAG, "Skipping method (not public, static or abstract): " + clazz + "." + methodName);
					}
				}
			}
			// 可以看到，会扫描所有的父类，不仅仅是当前类;看了还会遍历父类的订阅函数
			clazz = clazz.getSuperclass();
		}
		 // 最后加入缓存，第二次使用直接从缓存拿
		if (subscriberMethods.isEmpty()) {
			throw new EventBusException("Subscriber " + subscriberClass + " has no public methods called " + ON_EVENT_METHOD_NAME);
		}
		else {
			synchronized (methodCache) {
				methodCache.put(key, subscriberMethods);
			}
			return subscriberMethods;
		}
	}

	static void clearCaches() {
		synchronized (methodCache) {
			methodCache.clear();
		}
	}

}
