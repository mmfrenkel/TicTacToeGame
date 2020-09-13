package controllers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import models.GameBoard;
import models.Message;
import models.MessageStatus;
import models.Move;
import models.Player;

public class TicTacToeController {

	private GameBoard gameBoard;

	private static Logger logger = LoggerFactory.getLogger(PlayGame.class);

	public TicTacToeController(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	public static Context serveNewGame(Context ctx) {
		logger.info("Received request to serve main page.");
		ctx.redirect("/tictactoe.html");
		return ctx;
	};

	public Context startGame(Context ctx) {
		// Add player one to the game
		assignPlayerOne(ctx);

		JSONObject boardAsJson = convertGameBoardToJSON();
		ctx.result(boardAsJson.toString());
		ctx.contentType("application/json");
		ctx.status(200);
		return ctx;
	}

	/**
	 * Adds second player to the game; if first player doesn't yet exist, then
	 * redirects user to the new game end point.
	 * 
	 * @param ctx Context object
	 * @return Updated Context object
	 */
	public Context addSecondPlayer(Context ctx) {

		// if first player doesn't already exist, then
		// redirect to the start game end point
		if (gameBoard.getP1() == null) {
			logger.info("Player one doesn't exist yet.");
			ctx.redirect("/newgame");
			return ctx;
		}

		logger.info("Adding second player to the game. Player 1: " + gameBoard.getP1());
		assignPlayerTwo();

		ctx.redirect("/tictactoe.html?p=2");
		return ctx;
	}

	public void startGame() {
		gameBoard.setGameStarted(true);
	}

	public Context processPlayerMove(Context ctx) {

		// parse information from context
		Player currentPlayer = ctx.pathParam("playerId") == "1" ? gameBoard.getP1() : gameBoard.getP2();
		String moveX = ctx.formParam("x");
		String moveY = ctx.formParam("y");

		// test cases where user is playing via API end points; these two issues are not
		// possible when using the UI but should be considered
		if (moveX == null || moveY == null) {
			throw new BadRequestResponse("To make a game move, players must submit a board "
					+ "row number (X) and column number (Y) (e.g., x=0&y=0");
		}

		int x, y;
		try {
			x = Integer.parseInt(moveX);
			y = Integer.parseInt(moveX);
		} catch (NumberFormatException nfe) {
			// position played is not, in fact, represented by numbers
			throw new BadRequestResponse("Players can only submit integer values to " + "indiciate a gave move.");
		}

		Move playerMove = new Move(currentPlayer, x, y);
		Message message;
		if (!gameBoard.isValidMove(playerMove)) {
			message = new Message(false, MessageStatus.POSITION_NOT_ALLOWED,
					"Invalid move; choose unoccupied position within " + "coordinates 0,0 to 2,2");
		} else {
			// play move
			gameBoard.playMove(playerMove);
			message = new Message(true, MessageStatus.SUCCESS,
					"Player " + currentPlayer.getId() + " made move at (" + moveX + ", " + moveY + ").");
		}

		ctx.json(message);
		return ctx;
	}

	/**
	 * Gets the 'type' that the first player selected then create the new player and
	 * adds the player to the game board.
	 * 
	 * @param ctx Context object
	 * @throws BadRequestResponse if form parameter 'type' isn't one of expected
	 *                            values; default Javalin 400 response
	 */
	private void assignPlayerOne(Context ctx) throws BadRequestResponse {

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

		Player p1 = new Player(playerType, 1);
		gameBoard.setP1(p1);
	}

	/**
	 * Creates second player, assigning it's type based on whichever move type isn't
	 * taken ('X' or 'O') and adds player to the game board.
	 */
	private void assignPlayerTwo() {
		// assign second player to the type that wasn't already selected
		char playerType = gameBoard.getP1().getType() == 'X' ? 'O' : 'X';
		Player p2 = new Player(playerType, 2);

		gameBoard.setP1(p2);
	}

	/**
	 * Converts the game board into its JSON equivalent, with the format expected by
	 * the requesting program. Note that Jackson and other tools that auto convert
	 * classes to their JSON equivalents did not correctly convert empty game board
	 * spaces to '\u0000` or name the fields correctly; hence a custom approach was
	 * taken.
	 * 
	 * @return JSONObject representing the current game board state
	 */
	public JSONObject convertGameBoardToJSON() {
		JSONObject boardAsJson = new JSONObject();

		// 1. add player information, if they exist
		if (gameBoard.getP1() != null) {
			JSONObject p1Json = new JSONObject();
			p1Json.put("type", Character.toString(gameBoard.getP1().getType()));
			p1Json.put("id", gameBoard.getP1().getId());
			boardAsJson.put("p1", p1Json);
		}
		if (gameBoard.getP2() != null) {
			JSONObject p2Json = new JSONObject();
			p2Json.put("type", Character.toString(gameBoard.getP2().getType()));
			p2Json.put("id", gameBoard.getP2().getId());
			boardAsJson.put("p2", p2Json);
		}

		// 2. format game board state, setting null values to \u0000 encoding
		JSONArray jsonBoardState = new JSONArray();
		for (char[] row : gameBoard.getBoardState()) {
			JSONArray rowAsJson = new JSONArray();

			for (char pos : row) {
				if (pos == 0) {
					rowAsJson.put("\u0000");
				} else {
					rowAsJson.put(Character.toString(pos));
				}
			}
			jsonBoardState.put(rowAsJson);
		}

		// 3. add all fields to the new json object, correctly named
		boardAsJson.put("gameStarted", gameBoard.isGameStarted());
		boardAsJson.put("turn", gameBoard.getTurn());
		boardAsJson.put("boardState", jsonBoardState);
		boardAsJson.put("winner", gameBoard.getWinner());
		boardAsJson.put("isDraw", gameBoard.isDraw());
		return boardAsJson;
	}
}
