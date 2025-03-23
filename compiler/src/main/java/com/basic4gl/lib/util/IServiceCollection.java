package com.basic4gl.lib.util;

public interface IServiceCollection {
	<T> void registerService(Class<T> serviceClass, T serviceInstance);

	<T> T getService(Class<T> serviceClass);

	void clear();
}
