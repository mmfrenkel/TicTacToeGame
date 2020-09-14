package models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameBoardTest {
	
	private GameBoard emptyTestBoard;
	private GameBoard activeTestBoard;
	private char[][] emptyBoard = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
	private Player player1 = new Player('X', 1); 
	private Player player2 = new Player('O', 2);
	
	@BeforeEach
	void setGameboard() {
		this.emptyTestBoard = new GameBoard();
		this.activeTestBoard = new GameBoard(player1, player2, true, 1, emptyBoard, 0, false);
	}
	
	@Test
	@DisplayName("Moves are not allowed in already occupied positions.")
	void testIsValidMoveFalse() {
		
		char[][] boardState = {{0, 0, 'O'}, {0, 0, 'X'}, {0, 0, 0}};
		emptyTestBoard.setBoardState(boardState); 
		
		Move attemptedMove = new Move(player1, 0, 2);
		assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
	}
	
	@Test
	@DisplayName("Moves are allowed in unoccupied positions.")
	void testIsValidMoveTrue() {
		
		char[][] boardState = {{0, 0, 'O'}, {0, 0, 'X'}, {0, 0, 0}};
		emptyTestBoard.setBoardState(boardState); 

		Move attemptedMove = new Move(player1, 0, 1);
		assertEquals(true, emptyTestBoard.isValidMove(attemptedMove));
	}
	
	@Test
	@DisplayName("Played move should reflect in gameboard.")
	void testPlayMove() {
		
		char[][] startingBoardState = {{0, 0, 'O'}, {0, 0, 'X'}, {0, 0, 0}};
		emptyTestBoard.setBoardState(startingBoardState); 

		Move move = new Move(player1, 0, 1);
		emptyTestBoard.playMove(move);
		char[][] expectedBoardState = {{0, 'X', 'O'}, {0, 0, 'X'}, {0, 0, 0}};
		
		assertArrayEquals(expectedBoardState, emptyTestBoard.getBoardState());
	}

	@Test
	@DisplayName("An empty board is not a winning configuration.")
	void testNotWinneEmptyBoard() {
		char[][] boardState = {{0, 0, 0}, {0, 0, 'X'}, {0, 0, 0}};
		
		emptyTestBoard.setBoardState(boardState);

		assertEquals(false, emptyTestBoard.isWinningMove(1, 2, 'X'));
	}
	
	@Test
	@DisplayName("Not a winner; Winning board expects the same character in a single row, column or horizontal.")
	void testNotWinner() {
		char[][] boardState = {{0, 'X', 0}, {0, 'O', 0}, {'X', 0, 0}};
		
		emptyTestBoard.setBoardState(boardState);
		
		assertEquals(false, emptyTestBoard.isWinningMove(2, 0, 'X'));
	}
	
	@Test
	@DisplayName("Winner; Left Horizontal Configuration.")
	void testWinnerLeftHorizontal() {
		
		// test game board data; game hasn't started
		char[][] boardState = {{'X', 0, 'O'}, {'O', 'X', 0}, {0, 0, 'X'}};
		
		emptyTestBoard.setBoardState(boardState);
		
		assertEquals(true, emptyTestBoard.isWinningMove(2, 2, 'X'));
	}
	
	@Test
	@DisplayName("Winner; Right Horizontal Configuration.")
	void testWinnerRightHorizontal() {
		
		// test game board data; game hasn't started
		char[][] boardState = {{0, 0, 'O'}, {'X', 'O', 0}, {'O', 0, 'X'}};
		
		emptyTestBoard.setBoardState(boardState);

		assertEquals(true, emptyTestBoard.isWinningMove(1, 1, 'O'));
	}
	
	@Test
	@DisplayName("Winner; Vertical Configuration.")
	void testWinnerRow() {
		
		// test game board data; game hasn't started
		char[][] boardState = {{'X', 0, 'O'}, {'X', 0, 0}, {'X', 0, 0}};
		
		emptyTestBoard.setBoardState(boardState);
		
		assertEquals(true, emptyTestBoard.isWinningMove(2, 0, 'X'));
	}
	
	@Test
	@DisplayName("Winner; Horizontal Configuration.")
	void testWinnerColumn() {
		
		// test game board data; game hasn't started
		char[][] boardState = {{'X', 'X', 'X'}, {'O', 'O', 0}, {0, 0, 'O'}};
		
		emptyTestBoard.setBoardState(boardState);
		
		assertEquals(true, emptyTestBoard.isWinningMove(0, 2, 'X'));
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
	
	@Test()
	@DisplayName("Gameboard has no players, game has not started and player should not be able to make move.")
	void preventPlayingMoveIfMissingPlayers() {

		Move move = new Move(null, 0, 0);
	
		Message msg = emptyTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.MISSING_PLAYER.getValue(), msg.getCode());
	}
	
	@Test()
	@DisplayName("Gameboard has only 1 player, game has not started and player should not be able to make move.")
	void preventPlayingMoveIfMissingPlayer() {

		// configure game board for test
		char [][] boardState = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
		emptyTestBoard.setP1(player1);
		emptyTestBoard.setBoardState(boardState);
		
		Move move = new Move(player1, 0, 0);
		Message msg = emptyTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.MISSING_PLAYER.getValue(), msg.getCode());
	}

	@Test()
	@DisplayName("First player should always be the one to make the first move.")
	void testPlayerOneAlwaysPlaysFirst() {
		
		// configure game board for test
		Move move = new Move(player2, 0, 0);
		Message msg = activeTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.INVALID_ORDER_OF_PLAY.getValue(), msg.getCode());
	}

	
	@Test()
	@DisplayName("Player should not be able to make a move if it is not their turn.")
	void playerCannotMakeMoveIfNotTheirTurn() {
		
		// configure game board for test
		char [][] boardState = {{0, 'X', 0}, {0, 'O', 0}, {'X', 0, 0}};
		activeTestBoard.setBoardState(boardState);
		activeTestBoard.setTurn(2);
		
		Move move = new Move(player1, 0, 0);
		Message msg = activeTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.OTHER_PLAYERS_TURN.getValue(), msg.getCode());
	}
	
	
	@Test()
	@DisplayName("Player should not be able to make a move the position requested is already occupied.")
	void playerCannotMakeMoveToOccupiedPosition() {
		
		// configure game board for test
		char [][] boardState = {{0, 'X', 0}, {0, 'O', 0}, {'X', 0, 0}};
		activeTestBoard.setBoardState(boardState);
		activeTestBoard.setTurn(2);
		
		Move move = new Move(player2, 0, 1);  // this position is already occupied
		Message msg = activeTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.POSITION_NOT_ALLOWED.getValue(), msg.getCode());
	}

	@Test()
	@DisplayName("Player should not be able to make a move the position on the board that doesn't exist.")
	void playerCannotMakeMoveToNonexistentPosition() {
		
		// configure game board for test
		char [][] boardState = {{0, 'X', 0}, {0, 'O', 0}, {'X', 0, 0}};
		activeTestBoard.setBoardState(boardState);
		activeTestBoard.setTurn(2);
		
		Move move = new Move(player2, 4, 5);  // this position doesn't exist
		Message msg = activeTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.POSITION_NOT_ALLOWED.getValue(), msg.getCode());
	}
	
	@Test()
	@DisplayName("Player should not be able to continue playing if other player already won.")
	void playerCannotMakeMoveIfGameOver() {
		
		// configure game board for test
		char [][] boardState = {{'O', 'X', 0}, {'O', 'X', 0}, {'O', 0, 'X'}};
		activeTestBoard.setBoardState(boardState);
		activeTestBoard.setWinner(1);
		
		Move move = new Move(player1, 0, 2); 
		Message msg = activeTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.GAME_ALREADY_OVER.getValue(), msg.getCode());
	}
	
	@Test()
	@DisplayName("Player made winning move; game should report that they have won.")
	void playerMakesWinningMove() {
		
		// configure game board for test
		char [][] boardState = {{0, 'X', 0}, {'O', 'X', 0}, {'O', 0, 0}};
		activeTestBoard.setBoardState(boardState);
		
		Move move = new Move(player1, 2, 1);
		Message msg = activeTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.GAME_OVER_WINNER.getValue(), msg.getCode());
	}
	
	@Test()
	@DisplayName("Player made the last available move on the board, but no one won.")
	void playerMakesMoveForDraw() {
		
		// configure game board for test
		char [][] boardState = {{'O', 'X', 'O'}, {'X', 'O', 'X'}, {'X', 0, 'X'}};
		activeTestBoard.setBoardState(boardState);
		activeTestBoard.setTurn(2);
		
		Move move = new Move(player2, 2, 1);  // this position is already occupied
		Message msg = activeTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.GAME_OVER_NO_WINNER.getValue(), msg.getCode());
	}
	
	
	@Test()
	@DisplayName("Player made move; no one has won and no draw yet.")
	void turnSwitchesToOtherPlayerIfNoWinner() {
		// configure game board for test
		char [][] boardState = {{0, 'X', 0}, {0, 'O', 0}, {0, 0, 0}};
		activeTestBoard.setBoardState(boardState);
		
		Move move = new Move(player1, 0, 2); 
		Message msg = activeTestBoard.processPlayerMove(move);
		
		assertEquals(MessageStatus.SUCCESS.getValue(), msg.getCode());
		assertEquals(activeTestBoard.getTurn(), 2);
	}
}
