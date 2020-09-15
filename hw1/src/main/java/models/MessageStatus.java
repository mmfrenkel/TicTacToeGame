package models;

/*
 * Example for enumerated options implementation from B. Gurung at StackOverflow
 * on Sep 10, 2020 at https://rb.gy/fa9urh.
 */
public enum MessageStatus {
  SUCCESS(100), 
  INVALID_ORDER_OF_PLAY(410),
  POSITION_NOT_ALLOWED(411), 
  MISSING_PLAYER(412), 
  OTHER_PLAYERS_TURN(413),
  GAME_ALREADY_OVER(414), 
  GAME_OVER_WINNER(110), 
  GAME_OVER_NO_WINNER(111);
  
  private int value;
  
  MessageStatus(int value) {
    this.value = value;
  }
  
  public int getValue() {
    return value;
  }
}
