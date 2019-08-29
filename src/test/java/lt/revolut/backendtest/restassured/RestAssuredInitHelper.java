package lt.revolut.backendtest.restassured;

import io.restassured.RestAssured;

public class RestAssuredInitHelper {

  public static void initRestAssured() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = 8080;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }
}
