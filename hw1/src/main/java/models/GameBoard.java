package models;

public class GameBoard {

	private Player p1;

	private Player p2;

	private boolean gameStarted;

	private int turn;

	private char[][] boardState;

	private int winner;

	private boolean isDraw;

	final private int COLUMNS = 3;

	final private int ROWS = 3;

	/* Primary Constructor for GameBoard() */
	public GameBoard() {
		this.p1 = null;
		this.p2 = null;
		this.gameStarted = false;
		this.turn = 1; 								// p1 always goes first, even if we don't have p1 yet
		this.boardState = new char[ROWS][COLUMNS];  // default value of char data type is '\u0000
		this.winner = 0;
		this.isDraw = false;
	}

	/**
	 * Is the game board already full without a winner? Then its a draw game and no
	 * one wins.
	 * 
	 * @return If the game board is full
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
	 * Is the move provided a valid move (i.e., to a position that is currently
	 * unoccupied?
	 * 
	 * @param move Instance of Move object
	 * @return If the Move is valid
	 */
	public boolean isValidMove(Move move) {
		int x = move.getMoveX();
		int y = move.getMoveY();

		if (x >= ROWS || y <= COLUMNS) {
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
	 * was a winning move; if so, the board.
	 * 
	 * @param move Instance of Move object
	 */
	public void playMove(Move move) {
		int x = move.getMoveX();
		int y = move.getMoveY();
		char type = move.getPlayer().getType();

		this.boardState[x][y] = type;

		if (isWinningMove(type)) {
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
	public boolean isWinningMove(char type) {
		return winningRow(type) || winningColumn(type) || winningHorizontal(type);
	}

	private boolean winningRow(char type) {
		// check if there is a winner in the column set
		int r = 0, c = 0;

		// check for winning rows first
		while (r < ROWS) {
			c = 0;  // go back to the first column
			while (c < COLUMNS) {
				if (this.boardState[r][c] != type) {
					c = 0; // marker that test failed
					break;
				}
				c++;
			}
			if (c != 0) { // we found a winning row!
				return true;
			}
			r++; // otherwise check the next row
		}
		return false;
	}

	private boolean winningColumn(char type) {
		// check if there is a winner in the column set
		int r = 0, c = 0;

		while (c < COLUMNS) {
			r = 0;
			while (r < ROWS) {
				if (this.boardState[r][c] != type) {
					r = 0; // marker that test failed
					break;
				}
				r++;
			}
			if (r != 0) { // we found a winning column!
				return true;
			}
			c++; // otherwise check the next column
		}
		return false;
	}

	private boolean winningHorizontal(char type) {
		// check if there is a winner in the column set
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

		// did we find it on the left horizontal already?
		if (r != 0) {
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

		// did we find it on the right horizontal?
		if (r != 0) {
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

	public void setP2(Player p2) {
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

}
