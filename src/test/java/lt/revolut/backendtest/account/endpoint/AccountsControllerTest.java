package lt.revolut.backendtest.account.endpoint;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.with;
import static org.hamcrest.CoreMatchers.equalTo;

import io.restassured.http.ContentType;
import java.math.BigDecimal;
import lt.revolut.backendtest.App;
import lt.revolut.backendtest.account.endpoint.dto.AccountRequest;
import lt.revolut.backendtest.account.endpoint.dto.TransactionRequest;
import lt.revolut.backendtest.common.di.AppInjector;
import lt.revolut.backendtest.common.di.BindingsModuleForTesting;
import lt.revolut.backendtest.common.properties.SystemProperties;
import lt.revolut.backendtest.restassured.RestAssuredInitHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AccountsControllerTest {

  private static final String TRANS_NAME = "test transaction";

  @BeforeClass
  public static void setUp() {
    SystemProperties.initTest();
    AppInjector.init(new BindingsModuleForTesting());
    App.prepareDB();
    App.startServer(false);
    RestAssuredInitHelper.initRestAssured();
  }

  @AfterClass
  public static void closeResources() {
    App.stopServer();
  }

  @Test
  public void createSuccess() {
    AccountRequest account1 = AccountRequest.builder()
        .accountCurrency("EUR")
        .ibanCode("LT601010012345678901")
        .beneficiary("Test user1")
        .build();

    with().body(account1)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts")
        .then()
        .statusCode(200)
        .body("accountCurrency.currencyCode", equalTo(account1.getAccountCurrency()))
        .body("iban.ibanCode", equalTo(account1.getIbanCode()))
        .body("beneficiary", equalTo(account1.getBeneficiary()));

  }

  @Test
  public void findByIbanSuccess() {
    AccountRequest account1 = AccountRequest.builder()
        .accountCurrency("EUR")
        .ibanCode("MU43BOMM0101123456789101000MUR")
        .beneficiary("Test user1")
        .build();

    with().body(account1)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts")
        .then()
        .statusCode(200);

    get("/restapi/v1/accounts/MU43BOMM0101123456789101000MUR").then().statusCode(200).assertThat()
        .body("accountCurrency.currencyCode", equalTo(account1.getAccountCurrency()))
        .body("iban.ibanCode", equalTo(account1.getIbanCode()))
        .body("beneficiary", equalTo(account1.getBeneficiary()));
  }

  @Test
  public void creditSuccess() {
    AccountRequest account1 = AccountRequest.builder()
        .accountCurrency("EUR")
        .ibanCode("MD21EX000000000001234567")
        .beneficiary("Test user1")
        .build();

    with().body(account1)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts")
        .then()
        .statusCode(200);

    TransactionRequest transactionRequest = new TransactionRequest(TRANS_NAME, BigDecimal.valueOf(200), "EUR");
    with().body(transactionRequest)
        .contentType(ContentType.JSON)
        .when()
        .request("POST", "/restapi/v1/accounts/MD21EX000000000001234567/credit")
        .then()
        .statusCode(200);
  }
}
