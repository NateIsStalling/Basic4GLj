package com.basic4gl.library.desktopgl;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.opengl.GL11.glDeleteLists;

import com.basic4gl.runtime.util.IntHandleResources;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores OpenGL display lists handles
 */
public class DisplayListResourceStore extends IntHandleResources {

// Maps base to count
private Map<Integer, Integer> countMap = new HashMap<>();

protected void deleteHandle(int handle) {
	glDeleteLists(handle, countMap.get(handle));
}

public void clear() {
	super.clear();
	countMap.clear();
}

public void store(int handle, int count) {
	if (!isHandleValid(handle)
		|| countMap.get(handle) < count) { // Not already stored, or new value covers a bigger range
	super.addHandle(handle);
	countMap.put(handle, count);
	}
}

int getCount(int base) {
	assertTrue(isHandleValid(base));
	return countMap.get(base);
}
}
