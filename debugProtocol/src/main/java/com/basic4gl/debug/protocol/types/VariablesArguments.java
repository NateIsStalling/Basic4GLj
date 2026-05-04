package com.basic4gl.debug.protocol.types;

public class VariablesArguments {
	/**
	 * The variable for which children should be returned.
	 */
	public int variablesReference;

	/**
	 * Optional offset for paging child variables.
	 */
	public Integer start;

	/**
	 * Optional max number of child variables to return.
	 */
	public Integer count;
}
