package models;

public class Message {

	public Message(boolean moveValidity, MessageStatus code, String message) {
		this.moveValidity = moveValidity;
		this.code = code;
		this.message = message;
	}

	private boolean moveValidity;

	private MessageStatus code;

	private String message;

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
