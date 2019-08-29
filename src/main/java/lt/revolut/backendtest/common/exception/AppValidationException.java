package lt.revolut.backendtest.common.exception;

public class AppValidationException extends AppBusinessException {

  public AppValidationException(String message) {
    super(message);
  }

  public AppValidationException(String message, Exception e) {
    super(message, e);
  }
}
