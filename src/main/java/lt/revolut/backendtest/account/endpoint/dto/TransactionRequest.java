package lt.revolut.backendtest.account.endpoint.dto;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@NonNull
@ToString
public class TransactionRequest {

  private String description;
  private BigDecimal amount;
  private String currency;
}
