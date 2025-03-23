package com.basic4gl.lib.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceCollection implements IServiceCollection {
	private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

	@Override
	public <T> void registerService(Class<T> serviceClass, T serviceInstance) {
		if (serviceClass == null || serviceInstance == null) {
			throw new IllegalArgumentException("Service class and instance cannot be null.");
		}
		services.put(serviceClass, serviceInstance);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> serviceClass) {
		return (T) services.get(serviceClass);
	}

	@Override
	public void clear() {
		services.clear();
	}
}
