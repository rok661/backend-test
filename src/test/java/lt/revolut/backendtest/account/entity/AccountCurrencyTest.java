package lt.revolut.backendtest.account.entity;

import static org.junit.Assert.*;

import lt.revolut.backendtest.common.exception.CurrencyException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AccountCurrencyTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void valueOfSupportedCurrencies() {
    assertEquals("EUR", AccountCurrency.valueOf("EUR").getCurrencyCode());
    assertEquals("USD", AccountCurrency.valueOf("USD").getCurrencyCode());
    assertEquals("GBP", AccountCurrency.valueOf("GBP").getCurrencyCode());
  }

  @Test
  public void valueOfFailForNotSupportedCurrency() {
    expectedEx.expect(CurrencyException.class);
    expectedEx.expectMessage("Currency code LTL is not supported");

    AccountCurrency.valueOf("LTL");
  }
}