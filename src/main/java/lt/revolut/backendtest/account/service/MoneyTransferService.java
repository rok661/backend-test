package lt.revolut.backendtest.account.service;

import lt.revolut.backendtest.account.endpoint.dto.TransactionRequest;
import lt.revolut.backendtest.account.entity.Account;

public interface MoneyTransferService {

  void transfer(Account accountFrom, Account accountTo, TransactionRequest transactionRequest);

  void debit(Account account, TransactionRequest transactionRequest);

  void credit(Account account, TransactionRequest transactionRequest);
}
