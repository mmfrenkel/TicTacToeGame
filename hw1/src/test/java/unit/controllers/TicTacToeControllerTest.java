package unit.controllers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import controllers.TicTacToeController;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import models.GameBoard;
import models.Move;
import models.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TicTacToeControllerTest {

  private Context ctx;
  
  private TicTacToeController tttcontroller;
  private TicTacToeController mockTttcontroller;
  
  private GameBoard activeGameBoard;
  
  private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create(); 
  
  @BeforeEach
  void refreshTest() {
   
    ctx = mock(Context.class);
    
    // there is only one method we need to mock, so the rest of them 
    // can be real by default for the TicTacToeController class
    tttcontroller = new TicTacToeController();
    mockTttcontroller = mock(TicTacToeController.class, CALLS_REAL_METHODS);
    
    // a sample gameboard to faciliate in setting the status of the game
    char[][] emptyBoard = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 2);
    activeGameBoard = new GameBoard(player1, player2, true, 1, emptyBoard, 0, false);
  }
  
  @Test()
  @DisplayName("Invalid request; first player can only select 'X' or 'O'.")
  void createPlayerOneInValid() {
  
    when(ctx.formParam("type")).thenReturn("P"); // P is not a valid selection
    
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      tttcontroller.startGame(ctx);
    });
  }
  
  @Test()
  @DisplayName("Invalid request; the type of player must be submitted")
  void createPlayerOneInValidMissing() {
  
    when(ctx.formParam("type")).thenReturn(null); 
    
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      tttcontroller.startGame(ctx);
    });
  }
  
  @Test()
  @DisplayName("Controller should serve new game for player.")
  void testServeNewGame() {
    tttcontroller.serveNewGame(ctx);
    verify(ctx).status(200);
  }
  
  @Test()
  @DisplayName("Controller should serve new game for player with empty board.")
  void testServeNewGameBoard() {
    tttcontroller.serveNewGame(ctx);
    
    GameBoard gb = tttcontroller.getGameBoard();
    assertEquals(gb.isEmpty(), true);
  }
  
  @Test()
  @DisplayName("Cannot start game (add Player 1) if Player 1 already exists.")
  void testStartGamePlayerAlreadyExists() {
  
    Player player1 = new Player('X', 1);
    tttcontroller.getGameBoard().setP1(player1);
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      tttcontroller.startGame(ctx);
    });
  }
  
  @Test()
  @DisplayName("Cannot join game (add Player 2) if Player2 already exists.")
  void testJoinGamePlayerAlreadyExists() {
  
    Player player1 = new Player('X', 1);
    Player player2 = new Player('O', 2);
    tttcontroller.getGameBoard().setP1(player1);
    tttcontroller.getGameBoard().setP2(player2);
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      tttcontroller.addSecondPlayer(ctx);
    });
  }
  
  @Test()
  @DisplayName("Cannot join game (add Player 2) if Player1 doesn't exist.")
  void testJoinGamePlayer1DoesntExists() {
    tttcontroller.addSecondPlayer(ctx);
    verify(ctx).status(302); 
  }
  
  @Test()
  @DisplayName("Player 2 should be able to join the game if Player 1 exists.")
  void testJoinGamePlayer1Exists() {
    
    Player player1 = new Player('X', 1);
    tttcontroller.getGameBoard().setP1(player1);
    tttcontroller.addSecondPlayer(ctx);
    verify(ctx).status(200); 
  }
  
  @Test()
  @DisplayName("Valid request to create first player.")
  void createPlayerOneValid() {
  
    when(ctx.formParam("type")).thenReturn("X");
    
    tttcontroller.startGame(ctx);
    verify(ctx).status(200);
  }
  
  @Test()
  @DisplayName("Gameboard conversion to JSON failed to produce expected format.")
  void convertGameBoardToJsonEmpty() {
  
    JsonParser parser = new JsonParser();
    
    String boardAsJson = gson.toJson(tttcontroller.getGameBoard());
    
    String expectedBoardAsJson = "{\"gameStarted\":false,\"turn\":0," 
        + "\"boardState\":[[\"\\u0000\",\"\\u0000\",\"\\u0000\"]," 
        + "[\"\\u0000\",\"\\u0000\",\"\\u0000\"],[\"\\u0000\",\"\\u0000\",\"\\u0000\"]]," 
        + "\"winner\":0,\"isDraw\":false}";
    
    assertEquals(parser.parse(expectedBoardAsJson), parser.parse(boardAsJson));
  }
  
  
  @Test()
  @DisplayName("Gameboard conversion to JSON failed to produce expected format.")
  void convertGameBoardToJsonPlayer1() {
    
    tttcontroller.getGameBoard().setP1(new Player('X', 1));
  
    JsonParser parser = new JsonParser();
    
    String boardAsJson = gson.toJson(tttcontroller.getGameBoard());
    
    String expectedBoardAsJson = "{\"p1\":{\"type\":\"X\",\"id\":1},"
        + "\"gameStarted\":false,\"turn\":0,"
        + "\"boardState\":[[\"\\u0000\",\"\\u0000\",\"\\u0000\"],"
        + "[\"\\u0000\",\"\\u0000\",\"\\u0000\"],[\"\\u0000\",\"\\u0000\",\"\\u0000\"]],"
        + "\"winner\":0,\"isDraw\":false}";

    assertEquals(parser.parse(expectedBoardAsJson), parser.parse(boardAsJson));
  }
  
  @Test()
  @DisplayName("Move should not be successful if playerId is missing from formParams "
      + "on request for processing player move")
  void testProcessPlayerMoveInvalidPlayerIdMissing() {
   
    mockTttcontroller.setGameBoard(activeGameBoard);
    when(mockTttcontroller.parsePlayerIdFromPathParam(ctx)).thenReturn(null);
    
    // each move is expected to have a playerId
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      mockTttcontroller.processPlayerMove(ctx);
    });
  }
  
  @Test()
  @DisplayName("Move should not be successful if playerId is not valid "
      + "on request for processing player move")
  void testProcessPlayerMoveInvalidMissing() {
    
    mockTttcontroller.setGameBoard(activeGameBoard);
    when(mockTttcontroller.parsePlayerIdFromPathParam(ctx)).thenReturn("7");

    // each move is expected to have a valid player id (i.e., 1 or 2)
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      mockTttcontroller.processPlayerMove(ctx);
    });
  }
  
  @Test()
  @DisplayName("Move should not be successful if the x coordinate of the"
      + "move is missing")
  void testProcessPlayerMissingMoveX() {
    
    mockTttcontroller.setGameBoard(activeGameBoard);
    when(mockTttcontroller.parsePlayerIdFromPathParam(ctx)).thenReturn("1");

    when(ctx.formParam("x")).thenReturn(null);
    when(ctx.formParam("y")).thenReturn("1");
    
    // each move is expected to have valid coordinates
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      mockTttcontroller.processPlayerMove(ctx);
    });
  }
  
  @Test()
  @DisplayName("Move should not be successful if the y coordinate of the"
      + "move is missing")
  void testProcessPlayerMissingMoveY() {
    
    mockTttcontroller.setGameBoard(activeGameBoard);
    when(mockTttcontroller.parsePlayerIdFromPathParam(ctx)).thenReturn("2");

    when(ctx.formParam("x")).thenReturn("1");
    when(ctx.formParam("y")).thenReturn(null);
    
    // each move is expected to have valid coordinates
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      mockTttcontroller.processPlayerMove(ctx);
    });
  }
  
  @Test()
  @DisplayName("Move should not be successful if user provides something "
      + "non-numberic as the coordinate of x or y")
  void testProcessPlayerInvalidCoordinate() {
    
    mockTttcontroller.setGameBoard(activeGameBoard);
    when(mockTttcontroller.parsePlayerIdFromPathParam(ctx)).thenReturn("2");
   
    when(ctx.formParam("x")).thenReturn("1");
    when(ctx.formParam("y")).thenReturn("apples");
    
    // each move is expected to have valid coordinates
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      mockTttcontroller.processPlayerMove(ctx);
    });
  }
  
  @Test()
  @DisplayName("If valid playerId and coordinates are specified, the"
      + "Move instance should be successfully created")
  void testParseMoveFromRequestValid() {
    
    mockTttcontroller.setGameBoard(activeGameBoard);
    when(mockTttcontroller.parsePlayerIdFromPathParam(ctx)).thenReturn("1");
    
    when(ctx.formParam("x")).thenReturn("1");
    when(ctx.formParam("y")).thenReturn("2");
    
    Move move = mockTttcontroller.parseMoveFromRequest(ctx);
    Move expectedMove = new Move(new Player('X', 1), 1, 2);
    
    // assert multiple times in this case, to avoid 
    assertEquals(expectedMove, move);
  }
  
  @Test()
  @DisplayName("If valid playerId and coordinates are specified, the"
      + "player's move should be successfully processed")
  void testProcessPlayerValid() {
    
    mockTttcontroller.setGameBoard(activeGameBoard);
    when(mockTttcontroller.parsePlayerIdFromPathParam(ctx)).thenReturn("1");
    
    when(ctx.formParam("x")).thenReturn("1");
    when(ctx.formParam("y")).thenReturn("2");
    
    mockTttcontroller.processPlayerMove(ctx);
    
    // assert multiple times in this case, to avoid 
    verify(ctx).status(200);
  }
}

