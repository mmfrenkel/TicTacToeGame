package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import models.GameBoard;
import models.Message;
import models.Move;
import models.Player;

public class TicTacToeController {

	private GameBoard gameBoard;

	private static Logger logger = LoggerFactory.getLogger(PlayGame.class);
	
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
		return ctx;
	};

	/**
	 * Creates player one with their selected type (i.e., 'X' or 'O'). First player
	 * to join game will always be player 1; however the game does not officially
	 * start until player 2 joins.
	 * 
	 * @param ctx Context object for incoming request
	 * @return Updated Context object
	 */
	public Context startGame(Context ctx) {
		
		// Parse player 1 information then add player one to the game
		Player player1 = parsePlayerOneFromRequest(ctx);
		gameBoard.setP1(player1);
		logger.info("Added first player to the game: " + player1);
		logger.info(gameBoard.toString());

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
	 */
	public Context addSecondPlayer(Context ctx) {

		// if first player doesn't already exist, then
		// redirect to the start game end point so they can choose a
		if (gameBoard.getP1() == null) {
			logger.info("Currently there is no game to join (no Player 1 yet). "
					+ "Redirecting to new game. Board State: " + gameBoard);
			ctx.redirect("/newgame");
			return ctx;
		}

		gameBoard.autoSetP2();
		gameBoard.setGameStarted(true);
		logger.info("Added second player to the game; game board is now ready. Player 2: " + gameBoard.getP2());

		ctx.redirect("/tictactoe.html?p=2");
		return ctx;
	}

	/**
	 * Handles the move submitted by a user, parsing move from request and sending
	 * move to the game board.
	 * 
	 * @param ctx Context object from incoming request
	 * @return Updated Context object
	 */
	public Context processPlayerMove(Context ctx) {

		Move move = parseMoveFromRequest(ctx);
		logger.info("Handling move submitted by user: " + move);

		Message message = gameBoard.processPlayerMove(move);

		ctx.result(gson.toJson(message));
		return ctx;
	}

	/**
	 * Gets the 'type' that the first player selected then create the new player and
	 * adds the player to the game board.
	 * 
	 * @param ctx Context object
	 * @return new Player parsed from Context object
	 * @throws BadRequestResponse if form parameter 'type' isn't one of expected
	 *                            values; default Javalin 400 response
	 */
	private Player parsePlayerOneFromRequest(Context ctx) throws BadRequestResponse {

		// options for the form parameter "type" are "X" or "O"
		String submittedType = ctx.formParam("type");

		if (submittedType == null || !gameBoard.acceptedTypes().contains(submittedType.charAt(0))) {
			// the form parameter isn't what we expected; either it's missing or not
			// one of the accepted types, raise custom exception; use default Javalin 400
			// response
			throw new BadRequestResponse(
					"First player should select either " + "'X' " + "or 'O'; cannot accept " + submittedType);
		}

		char playerType = submittedType.charAt(0);

		return new Player(playerType, 1);
	}

	/**
	 * Extracts submitted information from context and returns a new Move() object
	 * representing the requested move from the user. To protect against invalid
	 * submissions by users accessing the game via an API interaction (instead of
	 * UI), this method checks to be sure that both coordinates are submitted and
	 * both are integer values.
	 * 
	 * @param ctx
	 * @return new instance of Move object
	 * @throws BadRequestResponse if player doesn't exist, missing information
	 */
	private Move parseMoveFromRequest(Context ctx) throws BadRequestResponse {
		// parse information from context; note OK if currentPlayer is null; in this
		// case, move is unassigned to a player and issue will be handled by calling method
		Player currentPlayer = ctx.pathParam("playerId").equals("1") ? gameBoard.getP1() : gameBoard.getP2();

		String moveX = ctx.formParam("x");
		String moveY = ctx.formParam("y");

		// test cases where user is playing via API end points; these issues are not
		// possible when using the UI but should be considered
		if (moveX == null || moveY == null) {
			throw new BadRequestResponse("To make a game move, players must submit a board "
					+ "row number (X) and column number (Y) (e.g., x=0&y=0");
		}
		
		if (currentPlayer == null) {
			throw new BadRequestResponse("In order to process a move, the ID of the player "
					+ "must be specified. Got null.");
		}

		int x, y;
		try {
			x = Integer.parseInt(moveX);
			y = Integer.parseInt(moveY);
		} catch (NumberFormatException nfe) {
			// position played is not, in fact, represented by numbers
			throw new BadRequestResponse("Players can only submit integer values to indiciate " + "a gave move; got "
					+ moveX + " and " + moveY);
		}

		Move playerMove = new Move(currentPlayer, x, y);
		return playerMove;
	}

	public GameBoard getGameBoard() {
		return gameBoard;
	}
	
	public String getGameBoardAsJson() {
		return gson.toJson(gameBoard);
	}

	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}
}
