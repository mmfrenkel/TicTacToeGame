package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import models.GameBoard;
import models.GameBoardInternalError;
import models.Message;
import models.Move;
import models.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.TicTacToeDbService;

public class TicTacToeController {

  private GameBoard gameBoard;
  
  private static Logger logger = LoggerFactory.getLogger(TicTacToeController.class);
  
  // Utilize Gson for object->json mapping instead of Jackson, the Javalin default
  private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

  /**
   * Primary Constructor to use default empty game board configuration.
   */
  public TicTacToeController() {
    this.gameBoard = new GameBoard();
  }
  
  /**
   * Secondary Constructor to set game board state manually. This is important for
   * testing.
   * 
   * @param gameBoard GameBoard instance
   */
  public TicTacToeController(GameBoard gameBoard) {
    this.gameBoard = gameBoard;
  }
  
  public TicTacToeController(TicTacToeDbService dbService) {
    this.gameBoard = new GameBoard(dbService);
  }
  
  /**
   * Sets the game board to the most recent game board in the database.
   * 
   * @throws GameBoardInternalError if there was an issue getting the last game
   *                                from the database
   */
  public void loadGameBoard() throws GameBoardInternalError {
    try {
      GameBoard lastState = gameBoard.getMostRecentDbState();
      setGameBoard(lastState);
      logger.info(getGameBoardAsJson());
      
    } catch (GameBoardInternalError e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      throw e;
    }
  }

  /**
   * Redirects user to a new game board and resets the game board to a new,
   * cleared board without any players or moves. This includes
   * deleting the existing game from the database.
   * 
   * @param ctx Context object for incoming request
   * @return Context object updated
   */
  public Context serveNewGame(Context ctx) {
    try {
      gameBoard.resetGameboard();
      
    } catch (GameBoardInternalError e) {
      ctx.result("An issue was encountered clearing the database for the new game. "
          + "Please try again.");
      ctx.status(500);              // this would be an unhandled internal error
      return ctx;
    } 
    
    ctx.status(200); 
    ctx.redirect("/tictactoe.html");
    return ctx;
  }

  /**
   * Creates player one with their selected type (i.e., 'X' or 'O'). First player
   * to join game will always be player 1; however the game does not officially
   * start until player 2 joins.
   * 
   * @param ctx Context object for incoming request
   * @return Updated Context object
   * @throws BadRequestResponse if there is already a Player 1 or if an invalid
   *                            player type was provided
   */
  public Context startGame(Context ctx) {
      
    // if there is already a player 1, we don't want to kick them out!
    if (gameBoard.getP1() != null) {
      throw new BadRequestResponse("There is already a Player 1 for this game board."
          + " If you'd like to start a new game, please visit our /newgame endpoint, "
          + "or to join the existing game, ask Player 1 to share his join link, "
          + "or vist our /joingame enpoint.");
    }
    
    // Parse player 1 information then add player one to the game
    Player player1 = parsePlayerOneFromRequest(ctx);
    
    try {
      gameBoard.saveP1(player1);
      ctx.result(getGameBoardAsJson());
      
    } catch (GameBoardInternalError e) {
      ctx.result("Could not create Player 1; it's possible that you never started a game. "
          + "Please go to /newgame first and try again.");
      ctx.status(500);
      return ctx;
    }

    logger.info("Added first player to the game: " + player1);
    ctx.status(200);
    return ctx;
  }
  
  /**
   * Adds second player to the game; if first player doesn't yet exist, then
   * redirects user to the new game end point so that they can enter as the first
   * player and select their player type.
   * 
   * @param ctx Context object from incoming request
   * @return Updated Context object
   * @throws BadRequestResponse if Player 2 already exists for this game
   */
  public Context addSecondPlayer(Context ctx) {
    
    logger.info(getGameBoardAsJson());
    
    // if there is already a player 2, we don't want to kick them out!
    if (gameBoard.getP2() != null) {
      throw new BadRequestResponse("Sorry, there are already two players for this game board."
          + " If you'd like to start a new game, please visit our /newgame endpoint.");
    }
  
    // if first player doesn't already exist, then redirect to the start game end
    // point so they can choose their player type
    if (gameBoard.getP1() == null) {
      logger.info("Currently there is no game to join (no Player 1 yet). "
          + "Redirecting user to new game. Board State: " + gameBoard);
      
      ctx.status(302);
      ctx.redirect("/newgame");
      return ctx;
    }
    
    try {
      // update player in memory + db
      gameBoard.autoSetP2();
      
    } catch (GameBoardInternalError e) {
      ctx.result("Could not add Player 2 due to a game board error; please try again!");
      ctx.status(500); // this would be an un-handled internal error
      return ctx;
    }
    
    ctx.status(200);
    ctx.redirect("/tictactoe.html?p=2"); 
    return ctx;
  }
  
