package lt.revolut.backendtest.account.repository;

import java.math.BigDecimal;
import java.util.Optional;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.account.entity.Iban;

public interface AccountRepository {

  Optional<Account> findAccountByIban(Iban iban);

  Account insertAccount(Account account);

  void updateAccountInDb(BigDecimal accountId, BigDecimal amount);
}
