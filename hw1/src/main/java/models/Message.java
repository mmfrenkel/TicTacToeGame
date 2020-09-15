package models;

import com.google.gson.annotations.Expose;

public class Message {
  
  /* -- @Expose to tell gson to add below fields to json returned to user -- */
  
  @Expose
  private boolean moveValidity;
  
  @Expose
  private int code;
  
  @Expose
  private String message;
  
  /* -- end fields to serialize here -- */
  
  /**
   * Default constructor for Message.
   * 
   * @param moveValidity boolean, if move is valid
   * @param code         integer representing status/issue
   * @param message      String, human-readable description of result
   */
  public Message(boolean moveValidity, int code, String message) {
    this.moveValidity = moveValidity;
    this.code = code;
    this.message = message;
  }
  
  /**
   * Secondary constructor for Message that utilizes the MessageStatus
   * enum class, mapping status codes to their corresponding issue.
   * 
   * @param moveValidity boolean, if move is valid
   * @param code         MessageStatus enum representing status/issue
   * @param message      String, human-readable description of result
   */
  public Message(boolean moveValidity, MessageStatus code, String message) {
    this.moveValidity = moveValidity;
    this.code = code.getValue();
    this.message = message;
  }
  
  public boolean isMoveValidity() {
    return moveValidity;
  }
  
  public void setMoveValidity(boolean moveValidity) {
    this.moveValidity = moveValidity;
  }
  
  public int getCode() {
    return this.code;
  }
  
  public void setCode(int code) {
    this.code = code;
  }
  
  public String getMessage() {
    return message;
  }
  
  public void setMessage(String message) {
    this.message = message;
  }
  
  @Override
  public String toString() {
    return "Message [moveValidity=" + moveValidity + ", code=" 
        + code + ", message=" + message + "]";
  }
}
