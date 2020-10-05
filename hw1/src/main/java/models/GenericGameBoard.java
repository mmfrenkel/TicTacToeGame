package models;

public interface GenericGameBoard {

  public boolean isEmpty();

  public boolean isFull();

  public Message processPlayerMove(Move move) throws GameBoardInternalError;

  public boolean isValidMove(Move move);

  public void playMove(Move move);

  public boolean isWinningMove(int x, int y, char type);
  
  public int getWinner();
  
  public boolean isDraw();
  
  public boolean isGameStarted();
  
  public int getTurn();

}
