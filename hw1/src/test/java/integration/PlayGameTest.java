package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.Gson;
import controllers.PlayGame;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import models.GameBoard;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class PlayGameTest {

  private Gson gson;

  /**
   * Starts the server once, before any tests run.
   */
  @BeforeAll
  public static void init() {
    PlayGame.main(null);
  }

  /**
   * This method tests to make sure that the server is up and running so that
   * tests can be conducted.
   */
  @BeforeEach
  public void testConnectionToServer() {

    // Check that we can hit the server
    HttpResponse<String> response = Unirest
        .get("http://localhost:8080/test")
        .asString();
    
    int restStatus = response.getStatus();
    if (restStatus != 200) {
      System.out.println("Check on the status of the server; test attempts at the"
          + " /test endpoint are returning " + restStatus + ".");
    }

    // this will be our helper for json -> object mapping
    gson = new Gson();
  }

  /**
   * This is a test case to evaluate the newgame endpoint; assuming the endpoint
   * is available, it should always return 200. It should reset the
   * gameboard; it should be empty.
   */
  @Test
  @Order(1)
  @DisplayName("A player should be able to start a new game.")
  public void newGameTest() {

    HttpResponse<String> response = Unirest
        .get("http://localhost:8080/newgame")
        .asString();

    assertEquals(200, response.getStatus());
    
    // ------------- Check the status of the GameBoard ------------- 
    
    HttpResponse<String> gameboard = Unirest
        .get("http://localhost:8080/gameboardstatus")
        .asString();

    // Get the GameBoard that was returned to the user
    GameBoard gameBoard = gson.fromJson(gameboard.getBody(), GameBoard.class);

    // There shouldn't be a player 1 or 2
    assertNull(gameBoard.getP1());
    assertNull(gameBoard.getP2());

    // Check that game has not started
    assertEquals(false, gameBoard.isGameStarted());
  }
  
  /**
   * Test case when a user attempts to start a new game with an invalid player
   * type. A user should receive a response that this is an invalid game board
   * configuration.
   */
  @Test
  @Order(2)
  @DisplayName("A player should not be able to start a game with an invalid player type.")
  public void startGameTestInvalid() {

    // Create a POST request to startgame endpoint
    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/startgame")
        .body("type=P")
        .asString();
    
    assertEquals(400, response.getStatus());
    assertEquals("First player should select either 'X' or 'O'; cannot accept 'P'.", 
        response.getBody());
  }

  /**
   * Test case when a user attempts to start a new game without a player
   * type. A user should receive a response that this is an invalid game board
   * configuration.
   */
  @Test
  @Order(3)
  @DisplayName("A player should not be able to start a game without a player type.")
  public void startGameTestInvalidMissingType() {

    // Create a POST request to startgame endpoint
    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/startgame")
        .body("")
        .asString();
    
    assertEquals(400, response.getStatus());
    assertEquals("First player should select either 'X' or 'O'; cannot accept 'null'.", 
        response.getBody());
  }
  
  /**
   * Test case for when a player attempts to ajoin a game when there is not
   * yet a Player 1 on the board.
   */
  @Test
  @Order(4)
  @DisplayName("Player 2 cannot join a game unless there is already a Player 1.")
  public void joinGameNoPlayers() {

    // Create a POST request to startgame endpoint
    HttpResponse<String> response = Unirest
        .get("http://localhost:8080/joingame")
        .asString();
    
    assertEquals(200, response.getStatus());
    
    // ------------- Check the status of the GameBoard ------------- 
    
    HttpResponse<String> gameboard = Unirest
        .get("http://localhost:8080/gameboardstatus")
        .asString();

    // Get the GameBoard that was returned to the user
    GameBoard gameBoard = gson.fromJson(gameboard.getBody(), GameBoard.class);

    // There shouldn't be a player 1 or 2
    assertNull(gameBoard.getP1());
    assertNull(gameBoard.getP2());

    // Check that game has not started
    assertEquals(false, gameBoard.isGameStarted());
  }

  /**
   * This is a test case to evaluate the startgame endpoint, after providing a
   * valid request to start the game with type=0. This will set Player 1 to be
   * type 'O'.
   */
  @Test
  @Order(5)
  @DisplayName("A player should be able to start a game with a valid type.")
  public void startGameTest() {

    // -------- Create a POST request to startgame endpoint -------- 
    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/startgame")
        .body("type=O")
        .asString();
    
    assertEquals(200, response.getStatus());

    // ------------- Check the status of the GameBoard ------------- 

    // Get the GameBoard that was returned to the user
    GameBoard gameBoard = gson.fromJson(response.getBody(), GameBoard.class);

    // Check if player type is correct
    assertEquals('O', gameBoard.getP1().getType());

    // Check that game has not started
    assertEquals(false, gameBoard.isGameStarted());

    // Check that gameboard is empty
    assertEquals(true, gameBoard.isEmpty());
  }

  /**
   * This is a test case for if Player 1 attempts to make a move on the game board
   * before Player 2 has joined.
   */
  @Test
  @Order(6)
  @DisplayName("A player cannot make a move until both players have joined the game.")
  public void attemptToMakeMoveTooEarly() {

    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=0&y=0")
        .asString();

    JSONObject jsonObject = new JSONObject(response.getBody());
    String msg = (String) jsonObject.get("message");

    assertEquals(400, response.getStatus());
    assertEquals("Game cannot start until there are two players", 
        msg.substring(0, 45));
  }

  /**
   * This is a test case to evaluate the response a user receives if they attempt
   * to start a game (i.e., join as player 1) where player 1 has already been
   * assigned.
   */
  @Test
  @Order(7)
  @DisplayName("If there is already a player 1 on the board, no one else can join"
      + "as player 1.")
  public void startGameTestDuplicate() {

    // Create a POST request to startgame endpoint
    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/startgame")
        .body("type=X")
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("There is already a Player 1", response.getBody().substring(0, 27));
  }

  /**
   * This is a test case for the valid addition of a second Player to the game
   * board, who should be assigned the opposite type as Player 1.
   */
  @Test
  @Order(8)
  @DisplayName("A second player should be able to join a game that has only player 1.")
  public void joinGameTest() {

    // -------- Create a GET request to joingame endpoint -------- //

    // Create a POST get request to join as the second player
    HttpResponse<String> response = Unirest
        .get("http://localhost:8080/joingame")
        .asString();
    
    assertEquals(200, response.getStatus());

    // ----------- Check the status of the GameBoard --------------//

    // Check on the status of the board
    HttpResponse<String> gameboard = Unirest
        .get("http://localhost:8080/gameboardstatus")
        .asString();

    // Get the GameBoard that was returned to the user
    GameBoard gameBoard = gson.fromJson(gameboard.getBody(), GameBoard.class);

    // Check if player type is correct; if Player 1 was 'O', Player 2 should be 'X'
    assertEquals('X', gameBoard.getP2().getType());

    // Check that game has started
    assertEquals(true, gameBoard.isGameStarted());

    // Check that gameboard is empty
    assertEquals(true, gameBoard.isEmpty());
  }

  /**
   * This is a test case for the invalid attempt to join a game that already has
   * two players. This should not be allowed.
   */
  @Test
  @Order(9)
  @DisplayName("If there are already two players, another player cannot join.")
  public void joinGameTestDuplicate() {

    // Create a POST get request to join as the second player
    HttpResponse<String> response = Unirest
        .get("http://localhost:8080/joingame")
        .asString();

    assertEquals(400, response.getStatus());
    assertEquals("Sorry, there are already two players", response.getBody().substring(0, 36));
  }

  /**
   * This is a test case for an invalid attempt by Player 2 to make the first
   * move, when Player 1 should be the first to make the a move.
   */
  @Test
  @Order(10)
  @DisplayName("After game has started, Player 1 always makes the first move.")
  public void invalidOrderOfPlay() {

    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=0&y=0")
        .asString();

    JSONObject jsonObject = new JSONObject(response.getBody());
    String msg = (String) jsonObject.get("message");

    assertEquals(400, response.getStatus());
    assertEquals("Player 1 makes the first move on an empty board", msg.substring(0, 47));
  }

  /**
   * This is a test case for Player 1 making a valid first move on the game board.
   * After their turn, the board is no longer empty and it should be Player 2's
   * turn to make a move afterwards.
   */
  @Test
  @Order(11)
  @DisplayName("If the game has started, Player 1 should be able to make a move.")
  public void testMakeValidMove() {

    // -------- Player 1 makes valid first move ----- //
    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=0&y=0")
        .asString();
    
    assertEquals(200, response.getStatus());

    // ------ Check the status of the gameboard ------//
    // Check on the status of the board
    HttpResponse<String> gameboard = Unirest
        .get("http://localhost:8080/gameboardstatus")
        .asString();
    
    GameBoard gameBoard = gson.fromJson(gameboard.getBody(), GameBoard.class);

    // Check that game has started
    assertEquals(true, gameBoard.isGameStarted());

    // Check that gameboard is not empty
    assertEquals(false, gameBoard.isEmpty());

    // Check that it is now player 2's turn
    assertEquals(2, gameBoard.getTurn());
  }

  /**
   * This is a test case for a player attempting to make a move when it is not
   * their turn.
   */
  @Test
  @Order(12)
  @DisplayName("A player should not be able to make two moves in their turn.")
  public void testInvalidTurn() {

    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=0&y=1")
        .asString();
    
    JSONObject jsonObject = new JSONObject(response.getBody());
    String msg = (String) jsonObject.get("message");

    assertEquals(400, response.getStatus());
    assertEquals("It is not currently your turn.", msg.substring(0, 30));

  }

  /**
   * This is a test case for a player attempting to make a move when the position
   * is already taken.
   */
  @Test
  @Order(13)
  @DisplayName("A player should not be able to make a move to an occupied position.")
  public void testMoveAttemptedToOccupiedPosition() {

    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=0&y=0")
        .asString();
    
    JSONObject jsonObject = new JSONObject(response.getBody());
    String msg = (String) jsonObject.get("message");

    assertEquals(400, response.getStatus());
    assertEquals("You cannot make a move", msg.substring(0, 22));

  }

  /**
   * This is a test case for a player attempting to make a move to an invalid
   * location on the gameboard.
   */
  @Test
  @Order(14)
  @DisplayName("A player should not be able to make a move to an invalid position.")
  public void testMoveAttemptedToInvalidPosition() {

    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=0&y=7")
        .asString();
    
    JSONObject jsonObject = new JSONObject(response.getBody());
    String msg = (String) jsonObject.get("message");

    assertEquals(400, response.getStatus());
    assertEquals("You cannot make a move", msg.substring(0, 22));

  }
  
  /**
   * This is a test case to make sure that a Player is capable of winning.
   */
  @Test
  @Order(15)
  @DisplayName("A player should be able to win a game.")
  public void testPlayerCanWinGame() {

    // ---- Play enough moves for the game board to be in winning configuration -- //
    
    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=0&y=1")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=1&y=1")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=0&y=2")
        .asString();

    // with this move, Player 1 makes a left horizontal and wins
    response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=2&y=2")
        .asString();

    JSONObject jsonObject = new JSONObject(response.getBody());
    String msg = (String) jsonObject.get("message");

    assertEquals(200, response.getStatus());
    assertEquals("Player 1 is the winner!", msg.substring(0, 23));
    
    // ----------------- Check the status of the gameboard -----------------//
    
    HttpResponse<String> gameboard = Unirest
        .get("http://localhost:8080/gameboardstatus")
        .asString();
    
    GameBoard gameBoard = gson.fromJson(gameboard.getBody(), GameBoard.class);
    
    // check to make sure there is a winner and that it's player 1
    assertEquals(1, gameBoard.getWinner());
    
    // check to make sure there is no draw
    assertEquals(false, gameBoard.isDraw());
  }
  
  /**
   * This is a test case to make sure that a Player is capable of making a move 
   * after the game is already won.
   */
  @Test
  @Order(16)
  @DisplayName("A player should not be able to make moves after a game is won.")
  public void testPlayerCannotMoveAFterGameWon() {
    
    HttpResponse<String> response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=1&y=2")
        .asString();

    JSONObject jsonObject = new JSONObject(response.getBody());
    String msg = (String) jsonObject.get("message");

    assertEquals(400, response.getStatus());
    assertEquals("Game is already over!", msg.substring(0, 21));
  }
  
  /**
   * This is a test case to make sure that it is possible to end the game in a
   * draw.
   */
  @Test
  @Order(17)
  @DisplayName("A game should be a draw if all the positions are exhausted and no one has won.")
  public void testGameEndsWithDraw() {

    // ------ Reset the game to the beginning and add new players  ------
    HttpResponse<String> response = Unirest
        .get("http://localhost:8080/newgame")
        .asString();

    // add back players 1 and 2
    response = Unirest
        .post("http://localhost:8080/startgame")
        .body("type=X")
        .asString();
    
    response = Unirest
        .get("http://localhost:8080/joingame")
        .asString();

    // -- Play enough moves for the game board to be in winning configuration ---

    response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=0&y=1")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=0&y=2")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=2&y=1")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=1&y=1")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=2&y=0")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=2&y=2")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=1&y=2")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/2")
        .body("x=1&y=0")
        .asString();
    
    response = Unirest
        .post("http://localhost:8080/move/1")
        .body("x=0&y=0")
        .asString();

    JSONObject jsonObject = new JSONObject(response.getBody());
    String msg = (String) jsonObject.get("message");

    assertEquals(200, response.getStatus());
    assertEquals("Game Over! Nobody wins.", msg.substring(0, 23));

    // ----------- Check the status of the gameboard ------------

    HttpResponse<String> gameboard = Unirest
        .get("http://localhost:8080/gameboardstatus")
        .asString();
    
    GameBoard gameBoard = gson.fromJson(gameboard.getBody(), GameBoard.class);

    // check to make sure there is not a winner
    assertEquals(0, gameBoard.getWinner());

    // check to make sure there is a draw
    assertEquals(true, gameBoard.isDraw());
  }
  
  /**
   * If a user goes to '/' instead of '/newgame', the get redirect successfully
   * without error to /newgame, and the gameboard should be cleared.
   */
  @Test
  @Order(18)
  @DisplayName("If someone goes to / instead of /newgame, redirect "
      + "without error to /newgame.")
  public void newGameTestRedirectNewGame() {

    HttpResponse<String> response = Unirest
        .get("http://localhost:8080/")
        .asString();
    
    // Make sure that the response to the user is successful
    assertEquals(200, response.getStatus());
    
    // ------------- Check the status of the GameBoard ------------- 
    
    HttpResponse<String> gameboard = Unirest
        .get("http://localhost:8080/gameboardstatus")
        .asString();

    // Get the gameboard; since we redirected to /newgame, it should be 
    // totally empty
    GameBoard gameBoard = gson.fromJson(gameboard.getBody(), GameBoard.class);

    // There shouldn't be a player 1 or 2
    assertNull(gameBoard.getP1());
    assertNull(gameBoard.getP2());

    // Check that game has not started
    assertEquals(false, gameBoard.isGameStarted());
  }

  /**
   * Stop the server when all the tests are complete.
   */
  @AfterAll
  public static void close() {
    // Stop Server
    PlayGame.stop();
  }
}
