package models;

import com.google.gson.annotations.Expose;
import java.util.Arrays;
import java.util.List;
import util.DbServiceException;
import util.TicTacToeDbService;
import util.TicTacToeSqliteDbService;

public class GameBoard implements GenericGameBoard {

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

  // a standard tic-tac-toe board has 3 rows and 3 columns  
  static final int columns = 3;

  static final int rows = 3;
  
  private TicTacToeDbService dbService;
  
  // currently there is only game "1" until game supports more than 1 game at a time
  // hence, the "current" game will always be the game with gameId = 1.
  private int gameId = 1; 
  
  // the accepted player types for this board
  private final List<Character> acceptedTypes = Arrays.asList('X', 'O');
  
  /**
   * Primary Constructor for GameBoard(), which will create an empty game board
   * (i.e., no players, game not started, no one's turn, empty board state, and no
   * winner or draw.
   * 
   * @param dbService instance of TicTacToeDbService to use; this can be any
   *                  database service, but is TicTacToeDbService by default if
   *                  using the empty argument constructor.
   */
  public GameBoard(TicTacToeDbService dbService) {
    this.p1 = null;
    this.p2 = null;
    this.gameStarted = false; // game cannot start until there are two players
    this.turn = 0;            // no ones turn yet
    this.boardState = new char[columns][rows];  // contents are 0 or '\u0000' by default
    this.winner = 0;          // no one is a winner yet
    this.isDraw = false;
    this.dbService = dbService;
  }
  
  /**
   * Secondary Constructor where database is not specified and the
   * SQLliteDbService class is used by default.
   */
  public GameBoard() {
    this(new TicTacToeSqliteDbService());
  }

  /**
   * Secondary Constructor primarily for easy testing, where a user is able to
   * provide any configuration of the board that they'd like. As a warning, this
   * constructor does not check for invalid configurations and assumes a user
   * understands the tic-tac-toe board game rules.
   * 
   * @param p1          instance of Player object, representing player 1
   * @param p2          instance of Player object, representing player 2
   * @param gameStarted boolean, for if game has started
   * @param turn        integer, representing the ID of the player who has the
   *                    next turn (1 or 2)
   * @param state       two-dimensional array of characters representing game
   *                    board state
   * @param winner      integer representing player ID of winner; 0 if no winner
   * @param isDraw      boolean, for if the game is already a draw
   * @param dbService   instance of TicTacToeDbService to use; this can be any
   *                    database service, but is TicTacToeDbService by default if
   *                    using the empty argument constructor
   */
  public GameBoard(Player p1, Player p2, boolean gameStarted, int turn, char[][] state, int winner, 
      boolean isDraw, TicTacToeDbService dbService) {
    this.p1 = p1;
    this.p2 = p2;
    this.gameStarted = gameStarted;
    this.turn = turn;
    this.winner = winner;
    this.isDraw = isDraw;
    setBoardState(state);
    this.dbService = dbService;
  }

  /**
   * Reset game board to the original and deletes previous instance in database.
   * 
   * @throws GameBoardInternalError thrown when an issue occurs reseting the game
   *                                in the database
   */
  public void resetGameboard() throws GameBoardInternalError {
    
    // reset all instance variables
    this.p1 = null;
    this.p2 = null;
    this.gameStarted = false;
    this.turn = 0;
    this.boardState = new char[columns][rows];  
    this.winner = 0;        
    this.isDraw = false;
    
    try {
      // delete the old game content from the database
      // this has to happen in two steps because otherwise the database file has a
      // lock on the row for gameId = 1 and unfortunately, for this iteration of the
      // game, the gameId is always 1.
      dbService.connect();
      dbService.deleteGame(gameId, true);
  
      // create the new game in db
      dbService.createNewGame(gameId);
      dbService.commit();
    } catch (DbServiceException e) {
      e.printStackTrace();
    
      try {
        dbService.close();
      }  catch (DbServiceException e1) {
        e1.printStackTrace();
      }
      throw new GameBoardInternalError("Reset gameboard operation failed.");
    }
  }
  
