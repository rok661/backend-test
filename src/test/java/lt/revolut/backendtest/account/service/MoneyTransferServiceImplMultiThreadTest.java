package lt.revolut.backendtest.account.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lt.revolut.backendtest.App;
import lt.revolut.backendtest.account.endpoint.dto.TransactionRequest;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.account.entity.AccountCurrency;
import lt.revolut.backendtest.account.entity.Iban;
import lt.revolut.backendtest.common.di.AppInjector;
import lt.revolut.backendtest.common.di.BindingsModuleForTesting;
import lt.revolut.backendtest.common.properties.SystemProperties;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoneyTransferServiceImplMultiThreadTest {

  private static final Logger logger = LoggerFactory.getLogger(MoneyTransferServiceImplMultiThreadTest.class);

  private Account account1;
  private Account account2;

  private MoneyTransferService moneyTransferService;
  private AccountService accountService;

  @Before
  public void setUp() {
    SystemProperties.initTest();
    AppInjector.init(new BindingsModuleForTesting());
    App.prepareDB();
    moneyTransferService = AppInjector.getInstance().getInstance(MoneyTransferService.class);
    accountService = AppInjector.getInstance().getInstance(AccountService.class);

    Account account1, account2;
    account1 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(1000))
        .iban(Iban.valueOf("AD1400080001001234567890"))
        .beneficiary("Test user1")
        .build();
    this.account1 = accountService.createAccount(account1);

    account2 = Account.builder()
        .accountCurrency(AccountCurrency.valueOf("EUR"))
        .balance(BigDecimal.valueOf(1000))
        .iban(Iban.valueOf("AT483200000012345864"))
        .beneficiary("Test user2")
        .build();
    this.account2 = accountService.createAccount(account2);
  }

  @Test
  public void transferMultiThhreadedSuccess() throws InterruptedException {

    Runnable threadRunable = () -> {
      String threadName = Thread.currentThread().getName();
      logger.debug("started thread " + threadName);
      TransactionRequest transactionRequest = new TransactionRequest(threadName, BigDecimal.valueOf(10), "EUR");
      moneyTransferService.transfer(account1, account2, transactionRequest);
    };

    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Thread thread = new Thread(threadRunable);
      thread.setName("thread " + i);
      threads.add(thread);
      thread.start();
    }

    //Waiting to finish all threads
    for (Thread thread : threads) {
      thread.join(5000);
    }

    Account account1After = accountService.findAccountByIban(account1.getIban()).get();
    Account account2After = accountService.findAccountByIban(account2.getIban()).get();

    assertEquals(BigDecimal.valueOf(900), account1After.getBalance());
    assertEquals(BigDecimal.valueOf(1100), account2After.getBalance());
  }
}
