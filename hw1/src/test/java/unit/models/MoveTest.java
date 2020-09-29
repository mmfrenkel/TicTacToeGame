package unit.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import models.Move;
import models.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MoveTest {
  
  Move move;
  Player player;
  
  /**
   * Creates test players and moves to be referred to in tests.
   */
  @BeforeEach
  void initMove() {
    player = new Player('X', 1);
    move = new Move(player, 1, 2);
  }
  
  /**
   * Tests that getPlayer() returns the correct player.
   */
  @Test
  @DisplayName("Move getPlayer() method should get the correct player.")
  void testGetPlayer() {
    
    Player movePlayer = move.getPlayer();
    assertEquals(player, movePlayer);
  }
  
  /**
   * Tests that getPlayerId() gets the ID of the correct player.
   */
  @Test
  @DisplayName("Move getPlayerId() method should get the correct player Id.")
  void testGetPlayerId() {

    int playerId = move.getPlayerId();
    assertEquals(playerId, player.getId());
  }
  
  /**
   * Tests that setPlayer() allows a player to be set.
   */
  @Test
  @DisplayName("Move setPlayer() method should allow Player to be set.")
  void testSetPlayer() {
    
    Player player = new Player('O', 2);
    move.setPlayer(player);
    assertEquals(player, move.getPlayer());
  }
  
  /**
   * Tests that getMoveX() returns the expected X coordinate of the move.
   */
  @Test
  @DisplayName("Move getMoveX() should correctly retrieve X coordinate of move.")
  void testGetMoveX() {

    assertEquals(1, move.getMoveX());
  }
  
  /**
   * Tests that getMoveY() returns the expected Y coordinate of the move.
   */
  @Test
  @DisplayName("Move getMoveY() should correctly retrieve Y coordinate of move.")
  void testGetMoveY() {

    assertEquals(2, move.getMoveY());
  }
  
  /**
   * Tests that getMoveX() returns the expected X coordinate of the move.
   */
  @Test
  @DisplayName("Move setMoveX() should correctly set X coordinate of move.")
  void testSetMoveX() {

    move.setMoveX(2);
    assertEquals(2, move.getMoveX());
  }
  
  /**
   * Tests that setMoveY() sets the Y coordinate of the move.
   */
  @Test
  @DisplayName("Move setMoveY() should correctly set Y coordinate of move.")
  void testSetMoveY() {

    move.setMoveY(1);
    assertEquals(1, move.getMoveY());
  }
  
  /**
   * Tests that Move toString() method reflects the state of the Move instance.
   */
  @Test
  @DisplayName("Move's toString() method should accurate reflect the Move instance.")
  void testToStringMove() {

    String expected = "Move [player=Player [type=X, id=1], moveX=1, moveY=2]";
    assertEquals(expected, move.toString());
  }
  
  /**
   * Test equals() function; a Move instance can never be equal to an instance
   * of another class.
   */
  @Test
  @DisplayName("A Move instance can never be equal to a String instance.")
  void testEqualityMovesDifferentClasses() {

    String s = "This is a string";
    assertEquals(false, move.equals(s));
  }
  
  /**
   * Test equals() function; a Move instance can never be equal to an instance
   * of Move with a different player.
   */
  @Test
  @DisplayName("Move instances are not identical if they don't have the same player.")
  void testEqualityMovesDifferentPlayers() {

    Move move2 = new Move(new Player('O', 1), 1, 2);
    assertEquals(false, move.equals(move2));
  }
  
  /**
   * Test equals() function; a Move instance can never be equal to an instance
   * of Move that doesn't have the same X coordinate.
   */
  @Test
  @DisplayName("Move instances are not identical if they don't have the same X move.")
  void testEqualityMovesDifferentX() {

    Move move2 = new Move(new Player('X', 1), 2, 2);
    assertEquals(false, move.equals(move2));
  }
  
  /**
   * Test equals() function; a Move instance can never be equal to an instance
   * of Move that doesn't have the same Y coordinate.
   */
  @Test
  @DisplayName("Move instances are not identical if they don't have the same Y move.")
  void testEqualityMovesDifferentY() {

    Move move2 = new Move(new Player('X', 1), 1, 1);
    assertEquals(false, move.equals(move2));
  }
  
  /**
   * Test equals() function; a Move instance is equal to another Move instance
   * if they share all the same fields.
   */
  @Test
  @DisplayName("Move instances are identical if they share the same fields.")
  void testEqualityMovesEqual() {

    Move move2 = new Move(new Player('X', 1), 1, 2);
    assertEquals(true, move.equals(move2));
  }
  
  /**
   * Tests to make sure that hashCode() calculation works.
   */
  @Test
  @DisplayName("Since tests for equality are specified for Move, test"
      + " objecthash() should pass.")
  void testObjectHash() {
    Move move1 = new Move(new Player('X', 1), 1, 2);
    Move move2 = new Move(new Player('X', 1), 1, 2);
    assertEquals(move1.hashCode(), move2.hashCode());
  }
}
