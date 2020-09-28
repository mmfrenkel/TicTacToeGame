package unit.controllers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import controllers.TicTacToeController;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import models.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TicTacToeControllerTest {

  private Context ctx;
  
  private TicTacToeController tttcontroller;
  
  private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create(); 
  
  @BeforeEach
  void refreshTest() {
    tttcontroller = new TicTacToeController();
    ctx = mock(Context.class);
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
}

