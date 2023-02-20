package com.basic4gl.runtime.util;
/**
 * Wrapper class to pass objects and primitives by reference
 * @author Nate
 * @param <T> Class of object or primitive
 */
public class Mutable<T> {
	private T value;
	public Mutable(T value){ this.value = value;}
	public void set(T value){ this.value = value;}
	public T get(){return value;}
}
