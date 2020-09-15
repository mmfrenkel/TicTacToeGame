package models;

public class Move {
  
  private Player player;
  
  private int moveX;
  
  private int moveY;
  
  /**
   * Constructor for Move class.
   * @param player  instance of Player
   * @param moveX   integer, representing x coordinate of move
   * @param moveY   integer, representing y coordinate of move
   */
  public Move(Player player, int moveX, int moveY) {
    this.player = player;
    this.moveX = moveX;
    this.moveY = moveY;
  }
  
  public Player getPlayer() {
    return player;
  }
  
  public int getPlayerId() {
    return getPlayer().getId();
  }
  
  public void setPlayer(Player player) {
    this.player = player;
  }
  
  public int getMoveX() {
    return moveX;
  }
  
  public void setMoveX(int moveX) {
    this.moveX = moveX;
  }
  
  public int getMoveY() {
    return moveY;
  }
  
  public void setMoveY(int moveY) {
    this.moveY = moveY;
  }
  
  @Override
  public String toString() {
    return "Move [player=" + player + ", moveX=" + moveX 
        + ", moveY=" + moveY + "]";
  }

}
