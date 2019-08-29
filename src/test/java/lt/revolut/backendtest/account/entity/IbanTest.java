package lt.revolut.backendtest.account.entity;

import lt.revolut.backendtest.common.exception.AppValidationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IbanTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void validateSuccess() {
    Iban.validate("AD1400080001001234567890");
  }

  @Test
  public void validateFail() {
    expectedEx.expect(AppValidationException.class);
    expectedEx.expectMessage("Iban AD14000800013001234567890 validation failed");

    Iban.validate("AD14000800013001234567890");
  }
}
