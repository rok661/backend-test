package lt.revolut.backendtest.common.exception;

public class NotFoundException extends AppBusinessException {

  public NotFoundException(String message) {
    super(message);
  }
}
