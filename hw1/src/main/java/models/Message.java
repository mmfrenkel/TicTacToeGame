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

}
