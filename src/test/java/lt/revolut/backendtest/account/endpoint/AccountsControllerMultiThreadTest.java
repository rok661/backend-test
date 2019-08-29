package lt.revolut.backendtest.account.endpoint;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.with;
import static org.junit.Assert.assertEquals;

import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lt.revolut.backendtest.App;
import lt.revolut.backendtest.account.endpoint.dto.AccountRequest;
import lt.revolut.backendtest.account.endpoint.dto.TransactionRequest;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.common.di.AppInjector;
import lt.revolut.backendtest.common.di.BindingsModuleForTesting;
import lt.revolut.backendtest.common.properties.SystemProperties;
import lt.revolut.backendtest.restassured.RestAssuredInitHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountsControllerMultiThreadTest {

  private static final Logger logger = LoggerFactory.getLogger(AccountsControllerMultiThreadTest.class);
  private static final String TRANS_NAME = "Test transaction";

  @BeforeClass
  public static void setUp() {
    SystemProperties.initTest();
    AppInjector.init(new BindingsModuleForTesting());
    App.prepareDB();
    App.startServer(false);
    RestAssuredInitHelper.initRestAssured();
    initAccounts();
  }

  @AfterClass
  public static void closeResources() {
    App.stopServer();
  }

  public static void initAccounts() {
    //1 account
    AccountRequest account1Request = AccountRequest.builder()
        .accountCurrency("EUR")
        .ibanCode("LT601010012345678901")
        .beneficiary("Test user1")
        .build();
    with().body(account1Request)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts")
        .then()
        .statusCode(200);

    //2 account
    AccountRequest account2Request = AccountRequest.builder()
        .accountCurrency("EUR")
        .ibanCode("BA393385804800211234")
        .beneficiary("Test user1")
        .build();
    with().body(account2Request)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts")
        .then()
        .statusCode(200);

    //Transferring some funds to kick-off money transfer transactions in tests
    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(1000), "EUR");
    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/LT601010012345678901/credit")
        .then()
        .statusCode(200);

    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/BA393385804800211234/credit")
        .then()
        .statusCode(200);
  }

  @Test
  public void transferSuccess() throws InterruptedException {
    Runnable threadRunable = () -> {
      String threadName = Thread.currentThread().getName();
      logger.debug("started thread {}", threadName);

      TransactionRequest transactionRequest = new TransactionRequest(threadName, BigDecimal.valueOf(10), "EUR");
      with().body(transactionRequest)
          .contentType(ContentType.JSON)
          .when()
          .request("POST", "/restapi/v1/accounts/LT601010012345678901/transfer-to/BA393385804800211234")
          .then()
          .statusCode(200);
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

    Account account1After = get("/restapi/v1/accounts/LT601010012345678901").then().statusCode(200).
        extract()
        .as(Account.class);
    Account account2After = get("/restapi/v1/accounts/BA393385804800211234").then().statusCode(200).
        extract()
        .as(Account.class);

    assertEquals(BigDecimal.valueOf(900), account1After.getBalance());
    assertEquals(BigDecimal.valueOf(1100), account2After.getBalance());
  }
}
