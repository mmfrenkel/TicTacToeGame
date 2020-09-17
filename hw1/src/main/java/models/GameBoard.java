package models;

import com.google.gson.annotations.Expose;
import java.util.Arrays;
import java.util.List;

public class GameBoard {

  /* - @Expose tells GSON to add only the below fields to JSON - */
  
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
  
  /* -- end fields to serialize to JSON from object here -- */
  
  private final int columns = 3;
  
  private final int rows = 3;
  
  private final List<Character> acceptedTypes = Arrays.asList('X', 'O');
  
  /**
   *  Primary Constructor for GameBoard().
   */
  public GameBoard() {
    this.p1 = null;
    this.p2 = null;
    this.gameStarted = false; // game cannot start until there are two players
    this.turn = 0; // no ones turn yet
    this.boardState = new char[columns][rows]; // contents are 0 or '\u0000' by default
    this.winner = 0; // no one is a winner yet
    this.isDraw = false;
  }
  
  /**
   * Secondary Constructor helpful for easy testing.
   */
  public GameBoard(Player p1, Player p2, boolean gameStarted, int turn, char[][] state, 
      int winner, boolean isDraw) {
    this.p1 = p1;
    this.p2 = p2;
    this.gameStarted = gameStarted;
    this.turn = turn;
    this.boardState = state;
    this.winner = winner;
    this.isDraw = isDraw;
  }
  
