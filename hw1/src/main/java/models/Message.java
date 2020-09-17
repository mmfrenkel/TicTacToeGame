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
   * Secondary constructor for Message that utilizes the MessageStatus enum class,
   * mapping status codes to their corresponding issue.
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

  /**
   * Returns whether or not the Move associated with the Message is valid.
   * 
   * @return boolean, true if valid, else false
   */
  public boolean isMoveValidity() {
    return moveValidity;
  }

  /**
   * Sets the validity of the Move associated with the Message.
   * 
   * @param moveValidity boolean, true if valid, else false
   */
  public void setMoveValidity(boolean moveValidity) {
    this.moveValidity = moveValidity;
  }

  /**
   * Gets the code associated with the Message. For more information, please see
   * the MessageStatus.java class for the full enumerated list of possible codes,
   * however, it is not strictly required to use one of those codes.
   * 
   * @return integer, representing category of issue or status
   */
  public int getCode() {
    return this.code;
  }

  /**
   * Sets the code associated with the Message. See MessageStatus.java for
   * predefined messages, although it is not strictly required to use one of this
   * prescribed codes.
   * 
   * @param code integer, representing category of issue or status
   */
  public void setCode(int code) {
    this.code = code;
  }

  /**
   * Returns the human-readable status associated with Message.
   * 
   * @return String representing human-readable status update
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the human-readable status update for the Message.
   * 
   * @param message String representing human-readable status update
   */
  public void setMessage(String message) {
    this.message = message;
  }
  
  @Override
  public String toString() {
    return "Message [moveValidity=" + moveValidity + ", code=" 
        + code + ", message=" + message + "]";
  }
}
