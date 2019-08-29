package lt.revolut.backendtest.account.service;

import java.util.Optional;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.account.entity.Iban;

public interface AccountService {

  Account createAccount(Account account);

  Optional<Account> findAccountByIban(Iban iban);
}
