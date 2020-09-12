package controllers;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Context;
import models.GameBoard;
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
		try {
			assignPlayerOne(ctx);
		} catch (InvalidGameParameter igp) {
			// IGP exception thrown when user submits bad type
			ctx.status(400).result(igp.getMessage());
			return ctx;
		}

		JSONObject boardAsJson = gameBoard.asJson();
		ctx.status(200).result(boardAsJson.toString()).contentType("application/json");
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

	/**
	 * Gets the 'type' that the first player selected then create the new player and
	 * adds the player to the game board.
	 * 
	 * @param ctx Context object
	 * @throws InvalidGameParameter if form parameter 'type' isn't one of expected
	 *                              values
	 */
	private void assignPlayerOne(Context ctx) throws InvalidGameParameter {

		// options for the form parameter "type" are "X" or "O"
		String submittedType = ctx.formParam("type");

		if (submittedType == null || !gameBoard.acceptedTypes().contains(submittedType.charAt(0))) {
			// the form parameter isn't what we expected; either it's missing or not
			// one of the accepted types, raise custom exception
			throw new InvalidGameParameter(
					"First player should select either " + "'X' or 'O'; cannot accept " + submittedType);
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
	
	
}
