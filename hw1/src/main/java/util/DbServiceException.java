package util;

public class DbServiceException extends Exception {
  
  /**
   * Adding default serialVerisonUID auto-set by Eclipse.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor for Exception class.
   * 
   * @param errorMessage String representing issue message/information
   */
  public DbServiceException(String errorMessage) {
    super(errorMessage);
  }
}
