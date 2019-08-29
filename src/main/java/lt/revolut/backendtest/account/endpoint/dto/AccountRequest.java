package lt.revolut.backendtest.account.endpoint.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NonNull
@Builder
public class AccountRequest {

  private String accountCurrency;
  private String ibanCode;
  private String beneficiary;
}
