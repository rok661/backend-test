package lt.revolut.backendtest.account.entity;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lt.revolut.backendtest.common.exception.CurrencyException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountCurrency {

  @NonNull
  @Getter
  private String currencyCode;

  private AccountCurrency(@NonNull String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public static AccountCurrency valueOf(@NonNull String code) {
    Currency foundCurrency = getSupportedCurrencies().stream()
        .filter(c -> c.getCurrencyCode().equals(code))
        .findFirst()
        .orElseThrow(() -> new CurrencyException(
            String.format("Currency code %s is not supported", code)));

    return new AccountCurrency(foundCurrency.getCurrencyCode());
  }

  private static List<Currency> getSupportedCurrencies() {
    return Arrays.asList(Currency.getInstance("EUR"), Currency.getInstance("GBP"), Currency.getInstance("USD"));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountCurrency that = (AccountCurrency) o;
    return Objects.equals(currencyCode, that.currencyCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currencyCode);
  }

  @Override
  public String toString() {
    return currencyCode;
  }
}
