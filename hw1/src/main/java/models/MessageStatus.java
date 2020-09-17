package models;

/*
 * Example for enumerated options implementation from B. Gurung at StackOverflow
 * on Sep 10, 2020 at https://rb.gy/fa9urh.
 */
public enum MessageStatus {
  
  // Predefined enums for game board
  SUCCESS(100), 
  INVALID_ORDER_OF_PLAY(410),
  POSITION_NOT_ALLOWED(411), 
  MISSING_PLAYER(412), 
  OTHER_PLAYERS_TURN(413),
  GAME_ALREADY_OVER(414), 
  GAME_OVER_WINNER(110), 
  GAME_OVER_NO_WINNER(111);
  
  private int value;
  
  /**
   * Allows association of an enum with a number representation.
   * 
   * @param value integer to associate with enum
   */
  MessageStatus(int value) {
    this.value = value;
  }

  /**
   * Gets the value associated with an enum. For example, 100 for SUCCESS.
   * 
   * @return integer value associated with enum
   */
  public int getValue() {
    return value;
  }
}