  /**
   * Handles the move submitted by a user, parsing move from Context object and
   * sending move to the game board. Outcome of the move is returned in Context
   * result as JSON message.
   * 
   * @param ctx Context object from incoming request
   * @return Updated Context object
   */
  public Context processPlayerMove(Context ctx) {
    
    Move move = parseMoveFromRequest(ctx);
    logger.info("Handling move submitted: " + move);
    
    try {
      Message message = gameBoard.processPlayerMove(move);
      logger.info("Outcome of processed move: " + message);
    
      ctx.result(gson.toJson(message));
    } catch (GameBoardInternalError e) {
      ctx.result("Move on game board could not be processed due to a database issue; " 
          + "please try again!");
      ctx.status(500); // this would be an un-handled internal error
      return ctx;
    }

    ctx.status(200); 
    return ctx;
  }
  
  /**
   * Helper function to facilitate the conversion of the game board into JSON.
   * Uses Gson for object->JSON mapping instead of default Jackson and Javalin
   * because of failure to handle null arrays as expected and better field
   * mapping.
   * 
   * @return JSON String representing state of game board
   */
  public String getGameBoardAsJson() {
    return gson.toJson(gameBoard);
  }
  
  /**
   * Returns the current game board.
   * 
   * @return instance of GameBoard class
   */
  public GameBoard getGameBoard() {
    return gameBoard;
  }

  /**
   * Sets the current game board configuration. Method
   * is helpful for testing purposes, but it not recommended 
   * otherwise, as there is currently no support for testing
   * invalid configurations.
   * 
   * @param gameBoard instance of GameBoard class
   */
  public void setGameBoard(GameBoard gameBoard) {
    this.gameBoard = gameBoard;
  }
  
  /**
   * Extracts the "type'"that the first player selected then creates the player.
   * 
   * @param ctx Context object
   * @return new Player parsed from Context object
   * @throws BadRequestResponse if form parameter 'type' isn't one of expected
   *                            values; default Javalin 400 response
   */
  private Player parsePlayerOneFromRequest(Context ctx) {

    // options for the form parameter "type" are "X" or "O"
    String submittedType = ctx.formParam("type");
    
    if (submittedType == null || !gameBoard.acceptedTypes().contains(submittedType.charAt(0))) {
      // the form parameter isn't what we expected; either it's missing or not
      // one of the accepted types, raise custom exception; use default Javalin 400
      // response
      throw new BadRequestResponse("First player should select either " 
          + "'X' or " + "'O'; cannot accept '" + submittedType + "'.");
    }
    
    return new Player(submittedType.charAt(0), 1);
  }

  /**
   * Extracts submitted information from context and returns a new Move() object
   * representing the requested move from the user. To protect against invalid
   * submissions by users accessing the game via an API interaction (instead of
   * UI), this method checks to be sure that both coordinates are submitted and
   * both are integer values, and that there is a valid player id submitted.
   * Method should be made private, but is kept public for testing purposes.
   * 
   * @param ctx Context object from incoming request
   * @return new instance of Move object
   * @throws BadRequestResponse If an expected form parameter is missing or not
   *         expected type
   */
  public Move parseMoveFromRequest(Context ctx) {
    
    String playerId = parsePlayerIdFromPathParam(ctx);

    // Check for valid player ID in request
    if (playerId == null || (!playerId.equals("1") && !playerId.equals("2"))) {
      throw new BadRequestResponse("Your request must include player Id (1 or 2) " 
          + "as a path parameter. Got " + playerId);
    }
    
    Player currentPlayer = playerId.equals("1") ? gameBoard.getP1() : gameBoard.getP2();
    String moveX = ctx.formParam("x");
    String moveY = ctx.formParam("y");
    int x;
    int y;
    
    // Make sure that submitted move includes x, y form integer parameters
    if (moveX == null || moveY == null) {
      throw new BadRequestResponse("To make a game move, players must submit a board "
          + "row number (X) and column number (Y) (e.g., x=0&y=0");
    }
    try {
      x = Integer.parseInt(moveX);
      y = Integer.parseInt(moveY);
    } catch (NumberFormatException nfe) {
      // position played is not, in fact, represented by numbers
      throw new BadRequestResponse("Players can only submit integer values to " 
          + "indiciate their move" + moveX + " and " + moveY);
    }
    return new Move(currentPlayer, x, y);
  }

  /**
   * This function gets the playerId from the Context and returns it. This
   * function was created specifically for testing purposes. It does not seem that
   * mocking support works the same way for pathParams as formParams. As a result,
   * you cannot easily mock with:
   * <p>
   * when(ctx.formParam("x")).thenReturn(null)
   * </p>
   * Instead, this function will remain the sole piece of code that is not able to
   * be tested, as the rest of the code will be tested around it.
   * 
   * @param ctx Context object from incoming request
   * @return String representing playerId (may be null)
   */
  public String parsePlayerIdFromPathParam(Context ctx) {
    return ctx.pathParam("playerId");
  }
}
