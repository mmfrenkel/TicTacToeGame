package models;

import com.google.gson.annotations.Expose;

public class Player {

	@Expose
	private char type;

	@Expose
	private int id;

	public Player(char type, int id) {
		this.type = type;
		this.id = id;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Player [type=" + type + ", id=" + id + "]";
	}
}
