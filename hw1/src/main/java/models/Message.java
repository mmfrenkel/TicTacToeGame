package models;

import com.google.gson.annotations.Expose;

public class Message {

	/* -- @Expose to tell gson to add only the below fields to json returned to user -- */
	
	@Expose
	private boolean moveValidity;

	@Expose
	private MessageStatus code;

	@Expose
	private String message;
	
	/* -- end fields to serialize here -- */
	
	public Message(boolean moveValidity, MessageStatus code, String message) {
		this.moveValidity = moveValidity;
		this.code = code;
		this.message = message;
	}

	public boolean isMoveValidity() {
		return moveValidity;
	}

	public void setMoveValidity(boolean moveValidity) {
		this.moveValidity = moveValidity;
	}

	public MessageStatus getCode() {
		return code;
	}

	public void setCode(MessageStatus code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
