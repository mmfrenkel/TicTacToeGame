package models;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

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

		if (x >= ROWS || y >= COLUMNS || x < 0 || y < 0) {
			// user trying to play position out of range
			System.out.println("Position (" + x + ", " + y + ") is out of range");
			return false;
		}

		if (this.boardState[x][y] != 0) {
			// this location on the board is already taken
			System.out.println("Position (" + x + ", " + y + ") is already taken");
			return false;
		}
		return true;
	}

	/**
	 * Plays the Move submitted, adding it to the board and checking to
	 * see if it was a winning move; if so, the board.
	 * @param move  Instance of Move object
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
	 * @param type   Character, either 'X' or 'O'
	 * @return If    the last move was a winning move
	 */
	public boolean isWinningMove(int x, int y, char type) {
		return winningRow(x, type) || winningColumn(y, type) || winningHorizontal(type);
	}

	/**
	 * Given a row index and a move type, determine if the 
	 * move completes the row specified.
	 * @param row    integer, index of a row
	 * @param type   char, a move type; either 'X' or 'O'
	 * @return       if it is a winning row
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
	 * Given a column index and a move type, determine if the 
	 * move completes the column specified.
	 * @param column integer, index of a column
	 * @param type   char, a move type; either 'X' or 'O'
	 * @return       if it is a winning column
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
	 * Given a row index and a move type, determine if the 
	 * move completes the row specified.
	 * @param row    integer, index of a row
	 * @param type   char, a move type; either 'X' or 'O'
	 * @return       if it is a winning row
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

		if (r != 0) {   // left horizontal works!
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
	
	public List<Character> acceptedTypes() {
		return this.ACCEPTED_TYPES;
	}

	public void printBoard() {
		System.out.println("-----");
		for (char[] arr: this.getBoardState()) {
			for(char c: arr) {
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
	
	public JSONObject asJson() {
		JSONObject boardAsJson = new JSONObject();
		
		if (p1 != null) {
			JSONObject p1Json = new JSONObject();
			p1Json.put("type", Character.toString(p1.getType()));
			p1Json.put("id", p1.getId());
			boardAsJson.put("p1", p1Json);
		}
		
		if (p2 != null) {
			JSONObject p2Json = new JSONObject();
			p2Json.put("type", Character.toString(p1.getType()));
			p2Json.put("id", p2.getId());
			boardAsJson.put("p2", p2Json);
		}
		
		JSONArray jsonBoardState = new JSONArray();
		for (char[] row: boardState) {
			JSONArray rowAsJson = new JSONArray();
			
			for (char pos: row) {
				if (pos == 0) {
					rowAsJson.put("\u0000");
				} else {
					rowAsJson.put(Character.toString(pos));
				}
			}
			jsonBoardState.put(rowAsJson);
		}
		
		boardAsJson.put("gameStarted", this.isGameStarted());
		boardAsJson.put("turn", this.getTurn());
		boardAsJson.put("boardState",jsonBoardState);
		boardAsJson.put("winner", this.getWinner());
		boardAsJson.put("isDraw", this.isDraw());
		return boardAsJson;
	}
}
