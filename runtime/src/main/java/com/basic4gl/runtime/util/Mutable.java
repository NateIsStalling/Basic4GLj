package com.basic4gl.runtime.util;
/**
 * 
 * @author Nate
 *	Wrapper class to pass objects and primitives by reference 
 * @param <T> Class of object or primitive
 */
public class Mutable<T> {
	private T mValue;
	public Mutable(T value){ mValue = value;}
	public void set(T value){ mValue = value;}
	public T get(){return mValue;};
}
