package models;

import com.google.gson.annotations.Expose;

public class Player {

  /* -- @Expose to tell gson to add below fields to json returned to user -- */
 
  @Expose
  private char type;
  
  @Expose
  private int id;
  
  /* -- end fields to serialize here -- */
  
  /**
   * Constructor for Player class.
   * 
   * @param type char representing player's selected board character
   * @param id   integer identifying the player
   */
  public Player(char type, int id) {
    this.type = type;
    this.id = id;
  }

  /**
   * Returns the player's game board "type", analogous to their player piece.
   * 
   * @return char, representing player type
   */
  public char getType() {
    return type;
  }

  /**
   * Sets the player's type.
   * 
   * @param type char, representing type of player
   */
  public void setType(char type) {
    this.type = type;
  }

  /**
   * Returns ID of the player.
   * 
   * @return integer representing ID of player
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the ID of the player.
   * 
   * @param id integer representing ID of player
   */
  public void setId(int id) {
    this.id = id;
  }
  
  @Override
  public boolean equals(Object o) {
    
    if (!(o instanceof Player)) {
      return false;
    }
    Player compared = (Player) o;
    return compared.getType() == this.getType() && compared.getId() == this.getId();
  }
  
  @Override
  public String toString() {
    return "Player [type=" + type + ", id=" + id + "]";
  }
}
