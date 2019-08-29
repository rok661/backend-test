package lt.revolut.backendtest.account.endpoint.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lt.revolut.backendtest.common.exception.AppValidationException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionRequestValidator {

  /**
   * @throws AppValidationException
   */
  public static void validate(TransactionRequest transactionRequest) {

    if ((transactionRequest.getAmount() == null)) {
      throw new AppValidationException("amount should be specified");
    }
    if (transactionRequest.getAmount().signum() != 1) {
      throw new AppValidationException("amount should be greater than 0");
    }
    if (transactionRequest.getCurrency() == null) {
      throw new AppValidationException("currency should be spefied");
    }
  }
}
