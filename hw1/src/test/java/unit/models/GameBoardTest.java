package unit.models;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import models.GameBoard;
import models.InvalidGameBoardConfigurationException;
import models.Message;
import models.MessageStatus;
import models.Move;
import models.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class GameBoardTest {

  private GameBoard emptyTestBoard;
  private GameBoard activeTestBoard;
  private char[][] emptyBoard = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
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

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 0, 2);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }

  @Test
  @DisplayName("Moves are allowed in unoccupied positions.")
  void testIsValidMoveTrue() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 0, 1);
    assertEquals(true, emptyTestBoard.isValidMove(attemptedMove));
  }
  
  @Test
  @DisplayName("Moves are not allowed in positions off game board (row doesn't exist #1).")
  void testIsValidMoveFalseRowOffBoard1() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 3, 0);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }
  
  @Test
  @DisplayName("Moves are not allowed in positions off game board (row doesn't exist #2).")
  void testIsValidMoveFalseRowOffBoard2() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, -1, 0);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }
  
  @Test
  @DisplayName("Moves are not allowed in positions off game board (column doesn't exist #1).")
  void testIsValidMoveFalseColumnOffBoard1() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 1, 5);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }
  
  @Test
  @DisplayName("Moves are not allowed in positions off game board (column doesn't exist #1).")
  void testIsValidMoveFalseColumnOffBoard2() {

    char[][] boardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(boardState);

    Move attemptedMove = new Move(player1, 1, -3);
    assertEquals(false, emptyTestBoard.isValidMove(attemptedMove));
  }

  @Test
  @DisplayName("Played move should reflect in gameboard.")
  void testPlayMove() {

    char[][] startingBoardState = { { 0, 0, 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };
    emptyTestBoard.setBoardState(startingBoardState);

    Move move = new Move(player1, 0, 1);
    emptyTestBoard.playMove(move);
    char[][] expectedBoardState = { { 0, 'X', 'O' }, { 0, 0, 'X' }, { 0, 0, 0 } };

    assertArrayEquals(expectedBoardState, emptyTestBoard.getBoardState());
  }

  @Test
  @DisplayName("An empty board is not a winning configuration.")
  void testNotWinneEmptyBoard() {
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 'X' }, { 0, 0, 0 } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(false, emptyTestBoard.isWinningMove(1, 2, 'X'));
  }

  @Test
  @DisplayName("Not a winner #1; Winning board expects the same character in "
      + "" + "a single row, column or horizontal.")
  void testNotWinner1() {
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 'X', 0, 0 } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(false, emptyTestBoard.isWinningMove(2, 0, 'X'));
  }
  
  @Test
  @DisplayName("Not a winner #2; Winning board expects the same character in "
      + "" + "a single row, column or horizontal.")
  void testNotWinner2() {
    char[][] boardState = { { 0, 'X', 'X' }, { 0, 'O', 'O' }, { 0, 0, 0 } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(false, emptyTestBoard.isWinningMove(1, 2, 'O'));
  }

  @Test
  @DisplayName("Winner; Left Horizontal Configuration.")
  void testWinnerLeftHorizontal() {

    // test game board data; game hasn't started
    char[][] boardState = { { 'X', 0, 'O' }, { 'O', 'X', 0 }, { 0, 0, 'X' } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(true, emptyTestBoard.isWinningMove(2, 2, 'X'));
  }

  @Test
  @DisplayName("Winner; Right Horizontal Configuration.")
  void testWinnerRightHorizontal() {

    // test game board data; game hasn't started
    char[][] boardState = { { 0, 0, 'O' }, { 'X', 'O', 0 }, { 'O', 0, 'X' } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(true, emptyTestBoard.isWinningMove(1, 1, 'O'));
  }

  @Test
  @DisplayName("Winner; Vertical Configuration.")
  void testWinnerRow() {

    // test game board data; game hasn't started
    char[][] boardState = { { 'X', 0, 'O' }, { 'X', 0, 0 }, { 'X', 0, 0 } };

    emptyTestBoard.setBoardState(boardState);

    assertEquals(true, emptyTestBoard.isWinningMove(2, 0, 'X'));
  }

  @Test
  @DisplayName("Winner; Horizontal Configuration.")
  void testWinnerColumn() {

    // test game board data; game hasn't started
    char[][] boardState = { { 'X', 'X', 'X' }, { 'O', 'O', 0 }, { 0, 0, 'O' } };

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

    char[][] boardState = { { 0, 'O', 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    testBoard.setBoardState(boardState);

    assertEquals(false, testBoard.isEmpty());
  }

  @Test
  @DisplayName("Game board is partially filled so cannot be full.")
  void testNotFullBoard() {

    GameBoard testBoard = new GameBoard();

    // test game board data; game has started
    char[][] boardState = { { 0, 'X', 0 }, { 0, 0, 'O' }, { 0, 0, 0 } };
    testBoard.setBoardState(boardState);

    assertEquals(false, testBoard.isFull());
  }

  @Test
  @DisplayName("Game board is filled; cannot add more moves.")
  void testFullBoard() {

    GameBoard testBoard = new GameBoard();

    // test game board data; cats game
    char[][] boardState = { { 'X', 'O', 'X' }, { 'O', 'O', 'X' }, { 'O', 'X', 'X' } };

    testBoard.setBoardState(boardState);

    assertEquals(true, testBoard.isFull());
  }

  @Test()
  @DisplayName("Gameboard has no players, game has not started and player"
      + " " + "should not be able to make move.")
  void preventPlayingMoveIfMissingPlayers() {

    Move move = new Move(null, 0, 0);

    Message msg = emptyTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.MISSING_PLAYER.getValue(), msg.getCode());
  }

  @Test()
  @DisplayName("Gameboard has only 1 player, game has not started and player "
      + "" + "should not be able to make move.")
  void preventPlayingMoveIfMissingPlayer() {

    // configure game board for test
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    emptyTestBoard.setP1(player1);
    emptyTestBoard.setBoardState(boardState);

    Move move = new Move(player1, 0, 0);
    Message msg = emptyTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.MISSING_PLAYER.getValue(), msg.getCode());
  }

  @Test()
  @DisplayName("Player two player should never be the one to make the first move.")
  void testPlayerOneAlwaysPlaysFirst() {
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    // configure game board for test
    Move move = new Move(player2, 0, 0);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.INVALID_ORDER_OF_PLAY.getValue(), msg.getCode());
  }
  
  @Test()
  @DisplayName("First player is allowed to make the first move.")
  void testPlayerOneAlwaysPlaysFirstOK() {
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    // configure game board for test
    Move move = new Move(player1, 0, 0);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.SUCCESS.getValue(), msg.getCode());
  }

  @Test()
  @DisplayName("Player should not be able to make a move if it is not their turn.")
  void playerCannotMakeMoveIfNotTheirTurn() {

    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 'X', 0, 0 } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player1, 0, 0);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.OTHER_PLAYERS_TURN.getValue(), msg.getCode());
  }

  @Test()
  @DisplayName("Player should not be able to make a move the position requested "
      + "" + "is already occupied.")
  void playerCannotMakeMoveToOccupiedPosition() {

    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 'X', 0, 0 } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player2, 0, 1); // this position is already occupied
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.POSITION_NOT_ALLOWED.getValue(), msg.getCode());
  }

  @Test()
  @DisplayName("Player should not be able to make a move the position on the board "
      + "" + "that doesn't exist.")
  void playerCannotMakeMoveToNonexistentPosition() {

    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 'X', 0, 0 } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player2, 4, 5); // this position doesn't exist
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.POSITION_NOT_ALLOWED.getValue(), msg.getCode());
  }

  @Test()
  @DisplayName("Player should not be able to continue playing if other player already won.")
  void playerCannotMakeMoveIfGameOver() {

    // configure game board for test
    char[][] boardState = { { 'O', 'X', 0 }, { 'O', 'X', 0 }, { 'O', 0, 'X' } };
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
    char[][] boardState = { { 0, 'X', 0 }, { 'O', 'X', 0 }, { 'O', 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    Move move = new Move(player1, 2, 1);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.GAME_OVER_WINNER.getValue(), msg.getCode());
  }

  @Test()
  @DisplayName("Player made the last available move on the board, but no one won.")
  void playerMakesMoveForDraw() {

    // configure game board for test
    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player2, 2, 1); // this position is already occupied
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.GAME_OVER_NO_WINNER.getValue(), msg.getCode());
  }

  @Test()
  @DisplayName("Player made move; no one has won and no draw yet. Player 2 should go next.")
  void turnSwitchesToOtherPlayerIfNoWinner1() {
    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 'O', 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    Move move = new Move(player1, 0, 2);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.SUCCESS.getValue(), msg.getCode());
    assertEquals(activeTestBoard.getTurn(), 2);
  }
  
  @Test()
  @DisplayName("Player made move; no one has won and no draw yet. Player 1 should go next.")
  void turnSwitchesToOtherPlayerIfNoWinner2() {
    // configure game board for test
    char[][] boardState = { { 0, 'X', 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);
    activeTestBoard.setTurn(2);

    Move move = new Move(player2, 0, 2);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.SUCCESS.getValue(), msg.getCode());
    assertEquals(activeTestBoard.getTurn(), 1);
  }

  @Test()
  @DisplayName("Process move where player 2 attempts to join game before game has started.")
  void playerTwoAttemptsFirstMove() {
    // configure game board for test
    char[][] boardState = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    activeTestBoard.setBoardState(boardState);

    Move move = new Move(player2, 0, 2);
    Message msg = activeTestBoard.processPlayerMove(move);

    assertEquals(MessageStatus.INVALID_ORDER_OF_PLAY.getValue(), msg.getCode());
  }

  @Test()
  @DisplayName("If Player 1 is already X, Player 2 should be auto-assigned O.")
  void testAutoSetPlayerP2O() {

    this.emptyTestBoard.setP1(player1);

    emptyTestBoard.autoSetP2();

    assertEquals(emptyTestBoard.getP2().getType(), 'O');

  }

  @Test()
  @DisplayName("If Player 1 is already O, Player 2 should be auto-assigned X.")
  void testAutoSetPlayerP2X() {

    this.emptyTestBoard.setP1(player2); // testing auto-assignment of move type

    emptyTestBoard.autoSetP2();

    assertEquals(emptyTestBoard.getP2().getType(), 'X');

  }

  @Test()
  @DisplayName("If Player 1 is already O, Player 2 cannot be O.")
  void testSetPlayerTwoIllegal() {

    this.emptyTestBoard.setP1(player1); // testing auto-assignment of move type

    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setP2(new Player('X', 1));
    });
  }

  @Test()
  @DisplayName("If Player 1 is already O, Player 2 can be O.")
  void testSetPlayerTwoValid() {

    this.emptyTestBoard.setP1(player1); // testing auto-assignment of move type

    // we just want to know this DOESN'T throw any exceptions
    emptyTestBoard.setP2(new Player('O', 1));

    assert true;
  }

  @Test()
  @DisplayName("Printing the board shouldn't cause any errors.")
  void testPrintBoarad() {

    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);
    this.activeTestBoard.printBoard();

    assert true; // we are only checking that no exception is thrown
  }

  @Test()
  @DisplayName("To string method should be a correct reflection of the board.")
  void testGameBoardToString() {

    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);

    String expectedGameBoard = "GameBoard [p1=Player [type=X, id=1], "
        + "p2=Player [type=O, id=2], gameStarted=true, turn=1, "
        + "boardState=[[O, X, O], [X, O, X], [X,  , X]], winner=0, isDraw=false]";

    assertEquals(activeTestBoard.toString(), expectedGameBoard);
  }
  
  @Test()
  @DisplayName("The only possible winners of the gameboard are players 1 or 2.")
  void testSetGameWinnerInvalidTooBig() {
    
    // you cannot assign player 7 as the winner
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setWinner(7);
    });
  }
  
  @Test()
  @DisplayName("The only possible winners of the gameboard are players 1 or 2.")
  void testSetGameWinnerInvalidNegPlayer() {
    
    // you cannot assign player -1 as the winner
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setWinner(-1);
    });
  }
  
  @Test()
  @DisplayName("Setting the player winner as player 1 or 2 is OK.")
  void testSetGameWinnerValid() {
    
    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 0, 'X' } };
    activeTestBoard.setBoardState(boardState);
    
    // you cannot assign player 7 as the winner
    activeTestBoard.setWinner(1);

    assert true; // we are only checking that no exception is thrown
  }
  
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
  
  @Test()
  @DisplayName("A draw is possible when the entire game board is full.")
  void testSetDrawValid() {
    
    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' }, { 'X', 'O', 'X' } };
    activeTestBoard.setBoardState(boardState);
    
    activeTestBoard.setDraw(true);
    assert true; // we are only checking that no exception is thrown
  }
  
  @Test()
  @DisplayName("A draw is possible when the entire game board is full.")
  void testGetIsDraw() {
    assertEquals(activeTestBoard.isDraw(), false);
  }
  
  @Test()
  @DisplayName("It is invalid to set the gameboard to have the wrong number of rows.")
  void testSetGameBoardInvalidRowSize() {
    char[][] boardState = { { 'O', 'X', 'O' }, { 'X', 'O', 'X' } };
    
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setBoardState(boardState);
    });
  }
  
  @Test()
  @DisplayName("It is invalid to set the gameboard to have the wrong number of columns.")
  void testSetGameBoardInvalidColumnSize() {
    char[][] boardState = { { 'O', 'X' }, { 'X', 'O' }, { 'X', 'O' } };
    
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setBoardState(boardState);
    });
  }
  
  @Test()
  @DisplayName("It is invalid to set the gameboard to invalid pieces (i.e., not X or O).")
  void testSetGameBoardInvalidTypes() {
    char[][] boardState = { { 'O', 'P', 'O' }, { 'X', 'O', 'K' }, { 'X', 'O', 'X' } };
    
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setBoardState(boardState);
    });
  }
  
  @Test()
  @DisplayName("It is invalid to set the turn to a player that is not 1 or 2 (-1 definitely not!)")
  void testSetTurnNegPlayer() {
    
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setTurn(-1);
    });
  }
  
  @Test()
  @DisplayName("It is invalid to set the turn to a player that is not 1 or 2 (7 definitely not!)")
  void testSetTurnInvalidPlayer() {
    
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      activeTestBoard.setTurn(7);
    });
  }
  
  @Test()
  @DisplayName("Cannot set the turn to player 1 if they don't exist!")
  void testSetTurnMissingPlayer1() {
    
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setTurn(1);
    });
  }
  
  @Test()
  @DisplayName("Cannot set the turn to player 2 if they don't exist!")
  void testSetTurnMissingPlayer2() {
    
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setTurn(2);
    });
  }
  
  @Test()
  @DisplayName("Cannot set game as started if there are no players on the board")
  void testSetGameStartedMissingPlayers() {
    
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setGameStarted(true);
    });
  }
  
  @Test()
  @DisplayName("Cannot set game as started if Player 1 is missing")
  void testSetGameStartedMissingPlayer1() {
    emptyTestBoard.setP1(player1);
    
    Assertions.assertThrows(InvalidGameBoardConfigurationException.class, () -> {
      emptyTestBoard.setGameStarted(true);
    });
  }
  
  @Test()
  @DisplayName("It is OK to start the game when both players are present")
  void testSetGameStartedValid() {

    activeTestBoard.setGameStarted(true);
    assert true; // we are only checking that no exception is thrown
  }
}
