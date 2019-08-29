package lt.revolut.backendtest.common.exception;

public class StorageException extends AppBusinessException {

  public StorageException(String message) {
    super(message);
  }

  public StorageException(String message, Exception e) {
    super(message, e);
  }
}
