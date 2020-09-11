package models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameBoardTest {
	
	private GameBoard testBoard;
	
	@BeforeEach
	void setGameboard() {
		this.testBoard = new GameBoard();
	}

	@Test
	@DisplayName("An empty board is not a winning configuration.")
	void testNotWinneEmptyBoard() {
		char[][] boardState = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
		
		testBoard.setBoardState(boardState);

		assertEquals(false, testBoard.isWinningMove('X'));
	}
	
	@Test
	@DisplayName("Not a winner; Winning board expects the same character in a single row, column or horizontal.")
	void testNotWinner() {
		char[][] boardState = {{0, 'X', 0}, {0, 'O', 0}, {'X', 0, 0}};
		
		testBoard.setBoardState(boardState);
		
		assertEquals(false, testBoard.isWinningMove('X'));
	}
	
	@Test
	@DisplayName("Winner; Winning board expects the same character in a single row, column or horizontal.")
	void testWinner() {
		
		// test game board data; game hasn't started
		char[][] boardState = {{'X', 0, 'O'}, {'O', 'X', 0}, {0, 0, 'X'}};
		
		testBoard.setBoardState(boardState);
		
		assertEquals(true, testBoard.isWinningMove('X'));
	}

	@Test
	@DisplayName("Game board is partially filled so cannot be full.")
	void testNotFullBoard() {
		
		GameBoard testBoard = new GameBoard();
		
		// test game board data; game hasn't started
		char[][] boardState = {{0, 'X', 0}, {0, 0, 'O'}, {0, 0, 0}};
		
		testBoard.setBoardState(boardState);
		
		assertEquals(false, testBoard.isFull());
	}
	
	@Test
	@DisplayName("Game board is filled; cannot add more moves.")
	void testFullBoard() {
		
		GameBoard testBoard = new GameBoard();
		
		// test game board data; game hasn't started
		char[][] boardState = {{'X', 'O', 'X'}, {'O', 'O', 'X'}, {'O', 'X', 'X'}};
		
		testBoard.setBoardState(boardState);
		
		assertEquals(true, testBoard.isFull());
	}
}