  /**
   * Loads the most recent version of the game board from the database. If there
   * is no game board yet, then the database returns a new game board.
   * 
   * @throws GameBoardInternalError if an error with the database occurred
   */
  public GameBoard getMostRecentDbState() throws GameBoardInternalError {
    try {
      GameBoard gb = (GameBoard) dbService.restoreMostRecentGameBoard();
      return gb;

    } catch (DbServiceException dbse) {
      System.err.println(dbse.getClass().getName() + ": " + dbse.getMessage());
      throw new GameBoardInternalError("Error encountered getting gameboard's "
          + "most recent state.");
    }
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
   * @throws GameBoardInternalError if there is an issue saving a valid move to
   *                                the database
   */
  public Message processPlayerMove(Move move) throws GameBoardInternalError {
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
      
    } else if (getWinner() != 0) {
      // 3. If the board was already won, then cannot make another move
      message = new Message(false, MessageStatus.GAME_ALREADY_OVER, 
          "Game is already over! Player " + getWinner() + " won!");

    } else if (move.getPlayerId() != getTurn()) {
      // 4. If it's not the player's turn, cannot make move
      message = new Message(false, MessageStatus.OTHER_PLAYERS_TURN, 
          "It is not currently your turn. Player " + getTurn() + " gets to make the next move.");
      
    } else if (!isValidMove(move)) {
      // 5. If the submitted move is not available, cannot make move
      message = new Message(false, MessageStatus.POSITION_NOT_ALLOWED, 
          "You cannot make a move at (" + move.getMoveX() + ", " + move.getMoveY() + "). "
              + "Please choose an unoccupied and valid position on the game board!");
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
      }
      
      saveMove(move);  // this saves the move but doesn't officially commit it
    }
    return message;
  }
  
  /**
   * Should be called after `processPlayerMove` when the user is confident that
   * the move made should be permanent.
   * 
   * @throws GameBoardInternalError if any issue occurred committing the transaction.
   */
  public void commitMove() throws GameBoardInternalError {

    try {
      dbService.commit();
    } catch (DbServiceException e) {
      try {
        dbService.close();
      } catch (DbServiceException e1) {
        e1.printStackTrace();
      }
      
      throw new GameBoardInternalError("Player move could not be saved to the "
          + "database due to a database error.");
    }
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
      this.setWinner(move.getPlayer().getId());
    }
    // swap turns for players
    setTurn(move.getPlayerId() == 1 ? 2 : 1);
  }
  
  /**
   * Saves the player move to the database, but doesn't do the commit step.
   * 
   * @param move Instance of Move to save
   * @throws GameBoardInternalError if there was an issue saving the move to the
   *                                database
   */
  private void saveMove(Move move) throws GameBoardInternalError {
    try {
      dbService.connect();
      dbService.saveValidMove(move, gameId);
      dbService.saveGameState(this, gameId);

    } catch (DbServiceException e) {
      e.printStackTrace();

      try {
        dbService.close();
      } catch (DbServiceException e1) {
        e1.printStackTrace();
      }
      
      throw new GameBoardInternalError("Player move could not be saved to the "
          + "database due to a database error.");
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
  
  /**
   *  Returns Player currently set as player 1; null if 
   *  player does not exist yet.
   *  
   *  @return instance of Player representing Player 1
   */
  public Player getP1() {
    return p1;
  }
  
  /**
   * Sets Player 1 on the game board and saves Player 1 to the database.
   * 
   * @param p1 instance of Player object
   * @throws GameBoardInternalError if there was an issue saving player 1 to the
   *                                database
   */
  public void saveP1(Player p1) throws GameBoardInternalError {
    setP1(p1);
    this.setTurn(1);

    try {
      dbService.connect();
      dbService.savePlayer(getP1(), gameId);
      dbService.saveGameState(this, gameId);
      dbService.commit();

    } catch (DbServiceException e) {
      e.printStackTrace();

      try {
        dbService.close();
      } catch (DbServiceException e1) {
        e1.printStackTrace();
      }
      throw new GameBoardInternalError("Error was encountered trying to save " 
          + " player 1 to the database.");
    }
  }
  
  /**
   * Sets Player 1 on the game board.
   * 
   * @param p1 instance of Player object
   */
  public void setP1(Player p1) {
    this.p1 = p1;
  }
  
  /**
   *  Returns Player currently set as player 2; null if 
   *  player does not exist yet.
   *  
   *  @return instance of Player representing Player 2
   */
  public Player getP2() {
    return p2;
  }
  
  /**
   * Will auto-set player 2 as the player type that player 1 is not. For
   * example, if player 1 already exists and has chosen type 'X', then player 2
   * will be 'O'. Saves the change to the database.
   * 
   * @throws GameBoardInternalError if there was an error updating the database
   */
  public void autoSetP2() throws GameBoardInternalError {
    
    if (getP1() == null) {
      throw new InvalidGameBoardConfigurationException("Cannot autoset player 2 until "
          + "player 1 also exists.");
    }
    
    char playerType = getP1().getType() == 'X' ? 'O' : 'X';
    Player p2 = new Player(playerType, 2);
    setP2(p2);
    setGameStarted(true);
    
    try {
      // save information to database
      dbService.connect();
      dbService.savePlayer(getP2(), gameId);
      dbService.saveGameState(this, gameId);
      dbService.commit();

    } catch (DbServiceException e) {
      e.printStackTrace();

      try {
        dbService.close();
      } catch (DbServiceException e1) {
        e1.printStackTrace();
      }
      throw new GameBoardInternalError("Error was encountered trying to save "
          + " player 2 to the database.");
    }
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

  /**
   * Determines if game has started. A game is started only when there are two
   * valid players on the game board.
   * 
   * @return true if game has begun, else false
   */
  public boolean isGameStarted() {
    return gameStarted;
  }

  /**
   * Sets the game as started; in order to do so, there must be two players on the
   * game board.
   * 
   * @throws InvalidGameBoardConfigurationException if two players don't yet exist
   */
  public void setGameStarted(boolean gameStarted) {
    if (getP1() == null || getP2() == null) {
      throw new InvalidGameBoardConfigurationException("Cannot set game as 'started' "
          + "until there are two players on the gameboard.");
    }
    
    this.gameStarted = gameStarted;
  }
  
  /**
   * Returns the ID of the player who has the next turn.
   * 
   * @return integer, representing ID of player
   */
  public int getTurn() {
    return turn;
  }

  /**
   * Set the turn of the player via passing the ID of that player.
   * 
   * @param turn Integer representing the ID of the player who has the next turn
   * @throws InvalidGameBoardConfigurationException if invalid player ID provided,
   *                                                or if the player whose ID is
   *                                                passed doesn't exist yet
   */
  public void setTurn(int turn) {
  
    if (turn < 1 || turn > 2) {
      throw new InvalidGameBoardConfigurationException("The only positions that "
          + "exist on this board are positions 1 and 2; got " + turn);
    }
    
    if ((turn == 1 && getP1() == null) || (turn == 2 && getP2() == null)) {
      throw new InvalidGameBoardConfigurationException("Cannot set the turn for a player that"
          + "does not exist on the game board yet.");
    }
    
    this.turn = turn;
  }
  
  /**
   * Returns the state of the board, as a new copy.
   * 
   * @return A two-dimensional array of characters representing board state
   */
  public char[][] getBoardState() {
    char[][] boardCopy = new char[rows][columns];
    
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        boardCopy[i][j] = this.boardState[i][j];
      }
    }
    return boardCopy;
  }
  
  /**
   * Sets the board state; must be 2D array of correct size and containing only
   * the correct possible piece types (0 (null), X or O).
   * 
   * @param newBoardState 2D array containing current configuration of board
   * @throws InvalidGameBoardConfigurationException if board submitted is invalid
   */
  public void setBoardState(char[][] newBoardState) {
  
    if (newBoardState.length != rows || newBoardState[0].length != columns) {
      throw new InvalidGameBoardConfigurationException("Board must be " 
          + rows + "x" + columns + " in size.");
    }
    
    for (char[] row : newBoardState) {
      for (char move : row) {
        if (move != 0 && !acceptedTypes().contains(move)) {
          throw new InvalidGameBoardConfigurationException("Board submitted contained "
              + "unexpected pieces; only 'X' and 'O' expected.");
        }
      }
    }
    
    // make a copy of the submitted board state
    char[][] boardCopy = new char[rows][columns];
    
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        boardCopy[i][j] = newBoardState[i][j];
      }
    }
    this.boardState = boardCopy;
  }
  
  /**
   * Returns the ID of the winner of the game, or 0 if there is no winner yet.
   * 
   * @return ID of winning player
   */
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
  
  /**
   * Returns whether or not the game is a draw. If true, the game board
   * is full and there are no more moves to be made.
   */
  public boolean isDraw() {
    return isDraw;
  }
  
  /**
   * Set whether or not the game board is a draw (i.e., game board is full and
   * there is no winner).
   * 
   * @param isDraw boolean, whether or not game is a draw
   * @throw InvalidGameBoardConfigurationException if the game board does not meet
   *        the qualifications of a draw.
   */
  public void setDraw(boolean isDraw) {
    
    if (getWinner() != 0) {
      throw new InvalidGameBoardConfigurationException("There is a winner on this game board,"
          + "so this game is not a draw.");
    }
    if (!isFull()) {
      throw new InvalidGameBoardConfigurationException("Game board is not yet full, "
          + "so there cannot be a draw yet.");
    }
    
    this.isDraw = isDraw;
  }
  
  /**
   * Gets the accepted player 'types' for the game board.
   * 
   * @return List of characters representing value types.
   */
  public List<Character> acceptedTypes() {
    return this.acceptedTypes;
  }
  
  public static int getColumns() {
    return columns;
  }

  public static int getRows() {
    return rows;
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
        + "turn=" + turn + ", boardState=" + Arrays.deepToString(boardState) + ", winner=" 
        + winner + ", isDraw=" + isDraw + "]";
  }
}
