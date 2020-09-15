package models;

public class InvalidGameBoardConfigurationException extends RuntimeException {
  
  // Added default serialVersionUID
  private static final long serialVersionUID = 1L;
  
  public InvalidGameBoardConfigurationException(String errorMessage) {
    super(errorMessage);
  }
}
