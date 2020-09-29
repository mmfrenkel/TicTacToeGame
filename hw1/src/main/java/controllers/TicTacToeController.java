package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import models.GameBoard;
import models.Message;
import models.MessageStatus;
import models.Move;
import models.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicTacToeController {

  private GameBoard gameBoard;
  
  private static Logger logger = LoggerFactory.getLogger(PlayGame.class);
  
  // Utilize Gson for object->json mapping instead of Jackson, the Javalin default
  private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
  
  public TicTacToeController() {
    this.gameBoard = new GameBoard();
  }

  /**
   * Redirects user to a new game board and resets the game board to a new,
   * cleared board without any players or moves.
   * 
   * @param ctx Context object for incoming request
   * @return Context object updated
   */
  public Context serveNewGame(Context ctx) {
    logger.info("Received request to start a new game!");
    
    setGameBoard(new GameBoard()); // used to reset gameBoard when necessary
    ctx.redirect("/tictactoe.html");
    ctx.status(200);
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
    gameBoard.setP1(player1);
    gameBoard.setTurn(1);
    
    logger.info("Added first player to the game: " + player1);
    ctx.result(getGameBoardAsJson());
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
      ctx.redirect("/newgame");
      ctx.status(302);  // redirect
      return ctx;
    }
    
    gameBoard.autoSetP2();
    gameBoard.setGameStarted(true);
    
    logger.info("Added second player; game board is now ready. Player 2: " + gameBoard.getP2());
    ctx.redirect("/tictactoe.html?p=2"); 
    ctx.status(200);
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
    
    Message message = gameBoard.processPlayerMove(move);
    
    logger.info("Outcome of processed move: " + message);
    ctx.result(gson.toJson(message));

    if (message.getCode() == MessageStatus.SUCCESS.getValue()
        || message.getCode() == MessageStatus.GAME_OVER_WINNER.getValue()
        || message.getCode() == MessageStatus.GAME_OVER_NO_WINNER.getValue()) {
      ctx.status(200);
    } else {
      ctx.status(400);
    }
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
          + "'X' or " + "'O'; cannot accept " + submittedType);
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
