package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import models.GameBoard;
import models.Move;
import models.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import util.DbServiceException;
import util.TicTacToeSqliteDbService;

@TestMethodOrder(OrderAnnotation.class)
public class TicTacToeSqliteDbServiceTest {
  
  String testDb = "test.db";
  TicTacToeSqliteDbService dbService = new TicTacToeSqliteDbService(testDb);
  TicTacToeSqliteDbService dbMock =  Mockito.spy(new TicTacToeSqliteDbService(testDb));
  Connection conn;

  /**
   * Run before each test method.
   * @throws SQLException un-handled SQL error
   * @throws DbServiceException DbService Exception on connect or create tables
   */
  @BeforeEach
  public void createConn() throws SQLException, DbServiceException {
    conn = DriverManager.getConnection("jdbc:sqlite:" + testDb);
    dbService.createDatabasesTables();
    dbService.connect();
  }

  @Test
  @Order(1)
  @DisplayName("Database should successfully create.")
  public void testCreateDatabasesTables() throws DbServiceException, SQLException {

    // check that tables were created
    String sql = ""
        + "SELECT \n" 
        +  "  name\n" 
        + "FROM \n" 
        + "  sqlite_master\n" 
        + "WHERE  type = \"table\"\n" 
        + "  AND name NOT LIKE \"sqlite_%\";";
    
    
    Statement statement = conn.createStatement();
    ResultSet rs = statement.executeQuery(sql);
    
    ArrayList<String> result = new ArrayList<String>();
    while (rs.next()) {
      result.add(rs.getString("name"));
    }
    
    ArrayList<String> expected = new ArrayList<>(Arrays.asList("games", "players", "moves"));
    assertEquals(expected, result);
    
  }
  
  @Test
  @Order(2)
  @DisplayName("With an empty database, the gameboard restored should be empty.")
  public void testRestoreMostRecentGameBoard() throws DbServiceException, SQLException {
    
    // we just want to see that this doesn't throw errors
    GameBoard result = dbService.restoreMostRecentGameBoard();
    
    GameBoard expected = new GameBoard();
    
    assertEquals(expected.toString(), result.toString());
  }
  
  @Test
  @Order(3)
  @DisplayName("With an ID that doesn't exist, the gameboard restored should be empty.")
  public void testLoadGameBoardById() throws DbServiceException, SQLException {
    
    // we just want to see that this doesn't throw errors
    GameBoard result = dbService.restoreGameBoard(1);
    
    GameBoard expected = new GameBoard();
    
    assertEquals(expected.toString(), result.toString());
  }
  
  @Test
  @Order(4)
  @DisplayName("After a game has started, it should exist in the db, but should be empty still.")
  public void testStartGame() throws DbServiceException, SQLException {
    
    dbService.createNewGame(1);
    dbService.commit();
    
    dbService.connect();
    GameBoard result = dbService.restoreGameBoard(1);
    
    GameBoard expected = new GameBoard();
    assertEquals(expected.toString(), result.toString());
  }
  
  @Test
  @Order(5)
  @DisplayName("Added players should be reflected in the restored game board.")
  public void testAddPlayers() throws DbServiceException, SQLException {
    
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 2);   
    
    dbService.savePlayer(player1, 1);
    dbService.savePlayer(player2, 1);
    dbService.commit();
    
    dbService.connect();
    GameBoard result = dbService.restoreGameBoard(1);
    
