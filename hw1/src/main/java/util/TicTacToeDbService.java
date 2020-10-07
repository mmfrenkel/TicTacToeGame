package util;

import java.util.List;
import models.GenericGameBoard;
import models.Move;
import models.Player;


public interface TicTacToeDbService {
  
  public void connect() throws DbServiceException;
  
  public void createDatabasesTables() throws DbServiceException;
  
  public void createNewGame(int gameId) throws DbServiceException;
  
  public List<Move> findAllMoves(int gameId) throws DbServiceException;
  
  public List<Player> findAllPlayers(int gameId) throws DbServiceException;
  
  public GenericGameBoard restoreMostRecentGameBoard() throws DbServiceException;
  
  public GenericGameBoard restoreGameBoard(int gameId) throws DbServiceException;
  
  public void savePlayer(Player player, int gameId) throws DbServiceException;
  
  public void saveGameState(GenericGameBoard gameboard, int gameId) throws DbServiceException;
  
  public void saveValidMove(Move move, int gameId) throws DbServiceException;
  
  public void deleteGame(int gameId, boolean autoCommit) throws DbServiceException;
  
  public void commit() throws DbServiceException;
  
  public void close() throws DbServiceException;

}
