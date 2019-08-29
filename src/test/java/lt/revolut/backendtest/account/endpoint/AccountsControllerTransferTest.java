package lt.revolut.backendtest.account.endpoint;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.with;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

import io.restassured.http.ContentType;
import java.math.BigDecimal;
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

/**
 * Some helper API tests used for other services than money transfer between accounts.
 */
public class AccountsControllerTransferTest {

  private static String TRANS_NAME = "Test transaction";

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

  private static void initAccounts() {
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

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(200), "EUR");
    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/LT601010012345678901/credit")
        .then()
        .statusCode(200);
  }

  @Test
  public void transferSuccess() {
    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(10), "EUR");

    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/LT601010012345678901/transfer-to/BA393385804800211234")
        .then()
        .statusCode(200);
    Account account1After = get("/restapi/v1/accounts/LT601010012345678901").then().statusCode(200).
        extract()
        .as(Account.class);
    Account account2After = get("/restapi/v1/accounts/BA393385804800211234").then().statusCode(200).
        extract()
        .as(Account.class);

    assertEquals(BigDecimal.valueOf(190), account1After.getBalance());
    assertEquals(BigDecimal.valueOf(10), account2After.getBalance());
  }

  @Test
  public void transferFailNotFound() {
    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(10), "EUR");

    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/CZ5508000000001234567899/transfer-to/BA393385804800211234")
        .then()
        .statusCode(404);

    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/BA393385804800211234/transfer-to/CZ5508000000001234567899")
        .then()
        .statusCode(404)
        .body("message", equalTo("Account with iban BA393385804800211234 not found"));
  }

  @Test
  public void transferFailZeroTransaction() {
    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(0), "EUR");

    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/LT601010012345678901/transfer-to/BA393385804800211234")
        .then()
        .statusCode(400)
        .body("message", equalTo("amount should be greater than 0"));
  }

  @Test
  public void transferFailNegativeTransaction() {
    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(-1), "EUR");

    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/LT601010012345678901/transfer-to/BA393385804800211234")
        .then()
        .statusCode(400)
        .body("message", equalTo("amount should be greater than 0"));
  }


  @Test
  public void transferFailMismatchCurrencyTransfer() {
    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(10), "USD");

    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/LT601010012345678901/transfer-to/BA393385804800211234")
        .then()
        .statusCode(400)
        .body("message", equalTo("Account LT601010012345678901 holds EUR currency, but money transaction requested USD"));
  }

  @Test
  public void transferFailMismatchCurrencyAccunts() {
    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(10), "EUR");

    //1 account
    AccountRequest account1Request = AccountRequest.builder()
        .accountCurrency("USD")
        .ibanCode("AD1400080001001234567890")
        .beneficiary("Test user1")
        .build();

    with().body(account1Request)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts")
        .then()
        .statusCode(200);

    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/LT601010012345678901/transfer-to/AD1400080001001234567890")
        .then()
        .statusCode(400)
        .body("message", equalTo("Account AD1400080001001234567890 holds USD currency, but money transaction requested EUR"));
  }
}
