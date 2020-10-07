package unit.models;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import models.GameBoard;
import models.GameBoardInternalError;
import models.InvalidGameBoardConfigurationException;
import models.Message;
import models.MessageStatus;
import models.Move;
import models.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.DbServiceException;
import util.TicTacToeSqliteDbService;

class GameBoardTest {

  private GameBoard emptyTestBoard;
  private GameBoard activeTestBoard;
  private TicTacToeSqliteDbService dbService;
  private char[][] emptyBoard = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
  private Player player1 = new Player('X', 1);
  private Player player2 = new Player('O', 2);

  /**
   * Setup an empty and active GameBoard for each test.
   */
  @BeforeEach
  void setGameboard() {
    
    // set up a mock for the database service
    dbService = mock(TicTacToeSqliteDbService.class);
    
    this.emptyTestBoard = new GameBoard(dbService);
    this.activeTestBoard = new GameBoard(player1, player2, true, 1, 
        emptyBoard, 0, false, dbService);
  }

  /**
   * Test move attempts to already occupied positions.
   */
  @Test
  @DisplayName("Moves are not allowed in already occupied positions.")
  void testIsValidMoveFalse() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 0, 2);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }

  /**
   * Test that it is possible to make a move to an unoccupied position 
   * that is at a valid location on the gameboard.
   */
  @Test
  @DisplayName("Moves are allowed in unoccupied positions.")
  void testIsValidMoveTrue() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 0, 1);
    assertEquals(true, emptyTestBoard.isValidMove(attemptedMove));
  }

  /**
   * Test that it is not possible to make moves to positions off the gameboard
   * (row out of range).
   */
  @Test
  @DisplayName("Moves are not allowed in positions off game board "
      + "(row doesn't exist #1).")
  void testIsValidMoveFalseRowOffBoard1() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 3, 0);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }

  /**
   * Test that it is not possible to make moves to positions off the gameboard
   * (row out of range; negative value).
   */
  @Test
  @DisplayName("Moves are not allowed in positions off game board "
      + "(row doesn't exist #2).")
  void testIsValidMoveFalseRowOffBoard2() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, -1, 0);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }

  /**
   * Test that it is not possible to make moves to positions off the gameboard
   * (column out of range).
   */
  @Test
  @DisplayName("Moves are not allowed in positions off game board "
      + "(column doesn't exist #1).")
  void testIsValidMoveFalseColumnOffBoard1() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 1, 5);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }

  /**
   * Test that it is not possible to make moves to positions off the gameboard
   * (column out of range, negative value).
   */
  @Test
  @DisplayName("Moves are not allowed in positions off game board "
      + "(column doesn't exist #1).")
  void testIsValidMoveFalseColumnOffBoard2() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 1, -3);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }

  /**
   * Test that the played move is reflected in the gameboard configuration.
   */
  @Test
  @DisplayName("Played move should reflect in gameboard.")
  void testPlayMove() {

    char[][] startingBoardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(startingBoardState);

    Move move = new Move(player1, 0, 1);
    activeTestBoard.playMove(move);
    char[][] expectedBoardState = { { 0, 'X', 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };

    assertArrayEquals(expectedBoardState, activeTestBoard.getBoardState());
  }

  /**
   * Test that a partially full board is not a winning configuration.
   */
  @Test
  @DisplayName("This partially-full board is not a winning configuration.")
  void testNotWinneEmptyBoard() {
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 'X' }, { 0, 0, 0 } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(false, emptyTestBoard.isWinningMove(1, 2, 'X'));
  }

  /**
   * Test that the current board configuration is not a winning configuration
   * (horizontal not complete).
   */
  @Test
  @DisplayName("Not a winner #1; Winning board expects the same character in " + ""
      + "a single row, column or horizontal.")
  void testNotWinner1() {
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 'X', 0, 0 } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(false, emptyTestBoard.isWinningMove(2, 0, 'X'));
  }

  /**
   * Test that the current board configuration is not a winning configuration
   * (row not complete).
   */
  @Test
  @DisplayName("Not a winner #2; Winning board expects the same character in " + ""
      + "a single row, column or horizontal.")
  void testNotWinner2() {
    char[][] boardState = { { 0, 'X', 'X' }, { 0, 'O', 'O' }, { 0, 0, 0 } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(false, emptyTestBoard.isWinningMove(1, 2, 'O'));
  }

  /**
   * Test that the current board configuration is a winning configuration
   * via the left horizontal.
   */
  @Test
  @DisplayName("Winner; Left Horizontal Configuration.")
  void testWinnerLeftHorizontal() {

    // test game board data; game hasn't started
    char[][] boardState = { { 'X', 0, 'O' }, { 'O', 'X', 0 }, { 0, 0, 'X' } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(true, emptyTestBoard.isWinningMove(2, 2, 'X'));
  }

  /**
   * Test that the current board configuration is a winning configuration
   * via the right horizontal.
   */
  @Test
  @DisplayName("Winner; Right Horizontal Configuration.")
  void testWinnerRightHorizontal() {

    // test game board data; game hasn't started
    char[][] boardState = { { 0, 0, 'O' }, { 'X', 'O', 0 }, { 'O', 0, 'X' } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(true, emptyTestBoard.isWinningMove(1, 1, 'O'));
  }

  /**
   * Test that the current board configuration is a winning configuration
   * via the first column.
   */
  @Test
  @DisplayName("Winner; Vertical Configuration.")
  void testWinnerRow() {

    // test game board data; game hasn't started
    char[][] boardState = { { 'X', 0, 'O' }, { 'X', 0, 0 }, { 'X', 0, 0 } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(true, emptyTestBoard.isWinningMove(2, 0, 'X'));
  }

  /**
   * Test that the current board configuration is a winning configuration
   * via the top row.
   */
  @Test
  @DisplayName("Winner; Horizontal Configuration.")
  void testWinnerColumn() {

    // test game board data; game hasn't started
    char[][] boardState = { { 'X', 'X', 'X' }, { 'O', 'O', 0 }, { 0, 0, 'O' } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(true, emptyTestBoard.isWinningMove(0, 2, 'X'));
  }

  /**
   * Test that without any moves played, the gameboard is considered empty.
   */
  @Test
  @DisplayName("No moves have been played; the game board should be empty.")
  void testBoardEmpty() {

    GameBoard testBoard = new GameBoard();

    assertEquals(true, testBoard.isEmpty());
  }

  /**
   * Test that after any moves played, the gameboard is not considered empty.
   */
  @Test
  @DisplayName("Moves have been played; the game board should not be empty.")
  void testBoardNotEmpty() {

    GameBoard testBoard = new GameBoard();

    char[][] boardState = { { 0, 'O', 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    testBoard.setBoardState(boardState);

    assertEquals(false, testBoard.isEmpty());
  }

  /**
   * Test that after several moves that do not fully fill the board, 
   * the gameboard is not considered full.
   */
  @Test
  @DisplayName("Game board is partially filled so cannot be full.")
  void testNotFullBoard() {

    GameBoard testBoard = new GameBoard();

    // test game board data; game has started
    char[][] boardState = { { 0, 'X', 0 }, { 0, 0, 'O' }, { 0, 0, 0 } };
    testBoard.setBoardState(boardState);

    assertEquals(false, testBoard.isFull());
  }

  /**
   * Test that after all positions on the gameboard are occuppied, 
   * the board is considered full.
   */
  @Test
  @DisplayName("Game board is filled; cannot add more moves.")
  void testFullBoard() {

    GameBoard testBoard = new GameBoard();

    // test game board data; cats game
    char[][] boardState = { { 'X', 'O', 'X' }, { 'O', 'O', 'X' }, { 'O', 'X', 'X' } };

    testBoard.setBoardState(boardState);

    assertEquals(true, testBoard.isFull());
  }

  /**
   * Test that if the gameboard has no players, and the Move is not assigned to a
   * player, the move cannot be made due to a missing player violation.
   * @throws GameBoardInternalError this 
   */
  @Test()
  @DisplayName("Gameboard has no players, game has not started and player "
      + "should not be able to make move.")
  void preventPlayingMoveIfMissingPlayers() throws GameBoardInternalError {

    Move move = new Move(null, 0, 0);

    Message msg = emptyTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.MISSING_PLAYER.getValue(), msg.getCode());
  }

  /**
   * Test that if the gameboard has only 1 player, an attempt to make a move
   * by that player faults because of a missing player violation.
   */
  @Test()
  @DisplayName("Gameboard has only 1 player, game has not started and player " + ""
      + "should not be able to make move.")
  void preventPlayingMoveIfMissingPlayer() throws GameBoardInternalError {

    // configure game board for test
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    emptyTestBoard.setP1(player1);
    emptyTestBoard.setBoardState(boardState);

    Move move = new Move(player1, 0, 0);
    Message msg = emptyTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.MISSING_PLAYER.getValue(), msg.getCode());
  }

  /**
   * Test that if the gameboard has both players, player 2 cannot 
   * make the first move.
   */
  @Test()
  @DisplayName("Player two player should never be the one to make the first move.")
  void testPlayerOneAlwaysPlaysFirst() throws GameBoardInternalError {
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    // configure game board for test
    Move move = new Move(player2, 0, 0);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.INVALID_ORDER_OF_PLAY.getValue(), msg.getCode());
  }

  /**
   * Test that if the gameboard has both players, player 1 can make the first move.
   */
  @Test()
  @DisplayName("First player is allowed to make the first move.")
  void testPlayerOneAlwaysPlaysFirstOK() throws GameBoardInternalError {
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    // configure game board for test
    Move move = new Move(player1, 0, 0);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.SUCCESS.getValue(), msg.getCode());
  }

  /**
   * Test that a player cannot make a move if it is not their turn.
   */
  @Test()
  @DisplayName("Player should not be able to make a move if it is not their turn.")
  void playerCannotMakeMoveIfNotTheirTurn() throws GameBoardInternalError {

    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 'X', 0, 0 } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player1, 0, 0);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.OTHER_PLAYERS_TURN.getValue(), msg.getCode());
  }

  /**
   * Test that a player cannot make a move at an occupied position.
   */
  @Test()
  @DisplayName("Player should not be able to make a move the position requested " 
      + "" + "is already occupied.")
  void playerCannotMakeMoveToOccupiedPosition() throws GameBoardInternalError {

    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 'X', 0, 0 } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player2, 0, 1); // this position is already occupied
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.POSITION_NOT_ALLOWED.getValue(), msg.getCode());
  }

  /**
   * Test that a player cannot make a move at a position that doesn't exist.
   */
  @Test()
  @DisplayName("Player should not be able to make a move the position on the board " + ""
      + "that doesn't exist.")
  void playerCannotMakeMoveToNonexistentPosition() throws GameBoardInternalError {

    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 'X', 0, 0 } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player2, 4, 5); // this position doesn't exist
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.POSITION_NOT_ALLOWED.getValue(), msg.getCode());
  }

  /**
   * Test that a player cannot make a move if the gameboard is already won (game over).
   */
  @Test()
  @DisplayName("Player should not be able to continue playing if other "
      + "player already won.")
  void playerCannotMakeMoveIfGameOver() throws GameBoardInternalError {

    // configure game board for test
    char[][] boardState = { { 'O', 'X', 0 }, { 'O', 'X', 0 }, { 'O', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setWinner(1);

    Move move = new Move(player1, 0, 2);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.GAME_ALREADY_OVER.getValue(), msg.getCode());
  }

  /**
   * Test that a player is alerted if they make a game-winning move.
   */
  @Test()
  @DisplayName("Player made winning move; game should report that they have won.")
  void playerMakesWinningMove() throws GameBoardInternalError {

    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 'O', 'X', 0 }, { 'O', 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    Move move = new Move(player1, 2, 1);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.GAME_OVER_WINNER.getValue(), msg.getCode());
  }

  /**
   * Test that a player is alerted if they make the last available move on the
   * board and there are no winners (i.e., game is a draw).
   */
  @Test()
  @DisplayName("Player made the last available move on the board, but no one won.")
  void playerMakesMoveForDraw() throws GameBoardInternalError {

    // configure game board for test
    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player2, 2, 1); // this position is already occupied
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.GAME_OVER_NO_WINNER.getValue(), msg.getCode());
  }

  /**
   * Test that turns switch between players if no one has won and 
   * there is no draw yet (player 2 should go next).
   */
  @Test()
  @DisplayName("Player made move; no one has won and no draw yet. "
      + "Player 2 should go next.")
  void turnSwitchesToOtherPlayerIfNoWinner1() throws GameBoardInternalError {
    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    Move move = new Move(player1, 0, 2);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.SUCCESS.getValue(), msg.getCode());
    assertEquals(2, activeTestBoard.getTurn());
  }

  /**
   * Test that turns switch between players if no one has won and there is no draw yet
   * (player 1 should go next).
   */
  @Test()
  @DisplayName("Player made move; no one has won and no draw yet. "
      + "Player 1 should go next.")
  void turnSwitchesToOtherPlayerIfNoWinner2() throws GameBoardInternalError {
    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player2, 0, 2);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.SUCCESS.getValue(), msg.getCode());
    assertEquals(1, activeTestBoard.getTurn());
  }

  /**
   * Test that it is not possible for player 2 to make the first move.
   */
  @Test()
  @DisplayName("Player 2 should not be able to make the first move")
  void playerTwoAttemptsFirstMove() throws GameBoardInternalError {
    // configure game board for test
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    Move move = new Move(player2, 0, 2);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.INVALID_ORDER_OF_PLAY.getValue(), msg.getCode());
  }

  /**
   * Test that if Player 1 has aleady selected type X, Player 2 is autoassigned
   * type O.
   */
  @Test()
  @DisplayName("If Player 1 is already X, Player 2 should be auto-assigned O.")
  void testAutoSetPlayerP2O() throws GameBoardInternalError {

    this.emptyTestBoard.setP1(player1);

    emptyTestBoard.autoSetP2();

    assertEquals('O', emptyTestBoard.getP2().getType());

  }

  /**
   * Test that if Player 1 has aleady selected type O, Player 2 is autoassigned
   * type X.
   */
  @Test()
  @DisplayName("If Player 1 is already O, Player 2 should be auto-assigned X.")
  void testAutoSetPlayerP2X() throws GameBoardInternalError {

    this.emptyTestBoard.setP1(player2); // testing auto-assignment of move type

    emptyTestBoard.autoSetP2();

    assertEquals('X', emptyTestBoard.getP2().getType());

  }

  /**
   * Test that it is not possible to set a Player 2 with the same type
   * as Player 1 that is already on the board (X).
   */
  @Test()
  @DisplayName("If Player 1 is already O, Player 2 cannot be O.")
  void testSetPlayerTwoIllegal() {

    this.emptyTestBoard.setP1(player1); // testing auto-assignment of move type

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setP2(new Player('X', 1));
    });
  }

  /**
   * Test that it is not possible to set a Player 2 with the same type
   * as Player 1 that is already on the board (O).
   */
  @Test()
  @DisplayName("If Player 1 is already O, Player 2 can be O.")
  void testSetPlayerTwoValid() {

    this.emptyTestBoard.setP1(player1); // testing auto-assignment of move type
    emptyTestBoard.setP2(new Player('O', 1));

    assert true;  // we are only checking that no exception is thrown
  }

  /**
   * Test print out board.
   */
  @Test()
  @DisplayName("Printing the board shouldn't cause any errors.")
  void testPrintBoarad() {

    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);
    this.activeTestBoard.printBoard();

    assert true; // we are only checking that no exception is thrown
  }

  /**
   * Test that toString() method is a correct reflection of board state.
   */
  @Test()
  @DisplayName("To string method should be a correct reflection of the board.")
  void testGameBoardToString() {

    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);

    String expectedGameBoard = "GameBoard [p1=Player [type=X, id=1], "
        + "p2=Player [type=O, id=2], gameStarted=true, turn=1, "
        + "boardState=[[O, X, O], [X, O, X], [X,  , X]], winner=0, isDraw=false]";

    assertEquals(expectedGameBoard, activeTestBoard.toString());
  }

  /**
   * Test that it is not possible to set the winner of the game to players other
   * than Player 1 or Player 2.
   */
  @Test()
  @DisplayName("The only possible winners of the gameboard are players 1 or 2.")
  void testSetGameWinnerInvalidTooBig() {

    // you cannot assign player 7 as the winner
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setWinner(7);
    });
  }

  /**
   * Test that it is not possible to set the winner of the game to players other
   * than Player 1 or Player 2 (negative value passed).
   */
  @Test()
  @DisplayName("The only possible winners of the gameboard are players 1 or 2.")
  void testSetGameWinnerInvalidNegPlayer() {

    // you cannot assign player -1 as the winner
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setWinner(-1);
    });
  }

  /**
   * Test that it is possible to set the winner of the game to 
   * Player 1 or Player 2.
   */
  @Test()
  @DisplayName("Setting the player winner as player 1 or 2 is OK.")
  void testSetGameWinnerValid() {

    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);

    // you cannot assign player 7 as the winner
    activeTestBoard.setWinner(1);

    assert true; // we are only checking that no exception is thrown
  }

  /**
   * Test that a draw is not possible to set until the gameboard 
   * is actually full.
   */
  @Test()
  @DisplayName("A draw is not possible until the entire game board is full.")
  void testSetDrawInvalidNotFull() {

    char[][] boardState = { { 0, 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);

    // you cannot set the draw until the the board is full
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setDraw(true);
    });
  }

  /**
   * Test that it is not possible to set a draw if there is
   *  already a winner.
   */
  @Test()
  @DisplayName("A draw is not possible if there is a winner.")
  void testSetDrawInvalidWinner() {

    char[][] boardState = { { 'X', 'X', 'O' }, { 'X', 'O', 'O' }, { 'X', 0, 'O' } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setWinner(1);

    // you cannot set the draw if there is already a winner on the board
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setDraw(true);
    });
  }

  /**
   * Test that a draw is possible if the gameboard is full.
   */
  @Test()
  @DisplayName("A draw is possible when the entire game board is full.")
  void testSetDrawValid() {

    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 'O', 'X' } };
    activeTestBoard.setBoardState(boardState);

    activeTestBoard.setDraw(true);
    assert true; // we are only checking that no exception is thrown
  }

  /**
   * Test that a draw is not possible to set until the gameboard is actually full.
   */
  @Test()
  @DisplayName("A partially full board cannot be considered a draw.")
  void testGetIsDraw() {
    assertEquals(activeTestBoard.isDraw(), false);
  }

  /**
   * Test that it is not possible to set a gameboard that is not 3x3 (wrong # rows).
   */
  @Test()
  @DisplayName("It is invalid to set the gameboard to have the "
      + "wrong number of rows.")
  void testSetGameBoardInvalidRowSize() {
    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' } };

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setBoardState(boardState);
    });
  }

  /**
   * Test that it is not possible to set a gameboard that is not 3x3 (wrong # columns).
   */
  @Test()
  @DisplayName("It is invalid to set the gameboard to have the wrong "
      + "number of columns.")
  void testSetGameBoardInvalidColumnSize() {
    char[][] boardState = { { 'O', 'X' }, { 'X', 'O' }, { 'X', 'O' } };

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setBoardState(boardState);
    });
  }

  /**
   * Test that it is not possible to set a gameboard that has invalid pieces (i.e., not
   * just X and Os).
   */
  @Test()
  @DisplayName("It is invalid to set the gameboard to invalid pieces "
      + "(i.e., not X or O).")
  void testSetGameBoardInvalidTypes() {
    char[][] boardState = { { 'O', 'P', 'O' }, { 'X', 'O', 'K' }, { 'X', 'O', 'X' } };

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setBoardState(boardState);
    });
  }

  /**
   * Test that it is not possible to set the next turn to a player that is not 
   * player 1 or player 2 (negative player test).
   */
  @Test()
  @DisplayName("It is invalid to set the turn to a player that is "
      + "not 1 or 2 (-1 definitely not!)")
  void testSetTurnNegPlayer() {

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setTurn(-1);
    });
  }

  /**
   * Test that it is not possible to set the next turn to a player that is not 
   * player 1 or player 2 (player 7 doesn't exist).
   */
  @Test()
  @DisplayName("It is invalid to set the turn to a player that is not 1 or 2 (7 definitely not!)")
  void testSetTurnInvalidPlayer() {

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setTurn(7);
    });
  }

  /**
   * Test that it is not possible to set the next turn to a player that does
   * not exist on the gameboard yet (Player 1).
   */
  @Test()
  @DisplayName("Cannot set the turn to player 1 if they don't exist!")
  void testSetTurnMissingPlayer1() {

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setTurn(1);
    });
  }

  /**
   * Test that it is not possible to set the next turn to a player that does
   * not exist on the gameboard yet (Player 2).
   */
  @Test()
  @DisplayName("Cannot set the turn to player 2 if they don't exist!")
  void testSetTurnMissingPlayer2() {

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setTurn(2);
    });
  }
  
  /**
   * Test that it is not possible to set the game as started if there are no
   * players on the board.
   */
  @Test()
  @DisplayName("Cannot set game as started if there are no players on the board")
  void testSetGameStartedMissingPlayers() {

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setGameStarted(true);
    });
  }

  /**
   * Test that it is not possible to set the game as started if Player 2 is
   * missing.
   */
  @Test()
  @DisplayName("Cannot set game as started if Player 2 is missing")
  void testSetGameStartedMissingPlayer1() {
    emptyTestBoard.setP1(player1);

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setGameStarted(true);
    });
  }

  /**
   * Test that it is possible to set the game as started if both
   * players are present.
   */
  @Test()
  @DisplayName("It is OK to start the game when both players are present")
  void testSetGameStartedValid() {

    activeTestBoard.setGameStarted(true);
    assert true; // we are only checking that no exception is thrown
  }
  
  /**
   * Test that it is possible to set the game as started if both
   * players are present.
   */
  @Test()
  @DisplayName("Resetting the board game should remove all moves and all players")
  void testResetGameboard() throws GameBoardInternalError {

    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);
    
    activeTestBoard.resetGameboard();
    GameBoard gb = new GameBoard();
    
    assertEquals(gb.toString(), activeTestBoard.toString());
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue reseting gb.
   */
  @Test()
  @DisplayName("GameBoardInternalError should be thrown if there was an issue "
      + "resetting the db (#1)")
  void testResetGameboardError() throws GameBoardInternalError, DbServiceException {

    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).createNewGame(1);
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      activeTestBoard.resetGameboard();
    });
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue reseting gb, double error.
   */
  @Test()
  @DisplayName("GameBoardInternalError should be thrown if there was an issue "
      + "resetting the db (#2)")
  void testResetGameboardErrorDouble() throws GameBoardInternalError, DbServiceException {

    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).createNewGame(1);
    
    doThrow(new DbServiceException("Exception thrown"))
    .when(dbService).close();
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      activeTestBoard.resetGameboard();
    });
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue committing move.ion 
   */
  @Test()
  @DisplayName("If there was an issue saving the move, it should result in "
      + "a GameBoardInternalError (#1)")
  void testCommitMoveError() throws GameBoardInternalError, DbServiceException {
    
    // mock the db service throwing an error
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).commit();
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      activeTestBoard.commitMove();
    });
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue committing move.ion 
   */
  @Test()
  @DisplayName("If there was an issue saving the move, it should result in "
      + "a GameBoardInternalError (#2)")
  void testCommitMoveErrorDouble() throws GameBoardInternalError, DbServiceException {
    
    // mock the db service throwing an error
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).commit();
    
    doThrow(new DbServiceException("Exception thrown"))
    .when(dbService).close();
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      activeTestBoard.commitMove();
    });
  }
  
  /**
   * Test commitMove with mock db should be successful.
   */
  @Test()
  @DisplayName("There should be no issues saving the move if the db is mocked")
  void testcommitMoveSuccess() throws GameBoardInternalError, DbServiceException {
    
    activeTestBoard.commitMove();
    
    // we just want to see that nothing fails
    assert true; 
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue committing move.
   */
  @Test()
  @DisplayName("Player 1 should save successfully")
  void testSaveP1() throws GameBoardInternalError {
    
    emptyTestBoard.saveP1(player1);
    
    assertEquals(player1, emptyTestBoard.getP1());
    assertEquals(1, emptyTestBoard.getTurn());
    
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue committing player to db, single exception.
   */
  @Test()
  @DisplayName("An error saving player one to db should yield a GameboardInternalError")
  void testSaveP1Error() throws GameBoardInternalError, DbServiceException {
    
    emptyTestBoard.saveP1(player1);
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).savePlayer(emptyTestBoard.getP1(), 1);
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      emptyTestBoard.saveP1(player1);
    });
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue committing player to db, double exception.
   */
  @Test()
  @DisplayName("An error saving player one to db should yield a GameboardInternalError")
  void testSaveP1ErrorDouble() throws GameBoardInternalError, DbServiceException {
    
    emptyTestBoard.saveP1(player1);
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).savePlayer(emptyTestBoard.getP1(), 1);
    
    doThrow(new DbServiceException("Exception thrown"))
    .when(dbService).close();
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      emptyTestBoard.saveP1(player1);
    });
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue committing player to db, single exception.
   */
  @Test()
  @DisplayName("An error saving player two to db should yield a GameboardInternalError")
  void testAutoSetP2Error() throws GameBoardInternalError, DbServiceException {
    
    emptyTestBoard.saveP1(player1);
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).saveGameState(emptyTestBoard, 1);
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      emptyTestBoard.autoSetP2();
    });
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue committing player to db, double exception.
   */
  @Test()
  @DisplayName("An error saving player two to db should yield a GameboardInternalError")
  void testAutoSetP2ErrorDouble() throws GameBoardInternalError, DbServiceException {
    
    emptyTestBoard.saveP1(player1);
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).saveGameState(emptyTestBoard, 1);
    
    doThrow(new DbServiceException("Exception thrown"))
    .when(dbService).close();
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      emptyTestBoard.autoSetP2();
    });
  }
  
  /**
   * Auto-setting P2 without P1 raises InvalidGameBoardConfigurationException.
   */
  @Test()
  @DisplayName("An error saving player two to db should yield a "
      + "InvalidGameBoardConfigurationException")
  void testAutoSetP2WithoutP1() {

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.autoSetP2();
    });
  }
  
  
  /**
   * Test GameBoardInternal Error sent when db issue saving move to db.
   */
  @Test()
  @DisplayName("An error saving move to db should yield a GameboardInternalError")
  void testAutoSetMoveError() throws GameBoardInternalError, DbServiceException {
    
    Move move = new Move(player1, 0, 2);
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).saveValidMove(move, 1);
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      activeTestBoard.processPlayerMove(move);
    });
  }
  
  /**
   * Test GameBoardInternal Error sent when db issue saving move to db, double exception.
   */
  @Test()
  @DisplayName("An error saving move to db should yield a GameboardInternalError")
  void testAutoSetMoveErrorDouble() throws GameBoardInternalError, DbServiceException {
    
    Move move = new Move(player1, 0, 2);
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).saveValidMove(move, 1);
    
    doThrow(new DbServiceException("Exception thrown"))
    .when(dbService).close();
    
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      activeTestBoard.processPlayerMove(move);
    });
  }
  
  /**
   * Test get the most recent state.
   */
  @Test()
  @DisplayName("Test get most recent state; should be succesful")
  void testGetMostRecentGameboard() throws GameBoardInternalError, DbServiceException {
   
    when(dbService.restoreMostRecentGameBoard()).thenReturn(new GameBoard());
    
    GameBoard result = activeTestBoard.getMostRecentDbState();
    GameBoard expected = new GameBoard();
    
    assertEquals(expected.toString(), result.toString());
  }
  
  /**
   * Test get the most recent state with error thrown by db.
   */
  @Test()
  @DisplayName("Test get most recent state; error thrown by db")
  void testGetMostRecentGbError() throws GameBoardInternalError, DbServiceException {
   
    when(dbService.restoreMostRecentGameBoard()).thenReturn(new GameBoard());
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).restoreMostRecentGameBoard();
   
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      activeTestBoard.getMostRecentDbState();
    });
  }
  
  /**
   * Test get the most recent state with error thrown by db, double error.
   */
  @Test()
  @DisplayName("Test get most recent state; error thrown by db")
  void testGetMostRecentGbErrorDouble() throws GameBoardInternalError, DbServiceException {
   
    when(dbService.restoreMostRecentGameBoard()).thenReturn(new GameBoard());
    
    // mock the db service throwing an error;
    doThrow(new DbServiceException("Exception thrown"))
      .when(dbService).restoreMostRecentGameBoard();
    
    doThrow(new DbServiceException("Exception thrown"))
    .when(dbService).close();
   
    Assertions.assertThrows(GameBoardInternalError.class, () -> {
      activeTestBoard.getMostRecentDbState();
    });
  }
  
  
  /**
   * Test GameBoardInternal Error sent when db issue saving move to db, double exception.
   */
  @Test()
  @DisplayName("There should be 3 columns and 3 rows on a TicTacToe gameboard")
  void testGameBoardSize() {
    
    assertEquals(3, GameBoard.getColumns());
    assertEquals(3, GameBoard.getRows());
  }
}
