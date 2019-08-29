package lt.revolut.backendtest.account.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lt.revolut.backendtest.common.exception.AppValidationException;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Iban {

  @NonNull
  @Getter
  private String ibanCode;

  public static Iban valueOf(String ibanCode) {
    validate(ibanCode);
    return new Iban(ibanCode);
  }

  public static void validate(String ibanCode) {
    // Using Iban4J for validation
    try {
      IbanUtil.validate(ibanCode);
    } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {
      throw new AppValidationException(String.format("Iban %s validation failed", ibanCode), e);
    }
  }

  @Override
  public String toString() {
    return this.ibanCode;
  }
}
