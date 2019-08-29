package lt.revolut.backendtest.account.endpoint.dto;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.account.entity.AccountCurrency;
import lt.revolut.backendtest.account.entity.Iban;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountRequestToAccountMapper {

  public static Account toAccount(AccountRequest accountRequest) {
    return Account.builder()
        .accountCurrency(AccountCurrency.valueOf(accountRequest.getAccountCurrency()))
        .beneficiary(accountRequest.getBeneficiary())
        .iban(Iban.valueOf(accountRequest.getIbanCode()))
        .balance(BigDecimal.valueOf(0))
        .build();
  }
}
