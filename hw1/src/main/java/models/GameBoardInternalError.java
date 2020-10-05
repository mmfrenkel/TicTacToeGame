package models;

public class GameBoardInternalError extends Exception {
  
  // Added default serialVersionUID
  private static final long serialVersionUID = 1L;

  /**
   * Constructor for Exception class.
   * 
   * @param errorMessage String representing issue message/information
   */
  public GameBoardInternalError(String errorMessage) {
    super(errorMessage);
  }
}