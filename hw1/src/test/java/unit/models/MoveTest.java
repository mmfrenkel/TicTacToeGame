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
  
  @BeforeEach
  void initMove() {
    player = new Player('X', 1);
    move = new Move(player, 1, 2);
  }
  
  @Test
  @DisplayName("Move getPlayer() method should get the correct player.")
  void testGetPlayer() {
    
    Player movePlayer = move.getPlayer();
    assertEquals(player, movePlayer);
  }
  
  @Test
  @DisplayName("Move getPlayerId() method should get the correct player Id.")
  void testGetPlayerId() {

    int playerId = move.getPlayerId();
    assertEquals(playerId, player.getId());
  }
  
  @Test
  @DisplayName("Move setPlayer() method should allow Player to be set.")
  void testSetPlayer() {
    
    Player player = new Player('O', 2);
    move.setPlayer(player);
    assertEquals(player, move.getPlayer());
  }
  
  @Test
  @DisplayName("Move getMoveX() should correctly retrieve X coordinate of move.")
  void testGetMoveX() {

    assertEquals(1, move.getMoveX());
  }
  
  @Test
  @DisplayName("Move getMoveY() should correctly retrieve Y coordinate of move.")
  void testGetMoveY() {

    assertEquals(2, move.getMoveY());
  }
  
  @Test
  @DisplayName("Move setMoveX() should correctly set X coordinate of move.")
  void testSetMoveX() {

    move.setMoveX(2);
    assertEquals(2, move.getMoveX());
  }
  
  @Test
  @DisplayName("Move setMoveY() should correctly set Y coordinate of move.")
  void testSetMoveY() {

    move.setMoveY(1);
    assertEquals(1, move.getMoveY());
  }
  
  @Test
  @DisplayName("Move's toString() method should accurate reflect the Move instance.")
  void testToStringMove() {

    String expected = "Move [player=Player [type=X, id=1], moveX=1, moveY=2]";
    assertEquals(expected, move.toString());
  }
  
  @Test
  @DisplayName("A Move instance can never be equal to a String instance.")
  void testEqualityMovesDifferentClasses() {

    String s = "This is a string";
    assertEquals(false, move.equals(s));
  }
  
  @Test
  @DisplayName("Move instances are not identical if they don't have the same player.")
  void testEqualityMovesDifferentPlayers() {

    Move move2 = new Move(new Player('O', 1), 1, 2);
    assertEquals(false, move.equals(move2));
  }
  
  @Test
  @DisplayName("Move instances are not identical if they don't have the same X move.")
  void testEqualityMovesDifferentX() {

    Move move2 = new Move(new Player('X', 1), 2, 2);
    assertEquals(false, move.equals(move2));
  }
  
  @Test
  @DisplayName("Move instances are not identical if they don't have the same Y move.")
  void testEqualityMovesDifferentY() {

    Move move2 = new Move(new Player('X', 1), 1, 1);
    assertEquals(false, move.equals(move2));
  }
  
  @Test
  @DisplayName("Move instances are identical if they share the same fields.")
  void testEqualityMovesEqual() {

    Move move2 = new Move(new Player('X', 1), 1, 2);
    assertEquals(true, move.equals(move2));
  }
}