  /**
   * Determines whether or not the game board is currently empty.
   * 
   * @return true if game board is empty (no moves made yet), else false
   */
  public boolean isEmpty() {
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        if (this.boardState[i][j] != 0) {
          return false;
        }
      }
    }
    return true;
  }
  
  /**
   * Determines whether or not the game board is currently full.
   * 
   * @return true if the game board is full, else false
   */
  public boolean isFull() {
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        if (this.boardState[i][j] == 0) {
          return false;
        }
      }
    }
    return true;
  }
  
  /**
   * Handles the move submitted by a user, checking several erroneous
   * circumstances, including lack of players, invalid ordering of moves, another
   * player's turn, occupied/invalid position and game already over. Moves are
   * played only if the move is determined to be valid under each of these rules.
   * The result of the move is returned within a Message object.
   * 
   * @param move instance of player Move
   * @return Message() object, reflecting outcome of Move
   */
  public Message processPlayerMove(Move move) {
    Message message;
    
    /* ---- Need to check several states to make sure move is valid ---- */
    if (!isGameStarted()) {
      // 1. If there aren't two players, game has not started and cannot make move
      message = new Message(false, MessageStatus.MISSING_PLAYER, 
          "Game cannot start until there are two players on the game board!");
      
    } else if (isEmpty() && move.getPlayerId() == 2) {
      // 2. First player should always be the one to make the first move
      message = new Message(false, MessageStatus.INVALID_ORDER_OF_PLAY, 
          "Player 1 makes the first move on an empty board!");

    } else if (move.getPlayerId() != getTurn()) {
      // 3. If it's not the player's turn, cannot make move
      message = new Message(false, MessageStatus.OTHER_PLAYERS_TURN, 
          "It is not currently your turn. Player " + getTurn() + " gets to make the next move.");
      
    } else if (!isValidMove(move)) {
      // 4. If the submitted move is not available, cannot make move
      message = new Message(false, MessageStatus.POSITION_NOT_ALLOWED, 
          "You cannot make a move at (" + move.getMoveX() + ", " + move.getMoveY() + "). "
              + "Please choose an unoccupied position on the game board!");
      
    } else if (getWinner() != 0) {
      // 5. If the board was already won, then cannot make another move
      message = new Message(false, MessageStatus.GAME_ALREADY_OVER, 
          "Game is already over! Player " + getWinner() + " won!");
    
    } else {
      // 6. Move is valid and should be played
      playMove(move);
      
      if (getWinner() != 0) {
        // 6a. If winning move, game over
        message = new Message(true, MessageStatus.GAME_OVER_WINNER, 
            "Player " + getWinner() + " is the winner!");
        
      } else if (isFull()) {
        // 6b. If not a winning move, but now the board is full, game is a draw and no
        // one can win
        setDraw(true);
        message = new Message(true, MessageStatus.GAME_OVER_NO_WINNER, "Game Over! Nobody wins.");
        
      } else {
        // 6c. No winners or draw yet
        message = new Message(true, MessageStatus.SUCCESS, "Player " + move.getPlayerId()
            + " made move at (" + move.getMoveX() + ", " + move.getMoveY() + ").");
        
        // swap turns for players
        setTurn(move.getPlayerId() == 1 ? 2 : 1);
      }
    }
    return message;
  }
  
  /**
   * Is the move provided a valid move (i.e., to a position that is currently
   * unoccupied and to a position that exists on the board)?
   * 
   * @param move Instance of Move object
   * @return true if the Move is valid, else false
   */
  public boolean isValidMove(Move move) {
    int x = move.getMoveX();
    int y = move.getMoveY();
    
    if (x >= rows || y >= columns || x < 0 || y < 0) {
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
   * state.
   * 
   * @param move Instance of Move object representing player and position to play
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
   * winning configuration on the game board.
   * 
   * @param type Character, either 'X' or 'O'
   * @return If the last move was a winning move
   */
  public boolean isWinningMove(int x, int y, char type) {
    return winningRow(x, type) || winningColumn(y, type) || winningHorizontal(type);
  }
  
  /**
   * Given a row index and a move type, determine if the move results in a winning
   * configuration in the row. For example, player X wins in this configuration
   * via second row completion:
   * X - O 
   * X X X 
   * O O -
   * 
   * @param row  integer, index of a row in game board
   * @param type char, a move type; either 'X' or 'O'
   * @return if it is a winning row
   */
  private boolean winningRow(int row, char type) {
    int column = 0;
    
    while (column < columns) {
      if (this.boardState[row][column] != type) {
        return false;
      }
      column++;
    }
    return true;
  }
  
  /**
   * Given a column index and a move type, determine if the move results in a
   * winning configuration in the column. For example, player X wins in this
   * configuration via first column completion:
   * X O - 
   * X - O 
   * X - -
   * 
   * @param column integer, index of a column in game board
   * @param type   char, a move type; either 'X' or 'O'
   * @return if it is a winning column
   */
  private boolean winningColumn(int column, char type) {
    int row = 0;
    
    while (row < rows) {
      if (this.boardState[row][column] != type) {
        return false;
      }
      row++;
    }
    return true;
  }
  
  /**
   * Given a row index and a move type, determine if the move results in a winning
   * configuration in the row. For example, player X wins in either of these
   * configurations:
   * X - O   - - X 
   * O X X   - X O 
   * O O X   X O O
   * 
   * @param row  integer, index of a row
   * @param type char, a move type; either 'X' or 'O'
   * @return if it is a winning row
   */
  private boolean winningHorizontal(char type) {
    int r = 0;
    int c = 0;
    
    // check left diagonal i.e., \
    while (c < columns && r < rows) {
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
    c = columns - 1;
    while (c >= 0 && r < rows) {
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
   * Method to auto-set player 2 as the player type that player 1 is not. For
   * example, if player 1 already exists and has chosen type 'X', then player 2
   * will be 'O'.
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
   * @param p2 Instance of Player object, representing the 2nd player to join
   * @throws InvalidGameBoardConfigurationException If player 2 tries to be the
   *                                                same 'type' as player 1
   */
  public void setP2(Player p2) throws InvalidGameBoardConfigurationException {
  
    // catch scenarios where player 2 wants to be 'X' but player 1 already is
    if (p2.getType() == getP1().getType()) {
      throw new InvalidGameBoardConfigurationException("Player 1 already has selected '" 
          + p2.getType() + "'. Cannot have two players with the same type");
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
  
  /**
   * Set the turn of the player via passing the ID of that player.
   * 
   * @param turn  Integer representing the ID of the player who has the next turn
   * @throws InvalidGameBoardConfigurationException if invalid player ID provided
   */
  public void setTurn(int turn) {
  
    if (turn < 1 || turn > 2) {
      throw new InvalidGameBoardConfigurationException("The only positions that "
          + "exist on this board are positions 1 and 2; got " + turn);
    }
    this.turn = turn;
  }
  
  public char[][] getBoardState() {
    return boardState;
  }
  
  /**
   * Sets the board state; must be 2D array of correct size and containing only
   * the correct possible piece types (null, X or O).
   * 
   * @param boardState 2D array containing current configuration of board
   * @throws InvalidGameBoardConfigurationException if board submitted is invalid
   */
  public void setBoardState(char[][] boardState) {
  
    if (boardState.length != rows || boardState[0].length != columns) {
      throw new InvalidGameBoardConfigurationException("Board must be " 
          + rows + "x" + columns + " in size.");
    }
    
    for (char[] row : boardState) {
      for (char move : row) {
        if (move != 0 && !acceptedTypes().contains(move)) {
          throw new InvalidGameBoardConfigurationException("Board submitted contained "
              + "unexpected pieces; only 'X' and 'O' expected.");
        }
      }
    }
    this.boardState = boardState;
  }
  
  public int getWinner() {
    return winner;
  }
  
  /**
   * Set a winner for the board by player ID.
   * 
   * @param winner integer representing player number, either 1 or 2.
   * @throws InvalidGameBoardConfigurationException if invalid player ID provided
   */
  public void setWinner(int winner) {
  
    if (winner < 1 || winner > 2) {
      throw new InvalidGameBoardConfigurationException("The only positions that "
          + "exist on this board are positions 1 and 2; got " + turn);
    }
    this.winner = winner;
  }
  
  public boolean isDraw() {
    return isDraw;
  }
  
  public void setDraw(boolean isDraw) {
    this.isDraw = isDraw;
  }
  
  public List<Character> acceptedTypes() {
    return this.acceptedTypes;
  }
  
  /**
   * Prints out the game board as a 3 x 3 square, visually similar to the board
   * shown in web UI. Helpful for logging and debugging.
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
  
  @Override
  public String toString() {
    return "GameBoard [p1=" + p1 + ", p2=" + p2 + ", gameStarted=" + gameStarted + ", "
        + "turn=" + turn + ", boardState=" + Arrays.toString(boardState) + ", winner=" 
        + winner + ", isDraw=" + isDraw + "]";
  }
}
