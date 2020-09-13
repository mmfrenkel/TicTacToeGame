package controllers;

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
		// Add player one to the game
		assignPlayerOne(ctx);
		logger.info("Added first player to the game. Player 1: " + gameBoard.getP1());
		
		ctx.json(gameBoard);
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
			ctx.redirect("/newgame");
			return ctx;
		}

		assignPlayerTwo();
		setGameReady();
		logger.info("Added second player to the game; game is ready. Player 2: " + gameBoard.getP2());

		ctx.redirect("/tictactoe.html?p=2");
		return ctx;
	}

	/**
	 * Sets the game as 'started'; this happens only after both players one and two
	 * are assigned.
	 */
	private void setGameReady() {
		gameBoard.setGameStarted(true);
	}

	/**
	 * Handles the move submitted by a user, checking several erroneous
	 * circumstances, including lack of players, invalid ordering of operations,
	 * another player's turn, occupied/invalid position and game already over. Moves
	 * are played only if the move is determined to be valid under each of these
	 * rules. Regardless, the outcome is reflected in the updated Context object.
	 * 
	 * @param ctx Context object from incoming request
	 * @return Updated Context object
	 */
	public Context processPlayerMove(Context ctx) {
		Move move = parseMoveFromRequest(ctx);
		Message message;

		/* ---- Need to check several states to make sure move is valid ---- */
		// 1. If there aren't two players, game has not started and cannot make move
		if (!gameBoard.isGameStarted()) {
			message = new Message(false, MessageStatus.MISSING_PLAYER,
					"Game cannot start until there are two players on the game board!");
		}
		// 2. First player should always be the one to make the first move
		else if (gameBoard.isEmpty() && move.getPlayerId() == 2) {
			message = new Message(false, MessageStatus.INVALID_ORDER_OF_PLAY,
					"Player 1 makes the first move on an empty board!");
		}
		// 3. If it's not the player's turn, cannot make move
		else if (move.getPlayerId() != gameBoard.getTurn()) {
			message = new Message(false, MessageStatus.OTHER_PLAYERS_TURN,
					"It is currently Player " + gameBoard.getTurn() + "'s turn!");
		}
		// 4. If the submitted move is not available, cannot make move
		else if (!gameBoard.isValidMove(move)) {
			message = new Message(false, MessageStatus.POSITION_NOT_ALLOWED, "Invalid move (" + move.getMoveX() + ", "
					+ move.getMoveY() + "); please choose unoccupied position within coordinates (0,0) to (2,2).");
		}
		// 5. If the board was already won, then cannot make another move
		else if (gameBoard.getWinner() != 0) {
			message = new Message(false, MessageStatus.GAME_ALREADY_OVER,
					"Game is already over! Player " + gameBoard.getWinner() + " won!");
		}
		// 6. Move is valid and should be played
		else {
			gameBoard.playMove(move);

			// 6a. If winning move, game over
			if (gameBoard.getWinner() != 0) {
				message = new Message(true, MessageStatus.GAME_OVER_WINNER,
						"Player " + gameBoard.getWinner() + " is the winner!");
			}
			// 6b. If draw and no one can win
			else if (gameBoard.isFull()) {
				message = new Message(true, MessageStatus.GAME_OVER_NO_WINNER, "Game Over! Nobody wins.");
			}
			// 6c. No winners or draw yet
			else {
				message = new Message(true, MessageStatus.SUCCESS, "Player " + move.getPlayerId() + " made move at ("
						+ move.getMoveX() + ", " + move.getMoveY() + ").");

				// swap turns for players
				gameBoard.setTurn(move.getPlayerId() == 1 ? 2 : 1);
			}

		}
		ctx.json(message);
		ctx.status(200);
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
	 * Extracts submitted information from context and returns a new Move() object
	 * representing the requested move from the user. To protect against invalid
	 * submissions by users accessing the game via an API interaction (instead of
	 * UI), this method checks to be sure that both coordinates are submitted and
	 * both are integer values.
	 * 
	 * @param ctx
	 * @return new instance of Move object
	 * @throws BadRequestResponse
	 */
	Move parseMoveFromRequest(Context ctx) {
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
			throw new BadRequestResponse("Players can only submit integer values to indiciate " + "a gave move; got "
					+ moveX + " and " + moveY);
		}

		Move playerMove = new Move(currentPlayer, x, y);
		return playerMove;
	}

	public GameBoard getGameBoard() {
		return gameBoard;
	}

	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}
}
