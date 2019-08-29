package lt.revolut.backendtest.common.exception;

/**
 * Generic app exception
 */
public class AppBusinessException extends RuntimeException {

  AppBusinessException(String message) {
    super(message);
  }

  AppBusinessException(String message, Exception e) {
    super(message, e);
  }
}
