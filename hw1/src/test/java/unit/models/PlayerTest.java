package unit.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import models.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PlayerTest {
  
  /**
   * Test that Player constructor is able to set Player ID.
   */
  @Test
  @DisplayName("Player constructor should be able to assign ID.")
  void testCreatePlayerId() {
    
    Player player = new Player('X', 1);
    assertEquals(1, player.getId());
  }
  
  /**
   * Test that Player constructor is able to set Player type.
   */
  @Test
  @DisplayName("Player constructor should be able to assign type.")
  void testCreatePlayerType() {
    
    Player player = new Player('X', 1);
    assertEquals('X', player.getType());
  }
  
  /**
   * Test that setType() allows type to be set for Player.
   */
  @Test
  @DisplayName("setType() method should allow type to be set.")
  void testSetType() {
    
    Player player = new Player('X', 1);
    player.setType('O');
    assertEquals('O', player.getType());
  }
  
  /**
   * Test that setId() allows id to be set for Player.
   */
  @Test
  @DisplayName("setId() method should allow type to be set.")
  void testSetId() {
    
    Player player = new Player('X', 1);
    player.setId(2);
    assertEquals(2, player.getId());
  }
  
  /**
   * Tests equals() method; an instance of Player can never be
   * equal to an instance of another class.
   */
  @Test
  @DisplayName("A String object is not a Player.")
  void testPlayerEqualityDifferentTypes() {
    Player player = new Player('X', 1);
    String s = "This is a string";
    assertEquals(false, player.equals(s));
  }
  
  /**
   * Tests equals() method; an instance of Player can never be
   * equal to an instance of Player with a different ID.
   */
  @Test
  @DisplayName("A Players with different IDs are not the same Player.")
  void testPlayerEqualityDiffernetIds() {
    Player player1 = new Player('X', 1);
    Player player2 = new Player('X', 2);
    assertEquals(false, player1.equals(player2));
  }
  
  /**
   * Tests equals() method; an instance of Player can never be
   * equal to an instance of Player with a different type.
   */
  @Test
  @DisplayName("A Players with different types are not the same Player.")
  void testPlayerEqualityDiffernetTypes() {
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 1);
    assertEquals(false, player1.equals(player2));
  }
  
  /**
   * Tests equals() method; an instance of Player is equal to another
   * instance of Player if they share the same ID and type.
   */
  @Test
  @DisplayName("A Players with the same ID and type are identical.")
  void testPlayerEquality() {
    Player player1 = new Player('X', 1);
    Player player2 = new Player('X', 1);
    assertEquals(true, player1.equals(player2));
  }
}
