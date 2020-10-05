package models;

/*
 * Design for class to hold enumerated options 
 * was inspired by an implementation from B. Gurung at StackOverflow
 * on 01/10/2012 at https://rb.gy/fa9urh (last viewed Sep 10, 2020).
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
  GAME_OVER_NO_WINNER(111),
  DATABASE_ERROR(500);
  
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
