package models;

public class Move {

  private Player player;

  private int moveX;

  private int moveY;

  /**
   * Main constructor for Move class.
   * 
   * @param player instance of Player
   * @param moveX  integer, representing x coordinate of move
   * @param moveY  integer, representing y coordinate of move
   */
  public Move(Player player, int moveX, int moveY) {
    this.player = player;
    this.moveX = moveX;
    this.moveY = moveY;
  }

  /**
   * Gets the current Player associated with the move.
   * 
   * @return instance of Player
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Helper function for getting the current Player ID associated with the move.
   * 
   * @return integer representing player's id
   */
  public int getPlayerId() {
    return getPlayer().getId();
  }

  /**
   * Set the player for the Move.
   * 
   * @param player instance of Player
   */
  public void setPlayer(Player player) {
    this.player = player;
  }

  /**
   * Gets the X coordinate position of the Move.
   * 
   * @return integer representing X coordinate
   */
  public int getMoveX() {
    return moveX;
  }

  /**
   * Sets the X coordinate position of the Move.
   * 
   * @param moveX integer representing X coordinate
   */
  public void setMoveX(int moveX) {
    this.moveX = moveX;
  }

  /**
   * Gets the Y coordinate position of the Move.
   * 
   * @return integer representing Y coordinate
   */
  public int getMoveY() {
    return moveY;
  }

  /**
   * Sets the Y coordinate position of the Move.
   * 
   * @param moveY integer representing Y coordinate
   */
  public void setMoveY(int moveY) {
    this.moveY = moveY;
  }
  
  @Override
  public String toString() {
    return "Move [player=" + player + ", moveX=" + moveX 
        + ", moveY=" + moveY + "]";
  }

}
