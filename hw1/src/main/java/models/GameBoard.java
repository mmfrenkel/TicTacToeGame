package models;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.Expose;

public class GameBoard {

	/*
	 * -- @Expose to tell gson to add only the below fields to json returned to user
	 * --
	 */

	@Expose
	private Player p1;

	@Expose
	private Player p2;

	@Expose
	private boolean gameStarted;

	@Expose
	private int turn;

	@Expose
	private char[][] boardState;

	@Expose
	private int winner;

	@Expose
	private boolean isDraw;

	/* -- end fields to serialize here -- */

	final private int COLUMNS = 3;

	final private int ROWS = 3;

	final private List<Character> ACCEPTED_TYPES = Arrays.asList('X', 'O');

	/* Primary Constructor for GameBoard() */
	public GameBoard() {
		this.p1 = null;
		this.p2 = null;
		this.gameStarted = false;
		this.turn = 1; // p1 always goes first, even if we don't have p1 yet
		this.boardState = new char[COLUMNS][ROWS];
		this.winner = 0;
		this.isDraw = false;
	}

	/* Secondary Constructor helpful for easy testing */
	public GameBoard(Player p1, Player p2, boolean gameStarted, int turn, 
			char[][] state, int winner, boolean isDraw) {
		this.p1 = p1;
		this.p2 = p2;
		this.gameStarted = gameStarted;
		this.turn = turn;
		this.boardState = state;
		this.winner = winner;
		this.isDraw = isDraw;
	}

