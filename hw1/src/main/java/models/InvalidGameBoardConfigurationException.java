package models;

public class InvalidGameBoardConfigurationException extends RuntimeException {
  
  // Added default serialVersionUID
  private static final long serialVersionUID = 1L;
  
  /**
   * Constructor for Exception class.
   * 
   * @param errorMessage String representing issue message/information
   */
  public InvalidGameBoardConfigurationException(String errorMessage) {
    super(errorMessage);
  }
}
