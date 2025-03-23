package com.basic4gl.debug.protocol.callbacks;

public class ProtocolMessage {

  private int id;

  protected String type;

  public ProtocolMessage(String type) {
    this.type = type;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public String getType() {
    return type;
  }
}
