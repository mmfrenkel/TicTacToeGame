package models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameBoardTest {
	
	private GameBoard testBoard;
	private Player player1;
	
	@BeforeEach
	void setGameboard() {
		this.testBoard = new GameBoard();
		this.player1 = new Player('X', 1); 
	}
	
	@Test
	@DisplayName("Moves are not allowed in already occupied positions.")
	void testIsValidMoveFalse() {
		
		char[][] boardState = {{0, 0, 'O'}, {0, 0, 'X'}, {0, 0, 0}};
		testBoard.setBoardState(boardState); 
		
		Move attemptedMove = new Move(player1, 0, 2);
		assertEquals(false, testBoard.isValidMove(attemptedMove));
	}
	
	@Test
	@DisplayName("Moves are allowed in unoccupied positions.")
	void testIsValidMoveTrue() {
		
		char[][] boardState = {{0, 0, 'O'}, {0, 0, 'X'}, {0, 0, 0}};
		testBoard.setBoardState(boardState); 

		Move attemptedMove = new Move(player1, 0, 1);
		assertEquals(true, testBoard.isValidMove(attemptedMove));
	}
	
	@Test
	@DisplayName("Played move should reflect in gameboard.")
	void testPlayMove() {
		
		char[][] startingBoardState = {{0, 0, 'O'}, {0, 0, 'X'}, {0, 0, 0}};
		testBoard.setBoardState(startingBoardState); 

		Move move = new Move(player1, 0, 1);
		testBoard.playMove(move);
		char[][] expectedBoardState = {{0, 'X', 'O'}, {0, 0, 'X'}, {0, 0, 0}};
		
		assertArrayEquals(expectedBoardState, testBoard.getBoardState());
	}

	@Test
	@DisplayName("An empty board is not a winning configuration.")
	void testNotWinneEmptyBoard() {
		char[][] boardState = {{0, 0, 0}, {0, 0, 'X'}, {0, 0, 0}};
		
		testBoard.setBoardState(boardState);

		assertEquals(false, testBoard.isWinningMove(1, 2, 'X'));
	}
	
	@Test
	@DisplayName("Not a winner; Winning board expects the same character in a single row, column or horizontal.")
	void testNotWinner() {
		char[][] boardState = {{0, 'X', 0}, {0, 'O', 0}, {'X', 0, 0}};
		
		testBoard.setBoardState(boardState);
		
		assertEquals(false, testBoard.isWinningMove(2, 0, 'X'));
	}
	
	@Test
	@DisplayName("Winner; Left Horizontal Configuration.")
	void testWinnerLeftHorizontal() {
		
		// test game board data; game hasn't started
		char[][] boardState = {{'X', 0, 'O'}, {'O', 'X', 0}, {0, 0, 'X'}};
		
		testBoard.setBoardState(boardState);
		
		assertEquals(true, testBoard.isWinningMove(2, 2, 'X'));
	}
	
	@Test
	@DisplayName("Winner; Right Horizontal Configuration.")
	void testWinnerRightHorizontal() {
		
		// test game board data; game hasn't started
		char[][] boardState = {{0, 0, 'O'}, {'X', 'O', 0}, {'O', 0, 'X'}};
		
		testBoard.setBoardState(boardState);

		assertEquals(true, testBoard.isWinningMove(1, 1, 'O'));
	}
	
	@Test
	@DisplayName("Winner; Vertical Configuration.")
	void testWinnerRow() {
		
		// test game board data; game hasn't started
		char[][] boardState = {{'X', 0, 'O'}, {'X', '0', 0}, {'X', 0, 0}};
		
		testBoard.setBoardState(boardState);
		
		assertEquals(true, testBoard.isWinningMove(2, 0, 'X'));
	}
	
	@Test
	@DisplayName("Winner; Horizontal Configuration.")
	void testWinnerColumn() {
		
		// test game board data; game hasn't started
		char[][] boardState = {{'X', 'X', 'X'}, {'O', 'O', 0}, {0, 0, 'O'}};
		
		testBoard.setBoardState(boardState);
		
		assertEquals(true, testBoard.isWinningMove(0, 2, 'X'));
	}
	
	@Test
	@DisplayName("No moves have been played; the game board should be empty.")
	void testBoardEmpty() {
		
		GameBoard testBoard = new GameBoard();
		
		assertEquals(true, testBoard.isEmpty());
	}
	
	@Test
	@DisplayName("Moves have been played; the game board should not be empty.")
	void testBoardNotEmpty() {
		
		GameBoard testBoard = new GameBoard();

		char[][] boardState = {{0, 'O', 0}, {0, 0, 0}, {0, 0, 0}};		
		testBoard.setBoardState(boardState);
		
		assertEquals(false, testBoard.isEmpty());
	}
	
	@Test
	@DisplayName("Game board is partially filled so cannot be full.")
	void testNotFullBoard() {
		
		GameBoard testBoard = new GameBoard();
		
		// test game board data; game has started
		char[][] boardState = {{0, 'X', 0}, {0, 0, 'O'}, {0, 0, 0}};
		testBoard.setBoardState(boardState);
		
		assertEquals(false, testBoard.isFull());
	}
	
	@Test
	@DisplayName("Game board is filled; cannot add more moves.")
	void testFullBoard() {
		
		GameBoard testBoard = new GameBoard();
		
		// test game board data; cats game
		char[][] boardState = {{'X', 'O', 'X'}, {'O', 'O', 'X'}, {'O', 'X', 'X'}};
		
		testBoard.setBoardState(boardState);
		
		assertEquals(true, testBoard.isFull());
	}
}
