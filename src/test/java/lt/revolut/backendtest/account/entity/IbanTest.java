package lt.revolut.backendtest.account.entity;

import lt.revolut.backendtest.common.exception.AppValidationException;
import org.junit.Test;

public class IbanTest {

  @Test
  public void validateSuccess() {
    Iban.validate("AD1400080001001234567890");
  }

  @Test(expected = AppValidationException.class)
  public void validateFail() {
    Iban.validate("AD14000800013001234567890");
  }
}
