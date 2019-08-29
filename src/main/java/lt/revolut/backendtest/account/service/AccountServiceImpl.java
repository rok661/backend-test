package lt.revolut.backendtest.account.service;

import java.util.Optional;
import javax.inject.Singleton;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.account.entity.Iban;
import lt.revolut.backendtest.account.repository.AccountRepository;
import lt.revolut.backendtest.account.repository.AccountRepositoryImpl;
import lt.revolut.backendtest.common.db.AppDataSource;
import lt.revolut.backendtest.common.db.TransactionManager;
import lt.revolut.backendtest.common.di.AppInjector;

@Singleton
public class AccountServiceImpl implements AccountService {

  @Override
  public Account createAccount(Account accountToCreate) {

    AppDataSource appDataSource = AppInjector.getInstance().getInstance(AppDataSource.class);

    try (TransactionManager transactionManager = new TransactionManager(appDataSource)) {
      AccountRepository accountRepository = new AccountRepositoryImpl(transactionManager);
      Account createdAccount = accountRepository.insertAccount(accountToCreate);
      transactionManager.commit();

      return createdAccount;
    }
  }

  @Override
  public Optional<Account> findAccountByIban(Iban iban) {

    AppDataSource appDataSource = AppInjector.getInstance().getInstance(AppDataSource.class);

    try (TransactionManager transactionManager = new TransactionManager(appDataSource)) {
      AccountRepository accountRepository = new AccountRepositoryImpl(transactionManager);

      return accountRepository.findAccountByIban(iban);
    }
  }
}