	/**
	 * Is the game board currently empty?
	 * 
	 * @return true if game board is empty (no moves made yet), else false
	 */
	public boolean isEmpty() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				if (this.boardState[i][j] != 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Is the game board already full without a winner? Then its a draw game and no
	 * one wins.
	 * 
	 * @return true if the game board is full, else false
	 */
	public boolean isFull() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				if (this.boardState[i][j] == 0) {
					return false;
				}
			}
		}
		this.setDraw(true);
		return true;
	}

	/**
	 * Handles the move submitted by a user, checking several erroneous
	 * circumstances, including lack of players, invalid ordering of operations,
	 * another player's turn, occupied/invalid position and game already over. Moves
	 * are played only if the move is determined to be valid under each of these
	 * rules. The result of the move is returned as a Move object
	 * 
	 * @param move instance of player Move
	 * @return Message() object, reflecting outcome of Move
	 */
	public Message processPlayerMove(Move move) {
		Message message;

		/* ---- Need to check several states to make sure move is valid ---- */
		// 1. If there aren't two players, game has not started and cannot make move
		if (!isGameStarted()) {
			message = new Message(false, MessageStatus.MISSING_PLAYER,
					"Game cannot start until there are two players on the game board!");
		}
		// 2. First player should always be the one to make the first move
		else if (isEmpty() && move.getPlayerId() == 2) {
			message = new Message(false, MessageStatus.INVALID_ORDER_OF_PLAY,
					"Player 1 makes the first move on an empty board!");
		}
		// 3. If it's not the player's turn, cannot make move
		else if (move.getPlayerId() != getTurn()) {
			message = new Message(false, MessageStatus.OTHER_PLAYERS_TURN,
					"It is currently Player " + getTurn() + "'s turn!");
		}
		// 4. If the submitted move is not available, cannot make move
		else if (!isValidMove(move)) {
			message = new Message(false, MessageStatus.POSITION_NOT_ALLOWED,
					"Cannot move " + move.getPlayer().getType() + "(" + move.getMoveX() + ", " + move.getMoveY()
							+ "); please choose unoccupied position within coordinates (0,0) to (2,2).");
		}
		// 5. If the board was already won, then cannot make another move
		else if (getWinner() != 0) {
			message = new Message(false, MessageStatus.GAME_ALREADY_OVER,
					"Game is already over! Player " + getWinner() + " won!");
		}
		// 6. Move is valid and should be played
		else {
			playMove(move);

			// 6a. If winning move, game over
			if (getWinner() != 0) {
				message = new Message(true, MessageStatus.GAME_OVER_WINNER,
						"Player " + getWinner() + " is the winner!");
			}
			// 6b. If draw and no one can win
			else if (isFull()) {
				message = new Message(true, MessageStatus.GAME_OVER_NO_WINNER, "Game Over! Nobody wins.");
			}
			// 6c. No winners or draw yet
			else {
				message = new Message(true, MessageStatus.SUCCESS, "Player " + move.getPlayerId() + " made move at ("
						+ move.getMoveX() + ", " + move.getMoveY() + ").");

				// swap turns for players
				setTurn(move.getPlayerId() == 1 ? 2 : 1);
			}

		}
		return message;
	}

	/**
	 * Is the move provided a valid move (i.e., to a position that is currently
	 * unoccupied?
	 * 
	 * @param move Instance of Move object
	 * @return If the Move is valid
	 */
	public boolean isValidMove(Move move) {
		int x = move.getMoveX();
		int y = move.getMoveY();

		if (x >= ROWS || y >= COLUMNS || x < 0 || y < 0) {
			// user trying to play position out of range
			return false;
		}

		if (this.boardState[x][y] != 0) {
			// this location on the board is already taken
			return false;
		}
		return true;
	}

	/**
	 * Plays the Move submitted, adding it to the board and checking to see if it
	 * was a winning move; if so, update the board to reflect the change in board
	 * state .
	 * 
	 * @param move Instance of Move object
	 */
	public void playMove(Move move) {
		int x = move.getMoveX();
		int y = move.getMoveY();
		char type = move.getPlayer().getType();

		this.boardState[x][y] = type;

		if (isWinningMove(x, y, type)) {
			int playerId = move.getPlayer().getId();
			this.setWinner(playerId);
		}
	}

	/**
	 * Determines whether or not the most recently submitted move resulted in a
	 * winner configuration on the game board.
	 * 
	 * @param type Character, either 'X' or 'O'
	 * @return If the last move was a winning move
	 */
	public boolean isWinningMove(int x, int y, char type) {
		return winningRow(x, type) || winningColumn(y, type) || winningHorizontal(type);
	}

	/**
	 * Given a row index and a move type, determine if the move completes the row
	 * specified.
	 * 
	 * @param row  integer, index of a row
	 * @param type char, a move type; either 'X' or 'O'
	 * @return if it is a winning row
	 */
	private boolean winningRow(int row, char type) {
		int column = 0;

		while (column < COLUMNS) {
			if (this.boardState[row][column] != type) {
				return false;
			}
			column++;
		}
		return true;
	}

	/**
	 * Given a column index and a move type, determine if the move completes the
	 * column specified.
	 * 
	 * @param column integer, index of a column
	 * @param type   char, a move type; either 'X' or 'O'
	 * @return if it is a winning column
	 */
	private boolean winningColumn(int column, char type) {
		int row = 0;

		while (row < ROWS) {
			if (this.boardState[row][column] != type) {
				return false;
			}
			row++;
		}
		return true;
	}

	/**
	 * Given a row index and a move type, determine if the move completes the row
	 * specified.
	 * 
	 * @param row  integer, index of a row
	 * @param type char, a move type; either 'X' or 'O'
	 * @return if it is a winning row
	 */
	private boolean winningHorizontal(char type) {
		int r = 0, c = 0;

		// check left diagonal i.e., \
		while (c < COLUMNS && r < ROWS) {
			if (this.boardState[r][c] != type) {
				r = 0; // marker that test failed
				break;
			}
			c++;
			r++;
		}

		if (r != 0) { // left horizontal works!
			return true;
		}

		// now check right diagonal, i.e., /
		c = COLUMNS - 1;
		while (c >= 0 && r < ROWS) {
			if (this.boardState[r][c] != type) {
				r = 0; // marker that test failed
				break;
			}
			c--;
			r++;
		}
		if (r != 0) { // right horizontal works!
			return true;
		}
		return false;
	}

	public Player getP1() {
		return p1;
	}

	public void setP1(Player p1) {
		this.p1 = p1;
	}

	public Player getP2() {
		return p2;
	}

	/**
	 * Method to auto-set player 2 as the player type that player 1 is not.
	 */
	public void autoSetP2() {
		char playerType = getP1().getType() == 'X' ? 'O' : 'X';
		Player p2 = new Player(playerType, 2);
		this.p2 = p2;
	}

	/**
	 * Set player 2 manually, with the risk of accidentally trying to assign a
	 * player whose type has already been taken (i.e., when Player 1 has already
	 * chosen 'X' as it's type you cannot set Player 2 to also have 'X').
	 * 
	 * @param p2 Instance of Player object, representing the second player to join
	 *           game
	 * @throws InvalidGameBoardConfigurationException
	 */
	public void setP2(Player p2) throws InvalidGameBoardConfigurationException {

		// catch scenarios where player 2 wants to be 'X' but player 1 already is
		if (p2.getType() == getP1().getType()) {
			throw new InvalidGameBoardConfigurationException(
					"Player 1 already has selected '" + p2.getType() + "'. Cannot have two players with the same type");
		}
		this.p2 = p2;
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	public void setGameStarted(boolean gameStarted) {
		this.gameStarted = gameStarted;
	}

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public char[][] getBoardState() {
		return boardState;
	}

	public void setBoardState(char[][] boardState) {
		this.boardState = boardState;
	}

	public int getWinner() {
		return winner;
	}

	public void setWinner(int winner) {
		this.winner = winner;
	}

	public boolean isDraw() {
		return isDraw;
	}

	public void setDraw(boolean isDraw) {
		this.isDraw = isDraw;
	}

	public List<Character> acceptedTypes() {
		return this.ACCEPTED_TYPES;
	}

	/**
	 * Prints out the game board as a 3 x 3 square, visually similar to the board
	 * shown in web UI.
	 */
	public void printBoard() {
		System.out.println("-----");
		for (char[] arr : this.getBoardState()) {
			for (char c : arr) {
				if (c == 0) {
					System.out.print("- ");
				} else {
					System.out.print(c + " ");
				}
			}
			System.out.println();
		}
		System.out.println("-----");
	}
}
