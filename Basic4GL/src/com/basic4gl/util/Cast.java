package com.basic4gl.util;

public class Cast {

	public static int[] toIntArray(float array[]){
		if (array == null)
	    {
	        return null; 
	    }
	    int[] output = new int[array.length];
	    for (int i = 0; i < array.length; i++)
	    {
	        output[i] = (int) array[i];
	    }
	    return output;
	}
	
	public static float[] toFloatArray(int array[]){
		if (array == null)
	    {
	        return null; 
	    }
	    float[] output = new float[array.length];
	    for (int i = 0; i < array.length; i++)
	    {
	        output[i] =  array[i];
	    }
	    return output;
	}
	
	//TODO Find where to use StringToInt
	public static int StringToInt (String s) {
	    int i;
	    String s2 = "";
	    String s3 = "";
	    // Check if it's a hex string
	    if (s.length() > 2)
	    	s2 = s.substring(0, 2);
	    if (s.length() > 3)
	    	s3 = s.substring(0, 3);
	    if (s2.equals("0x") || s2.equals("0X"))
	         i = Integer.parseInt(s.substring(2), 16);        // Strip 0x and parse as hex
	    else if (s3.equals("-0x") || s3.equals("-0X")) {
	    	i = Integer.parseInt(s.substring(3), 16);        // Strip -0x and parse as hex
	        i = -i;                                                 // Then negate
	    }
	    else
	        i = Integer.valueOf(s);                              // Parse as regular string
	    return i;
	}
}
