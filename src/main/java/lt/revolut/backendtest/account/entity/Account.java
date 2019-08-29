package lt.revolut.backendtest.account.entity;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class Account {

  private static final Logger logger = LoggerFactory.getLogger(Account.class);

  @Getter
  private BigDecimal accountId;
  @NonNull
  @Getter
  private AccountCurrency accountCurrency;
  @NonNull
  @Getter
  private Iban iban;
  @NonNull
  @Getter
  private String beneficiary;
  @NonNull
  @Getter
  private BigDecimal balance;

  public Account(BigDecimal accountId, @NonNull AccountCurrency accountCurrency, @NonNull Iban iban, @NonNull String beneficiary, BigDecimal balance) {
    this.accountId = accountId;
    this.accountCurrency = accountCurrency;
    this.iban = iban;
    this.beneficiary = beneficiary;
    this.balance = balance;

  }
}
