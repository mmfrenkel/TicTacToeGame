package util;

import controllers.TicTacToeController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import models.GameBoard;
import models.GenericGameBoard;
import models.Move;
import models.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;


public class TicTacToeSqliteDbService implements TicTacToeDbService {
  
  private Connection sqliteConn;
  
  static String defaultDatabase = "tictactoe.db";
  
  private static Logger logger = LoggerFactory.getLogger(TicTacToeController.class);
  
  /**
   * Default Constructor, no arguments.
   */
  public TicTacToeSqliteDbService() {
  }
  
  /**
   * Provides an alternative constructor to allow specification of where the
   * database file should be.
   * 
   * @param database Name of database. May be a full path to a *.db file
   */
  public TicTacToeSqliteDbService(String database) {
    defaultDatabase = database;
  }
 
  /**
   * Connect to an existing database; if the database doesn't already exist, then
   * it will be created. The database exists in the current directory.
   * 
   * @throws DbServiceException if could not connect to SQLite database
   */
  public void connect() throws DbServiceException {
    try {
      connect(defaultDatabase);

    } catch (Exception e) {
      logger.error(e.getClass().getName() + ": " + e.getMessage());
      throw new DbServiceException("Could not establish SQLite db connection.\n");
    }
  }
  
  /**
   * Connect to an existing database; if the database doesn't already exist, then
   * it will be created. The database exists in the current directory. This
   * connection function can be used to specify the location of the database. This
   * is most helpful for testing, where we want to work with a different database.
   * 
   * @param dbLocation a path to where the database should be created and will be
   *                   found, for example: "C://sqlite/db/test.db"
   * @throws DbServiceException if could not connect to SQLite database
   */
  public void connect(String dbLocation) throws DbServiceException {
    try {
      Class.forName("org.sqlite.JDBC");
      
      // setup database to enforce foreign keys
      SQLiteConfig config = new SQLiteConfig();  
      config.enforceForeignKeys(true);  
      sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dbLocation, 
          config.toProperties());

    } catch (Exception e) {
      logger.error(e.getClass().getName() + ": " + e.getMessage());
      throw new DbServiceException("Could not establish SQLite db connection.\n");
    }
  }

  /**
   * Creates database tables for TicTacToe game, if they do not already exit. The
   * database tables required include: 'moves', 'players' and 'gameState', which
   * contain the minimum amount of information to restore a GameBoard instance
   * from a database. Note that to use this method, a database connection must
   * already have been made. Note additionally, that in order for the transaction
   * to officially complete, the caller of the function must call the commit()
   * method.
   * 
   * @throws DbServiceException if an error occurs while creating the new database
   *                            tables (derived from a SQLException, most often)
   */
  public void createDatabasesTables() throws DbServiceException {

    try {
      connect();
      sqliteConn.setAutoCommit(false);

      createGamesTable();
      createPlayersTable();
      createMovesTable();

      commit();

    } catch (SQLException e) {
      logger.error(e.getClass().getName() + ": " + e.getMessage());
      throw new DbServiceException("Failed to successfully create database tables " 
          + "for TicTacToe game.\n");

    } catch (DbServiceException dbse) {
      logger.error(dbse.getClass().getName() + ": " + dbse.getMessage());
      throw new DbServiceException("Failed to successfully create database tables " 
          + "for TicTacToe game.\n");
      
    } finally {
      close();
    }
  }
  
  /**
   * Restores the most recent game from the game board.
   * 
   * @returns the restored game board from the database query
   * @throws DbServiceException if there was an error on SELECT statements to
   *                            obtain either the id of the most recent game or
   *                            components of the game board for the discovered id
   */
  public GameBoard restoreMostRecentGameBoard() throws DbServiceException {
    GameBoard restoredBoard = null;
    Statement statement = null;
    ResultSet rs = null;
    
    try {
      connect();
      sqliteConn.setAutoCommit(true);
      
      String sql = "SELECT MAX(id) AS max FROM games;";
      statement = sqliteConn.createStatement();
      rs = statement.executeQuery(sql);

      if (rs.next()) {
        int gameId = rs.getInt("max");
        if (gameId > 0) {
          restoredBoard = restoreGameBoard(gameId);
        }
      }
      
      if (restoredBoard == null) {
        // no game boards yet, so just set a new one
        restoredBoard = new GameBoard(); 
      }
      
    } catch (SQLException e) {
      logger.error(e.getClass().getName() + ": " + e.getMessage());
      throw new DbServiceException("Could not determine ID of most recent game.");

    } finally {
      try {
        if (statement != null) {
          statement.close();
        }
        if (rs != null) {
          rs.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DbServiceException("Error occurred while cleaning up SQL statement.");
      }
      close();
    }
    
    return restoredBoard;
  }
  
  /**
   * Restores the latest instance of the tic-tac-toe game board from the database
   * and returns it to calling method. By retrieving all the players and moves
   * from the database for a given game, you can replay all moves to figure out
   * the currently status of the game board. Note that the connection needs to be
   * opened before calling this method; it will not open the connection for you.
   * 
   * @return GameBoard instance restored from players, moves and game state in the
   *         database.
   * @throws DbServiceException if an error occurred restoring game board from
   *                            database information
   */
  public GameBoard restoreGameBoard(int gameId) throws DbServiceException {
    
    if (sqliteConn == null) {
      throw new DbServiceException("Please establish DB connection before "
          + "requesting DB action.");
    }

    // Start from a fresh game board
    GameBoard gb = new GameBoard();

    // Find and add players to the game board
    List<Player> players = findAllPlayers(gameId);
    
    // If there are no players yet, game board state is ready
    if (players.size() == 0) {
      return gb;
    }

    for (Player p : players) {
      
      if (p.getId() == 1) {
        gb.setP1(p);
        gb.setTurn(1);
        
      } else if (p.getId() == 2) {
        gb.setP2(p);
        gb.setGameStarted(true);
      }
    }

    // Find all moves for the game so far and reconstruct game board by replaying
    // them; playing the moves will 1. flip between turns and 2. set winner if 
    // there is one
    List<Move> moves = findAllMoves(gameId);
    for (Move move : moves) {
      gb.playMove(move);   
    }

    // review winning and loosing state (note that winner is set via playMove()
    if (gb.isFull() && gb.getWinner() == 0) {
      gb.setDraw(true);
    }

    return gb;
  }

  /**
   * Adds a new game to the database. Note that for the time being, only ONE game
   * is allowed a time in the database and games are dropped from the database on
   * completion and request for a new game. This means that the id of the game
   * will always be 1. Note that to use this method, a database connection must
   * already have been made. Note additionally, that in order for the transaction
   * to officially complete, the caller of the function must call the commit()
   * method.
   * 
   * @param gameId the id of the game in the database to create; this is explicit
   *               for now because we only want to ever have 1 game; this could
   *               change in the future
   * @throws DbServiceException if an error occurred creating a new game in the
   *                            database
   */
  public void createNewGame(int gameId) throws DbServiceException {

    if (sqliteConn == null) {
      throw new DbServiceException("Please establish DB connection before "
          + "requesting DB action.");
    }

    String sql = ""
        + "INSERT INTO games (id, has_started, winner_id, is_draw, turn) "
        + "VALUES (" + gameId + ", 0, 0, 0, 0);";
   
    logger.info(sql);
    update(sql, false);
  }
  
  /**
   * Updates the components of the game state in the database, including if the
   * game has officially started, who the winner is, if there is a draw, and who
   * has the next turn.
   * 
   * @param gameboard any implementation of the GenericGameBoard class (i.e.,
   *                  GameBoard for tic-tac-toe)
   * @param gameId    the game ID in the database to associated this game board
   *                  with
   */
  public void saveGameState(GenericGameBoard gameboard, int gameId) throws DbServiceException {
    
    String sqlStarted = ""
        + "UPDATE games\n"
        + "SET has_started = " + gameboard.isGameStarted() + " \n"
        + "WHERE id = " + gameId + ";";
    
    update(sqlStarted, false);
    
    String sqlWinner = ""
        + "UPDATE games\n"
        + "SET winner_id = " + gameboard.getWinner() + " \n"
        + "WHERE id = " + gameId + ";";
    
    update(sqlWinner, false);
    
    String sqlDraw = ""
        + "UPDATE games\n"
        + "SET is_draw = " + gameboard.isDraw() + " \n"
        + "WHERE id = " + gameId + ";";
    
    update(sqlDraw, false);
    
    String sqlTurn = ""
        + "UPDATE games\n"
        + "SET turn = " + gameboard.getTurn() + " \n"
        + "WHERE id = " + gameId + ";";

    update(sqlTurn, false);
    
  }

  /**
   * Saves a plater to the database for a given game. Note that to use this
   * method, a database connection must already have been made. Note additionally,
   * that in order for the transaction to officially complete, the caller of the
   * function must call the commit() method.
   * 
   * @param player a Player instance to write to the database
   * @param gameId the game ID to associated player with
   * @throws DbServiceException if an issue occurred executing the INSERT
   *                            statement
   */
  public void savePlayer(Player player, int gameId) throws DbServiceException {

    if (sqliteConn == null) {
      throw new DbServiceException("Please establish DB connection before "
          + "requesting DB action.");
    }
    
    String sql = ""
        + "INSERT INTO players (id, player_type, game_id) "
        + "VALUES (" + player.getId() + ", '" + player.getType() + "', " + gameId + ");";

    logger.info(sql);
    update(sql, false);
  }

  /**
   * Saves a player's move to the database for a given game. Note that to use this
   * method, a database connection must already have been made. Note additionally,
   * that in order for the transaction to officially complete, the caller of the
   * function must call the commit() method.
   * 
   * @param move   a Move instance to write to the database
   * @param gameId the game ID to associated move with
   * @throws DbServiceException if an issue occurred executing the INSERT
   *                            statement
   */
  public void saveValidMove(Move move, int gameId) throws DbServiceException {

    if (sqliteConn == null) {
      throw new DbServiceException("Please establish DB connection before "
          + "requesting DB action.");
    }

    String sql = ""
        + "INSERT INTO moves (game_id, player_id, x_coord, y_coord) "
        + "VALUES (" + gameId + ", " + move.getPlayerId() + ", " + move.getMoveX() 
        + ", " + move.getMoveY() + ");";

    logger.info(sql);
    update(sql, false);
  }

  /**
   * Deletes a game from the games table (and because of foreign key constraints
   * with cascade deletion all other rows in the players and moves table
   * corresponding to that game will be deleted. Note that to use this method, a
   * database connection must already have been made. Note additionally, that in
   * order for the transaction to officially complete, the caller of the function
   * must call the commit() method.
   * 
   * @param gameId the ID of the game in the database to delete
   * @throws DbServiceException if an issue occurred executing the DELETE
   *                            statement
   */
  public void deleteGame(int gameId, boolean autoCommit) throws DbServiceException {

    if (sqliteConn == null) {
      throw new DbServiceException("Please establish DB connection before "
          + "requesting DB action.");
    }

    String sql = ""
        + "DELETE FROM games "
        + "WHERE id = " + gameId + ";";
    
    logger.info(sql);
    update(sql, autoCommit);
  }
  

  /**
   * Queries the SQLite database for all moves made so far in a given game.
   * 
   * @param gameId the game Id to use in looking for associated moves
   * @throws DbServiceException if an issue occurred executing the SELECT
   *                            statement
   */
  public List<Move> findAllMoves(int gameId) throws DbServiceException {
    
    ArrayList<Move> moves = new ArrayList<Move>();
    Statement statement = null;
    ResultSet rs = null;

    String sql = "" 
        + "SELECT " 
        + "  moves.*, " 
        + "  players.player_type " 
        + "FROM moves " 
        + "LEFT JOIN players "
        + "  ON moves.player_id = players.id "
        + "WHERE moves.game_id = " + gameId + ";";

    logger.info(sql);
    
    try {
      statement = sqliteConn.createStatement();
      rs = statement.executeQuery(sql);
      
      while (rs.next()) {
        Player player = new Player(rs.getString("player_type").charAt(0), rs.getInt("player_id"));
        moves.add(new Move(player, rs.getInt("x_coord"), rs.getInt("y_coord")));
      }
      
    } catch (SQLException e) {
      logger.error(e.getClass().getName() + ": " + e.getMessage());
      throw new DbServiceException("Error occurred fetching moves from database.");
      
    } finally {
      try {
        if (statement != null) {
          statement.close();
        }
        if (rs != null) {
          rs.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DbServiceException("Error occurred while cleaning up SQL statement.");
      }
    }
    
    return moves;
  }

  /**
   * Queries the SQLite database for all the players in a given game.
   * 
   * @param gameId the game Id to use in looking for associated players
   * @throws DbServiceException if an issue occurred executing the SELECT
   *                            statement
   */
  public List<Player> findAllPlayers(int gameId) throws DbServiceException {
    
    ArrayList<Player> players = new ArrayList<Player>();
    Statement statement = null;
    ResultSet rs = null;
    
    String sql = "SELECT * FROM players WHERE game_id = " + gameId + ";";
    logger.info(sql);
    
    try {
      statement = sqliteConn.createStatement();
      rs = statement.executeQuery(sql);
      
      while (rs.next()) {
        players.add(new Player(rs.getString("player_type").charAt(0), rs.getInt("id")));
      }

    } catch (SQLException e) {
      logger.error(e.getClass().getName() + ": " + e.getMessage());
      throw new DbServiceException("Error occurred fetching players from database.");
    } finally {
      try {
        if (statement != null) {
          statement.close();
        }
        if (rs != null) {
          rs.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DbServiceException("Error occurred while cleaning up SQL statement.");
      }
    }
    
    return players;
  }
  

  /**
   * Creates the 'games' table, which holds the id of the game and if there is a
   * winner or a draw, if it doesn't already exist.
   * 
   * @throws DbServiceException if table could not be created
   */
  private void createGamesTable() throws DbServiceException {

    String sql = ""
        + "CREATE TABLE IF NOT EXISTS games (\n"
        + "  id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
        + "  has_started INTEGER DEFAULT NULL,\n"
        + "  winner_id INTEGER DEFAULT NULL,\n"
        + "  is_draw INTEGER DEFAULT NULL,\n"
        + "  turn INTEGER DEFAULT NULL\n"
        + ");";

    update(sql, false);
  }
  
  /**
   * Creates the 'players' table, which holds the 
   * id and the player type for each player in the game,
   * if it doesn't already exist.
   * 
   * @throws DbServiceException if table could not be created
   */
  private void createPlayersTable() throws DbServiceException {

    String sql = ""
        + "CREATE TABLE IF NOT EXISTS players (\n"
        + "  id INTEGER PRIMARY KEY,  \n"
        + "  player_type TEXT NOT NULL,\n"
        + "  game_id INTEGER NOT NULL, \n"
        + "  FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE\n"
        + ");";

    update(sql, false);
  }
  
  /**
   * Creates the 'moves' table, which holds each 
   * move that has been submitted by a user over
   * the course of a game,  if it doesn't already exist.
   * 
   * @throws DbServiceException if table could not be created
   */
  private void createMovesTable() throws DbServiceException {
      
    String sql = ""
        + "CREATE TABLE IF NOT EXISTS  moves (\n"
        + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
        + "  game_id INTEGER NOT NULL,\n"
        + "  player_id INTEGER NOT NULL,\n" 
        + "  x_coord INT NOT NULL,\n"
        + "  y_coord INT NOT NULL,\n"
        + "  FOREIGN KEY (game_id)  REFERENCES games (id) ON DELETE CASCADE,\n" 
        + "  FOREIGN KEY (player_id) REFERENCES players (id) ON DELETE CASCADE\n"
        + ");";

    update(sql, false);
  }
  
  
  /**
   * Call this function to officially commit changes to the database
   * associated with a specific transaction.
   * 
   * @throws DbServiceException if commit failed
   */
  public void commit() throws DbServiceException {
    if (sqliteConn == null) {
      return;
    }

    try {
      sqliteConn.commit();
      
    } catch (SQLException e) {
      logger.error(e.getClass().getName() + ": " + e.getMessage());
      throw new DbServiceException("Could not commit changes associated with " 
          + "transaction to the database");
      
    } finally {
      try {
        if (sqliteConn != null) {
          sqliteConn.close();
          sqliteConn = null;
        }
        
      } catch (SQLException e2) {
        logger.error(e2.getClass().getName() + ": " + e2.getMessage());
        throw new DbServiceException("Could not close SQLite db connection.");
      }
    }
  }
  
  /**
   * Call this function to officially roll-back changes to the database
   * associated with a specific transaction.
   * 
   * @throws DbServiceException if something happened with DB connection
   */
  public void close() throws DbServiceException {

    try {
      if (sqliteConn != null) {
        sqliteConn.close();
        sqliteConn = null;
      }
    } catch (SQLException e) {
      logger.error(e.getClass().getName() + ": " + e.getMessage());
      throw new DbServiceException("An issue was encountered closing connection.");
    }
  }
  
  /**
   * Executes update statement on SQLite database utilizing the SQL statement
   * provided. Use this method for create, update, and delete SQL statements.
   * Allows a user to specify whether to not to use 'auto-commit' functionality.
   * Selecting 'false' allows a user to have control over when the changes are
   * officially committed.
   * 
   * @param sql        A string representing a SQL create/update/deletion
   *                   statement
   * @param autoCommit boolean, true to allow SQLite to auto-commit
   * @throws DbServiceException if a SQLException was thrown during the execution
   */
  private void update(String sql, boolean autoCommit) throws DbServiceException {
    if (sqliteConn == null) {
      throw new DbServiceException("Please establish database connection before "
          + "atempting to execute a SQL create/update/delete statement.");
    }
    
    Statement statement = null;
    try {
      sqliteConn.setAutoCommit(autoCommit);
      statement = sqliteConn.createStatement();
      statement.executeUpdate(sql);
      statement.close();
      
    } catch (SQLException e) {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      }
      logger.error(e.getClass().getName() + ": " + e.getMessage());
      throw new DbServiceException("Error occurred performing database action.");
    }
  }
}
