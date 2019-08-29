package lt.revolut.backendtest.common.response.error;

import lombok.Getter;

class ErrorResponse {

  @Getter
  private String message;

  ErrorResponse(String message) {
    this.message = message;
  }
}