    assertEquals(player1, result.getP1());
    assertEquals(player2, result.getP2());
  }
  
  @Test
  @Order(6)
  @DisplayName("A saved game state should be reflected in the loaded game board.")
  public void testSaveGameState() throws DbServiceException, SQLException {
    
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 2); 
    char[][] emptyBoard = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    
    // (Player p1, Player p2, boolean gameStarted, int turn, char[][] state, 
    // int winner, boolean isDraw, TicTacToeDbService dbService)
    GameBoard gb = new GameBoard(player1, player2, true, 1, emptyBoard, 0, false, null);
    dbService.saveGameState(gb, 1);
    dbService.commit();
   
    dbService.connect();
    GameBoard result = dbService.restoreGameBoard(1);
    assertEquals(true, result.isGameStarted());
    assertEquals(1, result.getTurn());
    assertEquals(0, result.getWinner());
  }
  
  @Test
  @Order(7)
  @DisplayName("A saved move should be reflected in the loaded game board.")
  public void saveValidMove() throws DbServiceException, SQLException {
    
    Player player1 = new Player('X', 1);
    Move move = new Move(player1, 0, 0);
    
    dbService.saveValidMove(move, 1);
    dbService.commit();
   
    dbService.connect();
    GameBoard result = dbService.restoreGameBoard(1);
    
    assertEquals('X', result.getBoardState()[0][0]);
  }
  
  @Test
  @Order(8)
  @DisplayName("You should be able to find all moves on the game board.")
  public void findAllMoves() throws DbServiceException, SQLException {
    
    ArrayList<Move> result = (ArrayList<Move>) dbService.findAllMoves(1);
    
    ArrayList<Move> expected = new ArrayList<Move>();
    Player player1 = new Player('X', 1);
    expected.add(new Move(player1, 0, 0));
    
    assertEquals(expected, result);
  }
  
  @Test
  @Order(9)
  @DisplayName("You should be able to recover the players from the board game.")
  public void findAllPlayers() throws DbServiceException, SQLException {
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 2); 
    
    ArrayList<Player> result = (ArrayList<Player>) dbService.findAllPlayers(1);
    
    ArrayList<Player> expected = new ArrayList<Player>();
    expected.add(player1);
    expected.add(player2);
    
    assertEquals(expected, result);
  }
  
  @Test
  @Order(10)
  @DisplayName("Add players to game should fail without connection.")
  public void testNoConnection() throws DbServiceException, SQLException {

    dbService.deleteGame(1, true);
    
    // we just want to see that this doesn't throw errors
    dbService.connect();
    GameBoard result = dbService.restoreMostRecentGameBoard();
    
    GameBoard expected = new GameBoard();
    
    assertEquals(expected.toString(), result.toString());
  }
  
  @Test
  @Order(11)
  @DisplayName("You need to establish a connection before restoring gameboard.")
  public void testRestoreNoConn() throws DbServiceException, SQLException {
    
    dbService.close();

    Assertions.assertThrows(DbServiceException.class, () -> {
      dbService.restoreGameBoard(1);
    });
  }
  
  @Test
  @Order(12)
  @DisplayName("You need to establish a connection before creating new game.")
  public void testCreateNewGameNoConn() throws DbServiceException, SQLException {
    
    dbService.close();

    Assertions.assertThrows(DbServiceException.class, () -> {
      dbService.createNewGame(1);
    });
  }
  
  @Test
  @Order(13)
  @DisplayName("You need to establish a connection before creating saving game state.")
  public void testSaveGameStateNoConn() throws DbServiceException, SQLException {
    
    dbService.close();
    GameBoard gb = new GameBoard();
    
    Assertions.assertThrows(DbServiceException.class, () -> {
      dbService.saveGameState(gb, 1);
    });
  }
  
  @Test
  @Order(14)
  @DisplayName("You need to establish a connection before creating saving player.")
  public void testSavePlayerNoConn() throws DbServiceException, SQLException {
    
    dbService.close();
    Player player1 = new Player('X', 1);
    
    Assertions.assertThrows(DbServiceException.class, () -> {
      dbService.savePlayer(player1, 1);
    });
  }
  
  @Test
  @Order(15)
  @DisplayName("You need to establish a connection before creating saving move.")
  public void testSaveMoveNoConn() throws DbServiceException, SQLException {
    
    dbService.close();
    Player player1 = new Player('X', 1);
    
    Assertions.assertThrows(DbServiceException.class, () -> {
      dbService.saveValidMove(new Move(player1, 0, 0), 1);
    });
  }
  
  @Test
  @Order(16)
  @DisplayName("You need to establish a connection before creating saving move.")
  public void testDeleteDbNoConn() throws DbServiceException, SQLException {
    
    dbService.close();
    
    Assertions.assertThrows(DbServiceException.class, () -> {
      dbService.deleteGame(1, true);
    });
  }
  
  @Test
  @Order(17)
  @DisplayName("The test gameboard should be reloaded full, if it is.")
  public void testCreateFullGB() throws DbServiceException, SQLException {
    
    dbService.createNewGame(1);
    dbService.commit();
    
    dbService.connect();
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 2);   
    dbService.savePlayer(player1, 1);
    dbService.savePlayer(player2, 1);
    dbService.commit();
    
    // now add a bunch of moves
    dbService.connect();

    dbService.saveValidMove(new Move(player1, 0, 1), 1);
    dbService.saveValidMove(new Move(player2, 0, 2), 1);
    dbService.saveValidMove(new Move(player1, 2, 1), 1);
    dbService.saveValidMove(new Move(player2, 1, 1), 1);
    dbService.saveValidMove(new Move(player1, 2, 0), 1);
    dbService.saveValidMove(new Move(player2, 2, 2), 1);
    dbService.saveValidMove(new Move(player1, 1, 2), 1);
    dbService.saveValidMove(new Move(player2, 1, 0), 1);
    dbService.saveValidMove(new Move(player1, 0, 0), 1);
    dbService.commit();
    
    // reconnect and get the game board as saved
    dbService.connect();
    GameBoard result = dbService.restoreGameBoard(1);
    assertEquals(true, result.isDraw());
    assertEquals(true, result.isFull());
  }
  
  @Test
  @Order(18)
  @DisplayName("The test gameboard should be reloaded winner, if it is.")
  public void testLoadWinninGB() throws DbServiceException, SQLException {
    
    // reset database
    dbService.deleteGame(1, true);
    
    // create a new game with players
    dbService.connect();
    dbService.createNewGame(1);
    dbService.commit();
    
    dbService.connect();
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 2);   
    dbService.savePlayer(player1, 1);
    dbService.savePlayer(player2, 1);
    dbService.commit();
    
    // now add a bunch of moves
    dbService.connect();
    dbService.saveValidMove(new Move(player1, 0, 0), 1);
    dbService.saveValidMove(new Move(player2, 0, 1), 1);
    dbService.saveValidMove(new Move(player1, 1, 1), 1);
    dbService.saveValidMove(new Move(player2, 0, 2), 1);
    dbService.saveValidMove(new Move(player1, 2, 2), 1);
    dbService.commit();
    
    // reconnect and get the game board as saved
    dbService.connect();
    GameBoard result = dbService.restoreGameBoard(1);
    assertEquals(false, result.isDraw());
    assertEquals(false, result.isFull());
    assertEquals(1, result.getWinner());
  }
  
  @Test
  @Order(19)
  @DisplayName("The test gameboard should be reloaded full, if it is, "
      + "with a winner when there is one.")
  public void testFullGbWithWinner() throws DbServiceException, SQLException {
    
    // reset database
    dbService.deleteGame(1, true);
    
    // create a new game with players
    dbService.connect();
    dbService.createNewGame(1);
    dbService.commit();
    
    dbService.connect();
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 2);   
    dbService.savePlayer(player1, 1);
    dbService.savePlayer(player2, 1);
    dbService.commit();
    
    // now add a bunch of moves
    dbService.connect();

    dbService.saveValidMove(new Move(player1, 0, 1), 1);
    dbService.saveValidMove(new Move(player2, 0, 2), 1);
    dbService.saveValidMove(new Move(player1, 2, 1), 1);
    dbService.saveValidMove(new Move(player2, 1, 1), 1);
    dbService.saveValidMove(new Move(player1, 1, 0), 1);
    dbService.saveValidMove(new Move(player2, 2, 2), 1);
    dbService.saveValidMove(new Move(player1, 1, 2), 1);
    dbService.saveValidMove(new Move(player2, 2, 0), 1);
    dbService.saveValidMove(new Move(player1, 0, 0), 1);
    dbService.commit();
    
    // reconnect and get the game board as saved
    dbService.connect();
    GameBoard result = dbService.restoreGameBoard(1);
    assertEquals(false, result.isDraw());
    assertEquals(true, result.isFull());
    assertEquals(2, result.getWinner());
  }
  
  @Test
  @Order(20)
  @DisplayName("Only players 1 and 2 should ever be loaded to the gameboard")
  public void testValidPlayersLoaded() throws DbServiceException {
    dbService.deleteGame(1, true);
    
    // create a new game with players
    dbService.connect();
    dbService.createNewGame(1);
    dbService.commit();
    
    // create players
    dbService.connect();
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 2);   
    Player player3 = new Player('O', 3);  
    dbService.savePlayer(player1, 1);
    dbService.savePlayer(player2, 1);
    dbService.savePlayer(player3, 1);
    dbService.commit();
    
    dbService.connect();
    GameBoard result = dbService.restoreGameBoard(1);
    assertEquals(player1, result.getP1());
    assertEquals(player2, result.getP2());
  }
  
  @Test
  @Order(21)
  @DisplayName("Exception should be thrown if foreign key constraint violated - Players.")
  public void testForeignKeyViolationPlayers() throws DbServiceException, SQLException {
    dbService.deleteGame(1, true);
    
    dbService.connect();
    Player player1 = new Player('X', 1);

    Assertions.assertThrows(DbServiceException.class, () -> {
      dbService.savePlayer(player1, 1);
    });
    dbService.close();
  }
  
  @Test
  @Order(22)
  @DisplayName("Exception should be thrown if foreign key constraint violated - Moves.")
  public void testForeignKeyViolationMoves() throws DbServiceException, SQLException {

    Player player1 = new Player('X', 1);

    Assertions.assertThrows(DbServiceException.class, () -> {
      dbService.saveValidMove(new Move(player1, 0, 1), 1);
    });
  }

  @Test
  @Order(24)
  @DisplayName("Exception thrown trying to create databases.")
  public void testCreateDatabasesFailed2() throws DbServiceException, SQLException {

    doThrow(new DbServiceException("Exception thrown"))
    .when(dbMock).connect(); 

    Assertions.assertThrows(DbServiceException.class, () -> {
      dbMock.createDatabasesTables();
    });
  }
  
  @AfterAll
  public static void deleteDb() {
    boolean result = new File("test.db").delete();
  }
  
}
