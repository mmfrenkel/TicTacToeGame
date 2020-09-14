package models;

/*
 * Example for enumerated options implementation details from B. Gurung at 
 * https://stackoverflow.com/questions/8811815/is-it-possible-to-assign-numeric-value-to-an-enum-in-java
 */
public enum MessageStatus {
	SUCCESS(100), INVALID_ORDER_OF_PLAY(410), POSITION_NOT_ALLOWED(115), MISSING_PLAYER(412), OTHER_PLAYERS_TURN(413),
	GAME_ALREADY_OVER(414), GAME_OVER_WINNER(110), GAME_OVER_NO_WINNER(111);

	private int value;

	MessageStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
