package lt.revolut.backendtest.account.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import lt.revolut.backendtest.App;
import lt.revolut.backendtest.account.endpoint.dto.TransactionRequest;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.account.entity.AccountCurrency;
import lt.revolut.backendtest.account.entity.Iban;
import lt.revolut.backendtest.common.di.AppInjector;
import lt.revolut.backendtest.common.di.BindingsModuleForTesting;
import lt.revolut.backendtest.common.exception.AppValidationException;
import lt.revolut.backendtest.common.exception.CurrencyException;
import lt.revolut.backendtest.common.properties.SystemProperties;
import org.junit.Before;
import org.junit.Test;

public class MoneyTransferServiceImplTest {

  private static final String TRANS_NAME = "test transaction";

  private MoneyTransferService moneyTransferService;
  private AccountService accountService;

  @Before
  public void setUp() {
    SystemProperties.initTest();
    AppInjector.init(new BindingsModuleForTesting());
    App.prepareDB();
    moneyTransferService = AppInjector.getInstance().getInstance(MoneyTransferService.class);
    accountService = AppInjector.getInstance().getInstance(AccountService.class);
  }

  @Test
  public void creditSuccess() {
    Account account1 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(0))
        .iban(Iban.valueOf("HR1723600001101234565"))
        .beneficiary("Test user1")
        .build();
    account1 = accountService.createAccount(account1);

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(45.55), "EUR");
    moneyTransferService.credit(account1, transactionRequest);

    Account updatedAccount = accountService.findAccountByIban(Iban.valueOf("HR1723600001101234565")).get();
    assertEquals(BigDecimal.valueOf(45.55), updatedAccount.getBalance());
  }

  @Test(expected = AppValidationException.class)
  public void creditFailNegativeAmountInTransaction() {
    Account account1 = Account.builder()
        .accountId(BigDecimal.valueOf(1))
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(0))
        .iban(Iban.valueOf("HR1723600001101234565"))
        .beneficiary("Test user1")
        .build();

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(-10), "EUR");
    moneyTransferService.credit(account1, transactionRequest);
  }

  @Test
  public void debitSuccess() {
    Account account1 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(100))
        .iban(Iban.valueOf("AZ96AZEJ00000000001234567890"))
        .beneficiary("Test user1")
        .build();
    account1 = accountService.createAccount(account1);

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(10.5), "EUR");
    moneyTransferService.debit(account1, transactionRequest);

    Account updatedAccount = accountService.findAccountByIban(Iban.valueOf("AZ96AZEJ00000000001234567890")).get();
    assertEquals(BigDecimal.valueOf(89.5), updatedAccount.getBalance());
  }

  @Test(expected = AppValidationException.class)
  public void debitFailNotEnoughFunds() {
    Account account1 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(100))
        .iban(Iban.valueOf("BE71096123456769"))
        .beneficiary("Test user1")
        .build();
    account1 = accountService.createAccount(account1);

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(101), "EUR");
    moneyTransferService.debit(account1, transactionRequest);
  }

  @Test
  public void transferSuccess() {
    Account account1 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(100))
        .iban(Iban.valueOf("AD1400080001001234567890"))
        .beneficiary("Test user1")
        .build();
    account1 = accountService.createAccount(account1);

    Account account2 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(200))
        .iban(Iban.valueOf("AT483200000012345864"))
        .beneficiary("Test user2")
        .build();
    account2 = accountService.createAccount(account2);

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(45.55), "EUR");
    moneyTransferService.transfer(account1, account2, transactionRequest);

    Account updatedAccount1 = accountService.findAccountByIban(Iban.valueOf("AD1400080001001234567890")).get();
    Account updatedAccount2 = accountService.findAccountByIban(Iban.valueOf("AT483200000012345864")).get();

    assertEquals(BigDecimal.valueOf(54.45), updatedAccount1.getBalance());
    assertEquals(BigDecimal.valueOf(245.55), updatedAccount2.getBalance());
  }

  @Test(expected = AppValidationException.class)
  public void transferFailNotEnoughFunds() {
    Account account1 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(45))
        .iban(Iban.valueOf("BH02CITI00001077181611"))
        .beneficiary("Test user1")
        .build();
    account1 = accountService.createAccount(account1);

    Account account2 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(200))
        .iban(Iban.valueOf("BY86AKBB10100000002966000000"))
        .beneficiary("Test user2")
        .build();
    account2 = accountService.createAccount(account2);

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(45.55), "EUR");
    moneyTransferService.transfer(account1, account2, transactionRequest);
  }

  @Test(expected = CurrencyException.class)
  public void transferFailMisMatchCurrencyInAccounts() {
    Account account1 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("USD"))
        .balance(BigDecimal.valueOf(45))
        .iban(Iban.valueOf("CZ5508000000001234567899"))
        .beneficiary("Test user1")
        .build();
    account1 = accountService.createAccount(account1);

    Account account2 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(200))
        .iban(Iban.valueOf("DK9520000123456789"))
        .beneficiary("Test user2")
        .build();
    account2 = accountService.createAccount(account2);

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(10), "EUR");
    moneyTransferService.transfer(account1, account2, transactionRequest);
  }

  @Test(expected = CurrencyException.class)
  public void transferFailMisMatchCurrencyInTransfer() {
    Account account1 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(45))
        .iban(Iban.valueOf("CZ5508000000001234567899"))
        .beneficiary("Test user1")
        .build();
    account1 = accountService.createAccount(account1);

    Account account2 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(200))
        .iban(Iban.valueOf("DK9520000123456789"))
        .beneficiary("Test user2")
        .build();
    account2 = accountService.createAccount(account2);

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(10), "USD");
    moneyTransferService.transfer(account1, account2, transactionRequest);
  }
}
