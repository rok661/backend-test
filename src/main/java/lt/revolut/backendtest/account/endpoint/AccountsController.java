package lt.revolut.backendtest.account.endpoint;

import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lt.revolut.backendtest.account.endpoint.dto.AccountRequest;
import lt.revolut.backendtest.account.endpoint.dto.AccountRequestToAccountMapper;
import lt.revolut.backendtest.account.endpoint.dto.TransactionRequest;
import lt.revolut.backendtest.account.endpoint.dto.TransactionRequestValidator;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.account.entity.Iban;
import lt.revolut.backendtest.account.service.AccountService;
import lt.revolut.backendtest.account.service.MoneyTransferService;
import lt.revolut.backendtest.common.di.AppInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountsController {

  private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);
  private static final String ERROR_MSG_ACCOUNT_NOT_FOUND = "Account with iban %s not found";

  private AccountService accountService;
  private MoneyTransferService moneyTransferService;

  public AccountsController() {
    accountService = AppInjector.getInstance().getInstance(AccountService.class);
    moneyTransferService = AppInjector.getInstance().getInstance(MoneyTransferService.class);
  }

  @POST
  public Response create(AccountRequest accountRequest) {
    Account accountToCreate = AccountRequestToAccountMapper.toAccount(accountRequest);
    Account createdAccount = accountService.createAccount(accountToCreate);

    return Response.ok()
        .entity(createdAccount)
        .build();
  }

  @GET
  @Path("{iban}")
  public Account findAccountByIban(@PathParam(value = "iban") String iban) {
    logger.info("findAccountByIban for iban {}", iban);

    Iban.validate(iban);
    Optional<Account> accountOptional = accountService.findAccountByIban(Iban.valueOf(iban));

    return accountOptional.orElseThrow(() -> new RuntimeException(String.format("Account not found by %s iban", iban)));
  }

  @POST
  @Path("{iban}/credit")
  public Response credit(
      @PathParam(value = "iban") String iban,
      TransactionRequest transactionRequest) {
    logger.info("credit call for iban {} with {}", iban, transactionRequest);

    Iban ibanValueObject = Iban.valueOf(iban);
    TransactionRequestValidator.validate(transactionRequest);
    Account accountFrom = accountService.findAccountByIban(ibanValueObject).orElseThrow(() -> new NotFoundException(String.format(ERROR_MSG_ACCOUNT_NOT_FOUND, iban)));
    moneyTransferService.credit(accountFrom, transactionRequest);

    return Response.ok().build();
  }

  @POST
  @Path("{iban}/debit")
  public Response debit(
      @PathParam(value = "iban") String iban,
      TransactionRequest transactionRequest) {
    logger.info("debit call for iban {} with {}", iban, transactionRequest);

    Iban ibanValueObject = Iban.valueOf(iban);
    TransactionRequestValidator.validate(transactionRequest);
    Account accountFrom = accountService.findAccountByIban(ibanValueObject).orElseThrow(() -> new NotFoundException(String.format(ERROR_MSG_ACCOUNT_NOT_FOUND, iban)));
    moneyTransferService.debit(accountFrom, transactionRequest);

    return Response.ok().build();
  }

  @POST
  @Path("{iban}/transfer-to/{ibanTo}")
  public Response makeTransfer(
      @PathParam(value = "iban") String iban,
      @PathParam(value = "ibanTo") String ibanTo,
      TransactionRequest transactionRequest) {
    logger.info("makeTransfer call for iban {} with {}", iban, transactionRequest);

    Account accountFrom = accountService.findAccountByIban(Iban.valueOf(iban)).orElseThrow(() -> new NotFoundException(String.format(ERROR_MSG_ACCOUNT_NOT_FOUND, iban)));
    Account accountTo = accountService.findAccountByIban(Iban.valueOf(ibanTo)).orElseThrow(() -> new NotFoundException(String.format(ERROR_MSG_ACCOUNT_NOT_FOUND, iban)));

    TransactionRequestValidator.validate(transactionRequest);
    moneyTransferService.transfer(accountFrom, accountTo, transactionRequest);

    return Response.ok().build();
  }
}
