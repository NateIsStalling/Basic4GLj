package com.basic4gl.runtime.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Nate on 2/18/2015.
 */
public interface Streamable {
    public boolean StreamIn(DataInputStream stream) throws IOException;
    public void StreamOut(DataOutputStream stream) throws IOException;
}
