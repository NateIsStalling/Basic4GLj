package com.basic4gl.runtime.types;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * A small number of VM op-codes operate on VmValType advanced data types
 * (e.g. OP_COPY). Op-codes don't have storage space to specify an advanced
 * data type, so instead they specify an index into this set array.
 */
public class ValTypeSet implements Streamable {

  private Vector<ValType> valTypes;

  public ValTypeSet() {
    valTypes = new Vector<>();
  }

  public void clear() {
    valTypes.clear();
  }

  /**
   * Get index of type "type" in our set.
   * If type is not present, create a new one and return an index to that.
   * @param type
   * @return
   */
  public int getIndex(ValType type) {
    // Look for type
    int i;
    for (i = 0; i < valTypes.size(); i++) {
      if (valTypes.get(i).equals(type)) {
        return i;
      }
    }

    // Otherwise create new one
    i = valTypes.size();
    valTypes.add(new ValType(type));
    return i;
  }

  public ValType getValType(int index) {
    if (index >= 0 && index < valTypes.size()) {
      return valTypes.get(index);
    }
    return null;
  }

  // Streaming
  public void streamOut(DataOutputStream stream) throws IOException {

    Streaming.writeLong(stream, valTypes.size());
    for (int i = 0; i < valTypes.size(); i++) {
      valTypes.get(i).streamOut(stream);
    }
  }

  public boolean streamIn(DataInputStream stream) throws IOException {
    int count;
    count = (int) Streaming.readLong(stream);

    valTypes.setSize(count);
    for (int i = 0; i < count; i++) {
      valTypes.set(i, new ValType());
      valTypes.get(i).streamIn(stream);
    }
    return true;
  }
}
