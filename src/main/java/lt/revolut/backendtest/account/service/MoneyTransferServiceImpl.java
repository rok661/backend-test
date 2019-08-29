package lt.revolut.backendtest.account.service;

import java.math.BigDecimal;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lt.revolut.backendtest.account.endpoint.dto.TransactionRequest;
import lt.revolut.backendtest.account.endpoint.dto.TransactionRequestValidator;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.account.entity.AccountCurrency;
import lt.revolut.backendtest.account.entity.Iban;
import lt.revolut.backendtest.account.repository.AccountRepository;
import lt.revolut.backendtest.account.repository.AccountRepositoryImpl;
import lt.revolut.backendtest.common.db.AppDataSource;
import lt.revolut.backendtest.common.db.TransactionManager;
import lt.revolut.backendtest.common.di.AppInjector;
import lt.revolut.backendtest.common.exception.AppValidationException;
import lt.revolut.backendtest.common.exception.CurrencyException;
import lt.revolut.backendtest.common.exception.NotFoundException;
import lt.revolut.backendtest.common.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MoneyTransferServiceImpl implements MoneyTransferService {

  private static final Logger logger = LoggerFactory.getLogger(MoneyTransferServiceImpl.class);

  private LockManager lockManager;

  @Inject
  public MoneyTransferServiceImpl(LockManager lockManager) {
    this.lockManager = lockManager;
  }

  @Override
  public void transfer(@NonNull Account accountFrom, @NonNull Account accountTo, TransactionRequest transactionRequest) {
    logger.debug("Starting making transfer of transaction {}", transactionRequest.getDescription());

    //Validation
    TransactionRequestValidator.validate(transactionRequest);

    AccountCurrency transferCurrency = AccountCurrency.valueOf(transactionRequest.getCurrency());
    validateCurrencyForAccount(accountFrom, transferCurrency);
    validateCurrencyForAccount(accountTo, transferCurrency);

    // Preparing transaction
    AppDataSource appDataSource = AppInjector.getInstance().getInstance(AppDataSource.class);
    try (TransactionManager transactionManager = new TransactionManager(appDataSource)) {
      lockManager.executeLockedResources(accountFrom.getAccountId(),
          accountTo.getAccountId(),
          () -> {
            //Accounts locked
            debit(accountFrom.getIban(), transactionRequest, transactionManager);
            credit(accountTo.getIban(), transactionRequest, transactionManager);
            transactionManager.commit();
          });
    }
  }

  @Override
  public void debit(@NonNull Account account, @NonNull TransactionRequest transactionRequest) {

    AppDataSource appDataSource = AppInjector.getInstance().getInstance(AppDataSource.class);

    try (TransactionManager transactionManager = new TransactionManager(appDataSource)) {
      lockManager.executeLockedResource(account.getAccountId(), () -> {
        debit(account.getIban(), transactionRequest, transactionManager);
        transactionManager.commit();
      });
    }
  }

  @Override
  public void credit(@NonNull Account account, @NonNull TransactionRequest transactionRequest) {

    AppDataSource appDataSource = AppInjector.getInstance().getInstance(AppDataSource.class);

    try (TransactionManager transactionManager = new TransactionManager(appDataSource)) {
      credit(account.getIban(), transactionRequest, transactionManager);
      transactionManager.commit();
    }
  }

  /**
   * This method should be executed always with locked account
   */
  private void debit(@NonNull Iban iban, @NonNull TransactionRequest transactionRequest, @NonNull TransactionManager transactionManager) {

    TransactionRequestValidator.validate(transactionRequest);

    AccountRepository accountRepository = new AccountRepositoryImpl(transactionManager);
    Account account = accountRepository.findAccountByIban(iban).orElseThrow(() -> new NotFoundException(String.format("Account with iban %s not found", iban)));

    AccountCurrency transferCurrency = AccountCurrency.valueOf(transactionRequest.getCurrency());
    validateCurrencyForAccount(account, transferCurrency);

    BigDecimal balanceFrom = account.getBalance();
    BigDecimal leftAmount = balanceFrom.subtract(transactionRequest.getAmount());
    if (leftAmount.signum() == -1) {
      throw new AppValidationException(
          String.format("Account from iban %s is having not enough funds balance to make a money transfer. Missing balance is %.2f", account.getIban(), leftAmount)
      );
    }

    accountRepository.updateAccountInDb(account.getAccountId(), leftAmount);
  }

  /**
   * This method should be executed always with locked account
   */
  private void credit(@NonNull Iban iban, @NonNull TransactionRequest transactionRequest, @NonNull TransactionManager transactionManager) {

    TransactionRequestValidator.validate(transactionRequest);

    AccountRepository accountRepository = new AccountRepositoryImpl(transactionManager);
    Account account = accountRepository.findAccountByIban(iban).orElseThrow(() -> new NotFoundException(String.format("Account with iban %s not found", iban)));

    BigDecimal balanceFrom = account.getBalance();
    BigDecimal leftAmount = balanceFrom.add(transactionRequest.getAmount());

    accountRepository.updateAccountInDb(account.getAccountId(), leftAmount);
  }

  private void validateCurrencyForAccount(Account account, AccountCurrency transferCurrency) {
    if (!account.getAccountCurrency().equals(transferCurrency)) {
      throw new CurrencyException(String.format("Account %s holds %s currency, but money transaction requested %s", account.getIban(), account.getAccountCurrency(), transferCurrency));
    }
  }
}
