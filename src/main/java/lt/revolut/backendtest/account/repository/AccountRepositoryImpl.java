package lt.revolut.backendtest.account.repository;

import static com.google.common.base.Preconditions.checkState;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import lombok.NonNull;
import lt.revolut.backendtest.account.entity.Account;
import lt.revolut.backendtest.account.entity.AccountCurrency;
import lt.revolut.backendtest.account.entity.Iban;
import lt.revolut.backendtest.common.db.TransactionManager;
import lt.revolut.backendtest.common.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountRepositoryImpl implements AccountRepository {

  private static final Logger logger = LoggerFactory.getLogger(AccountRepositoryImpl.class);

  private static final String EXCEPTION_MSG_ACCOUNT = "Exception found on storing account";
  private static final String EXCEPTION_MSG_UPDATE = "Exception on update account";

  private Connection conn;

  public AccountRepositoryImpl(@NonNull TransactionManager transactionManager) {
    this.conn = transactionManager.getConnection();
  }

  @Override
  public Optional<Account> findAccountByIban(@NonNull Iban iban) {
    String sql = "select id, currency, iban, beneficiary, balance from account where iban=?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, iban.getIbanCode());
      try (ResultSet resultSet = stmt.executeQuery()) {
        if (!resultSet.next()) {
          return Optional.empty();
        }

        Account fetchedAccount = Account.builder()
            .accountId(resultSet.getBigDecimal("id"))
            .accountCurrency(AccountCurrency.valueOf(resultSet.getString("currency")))
            .beneficiary(resultSet.getString("beneficiary"))
            .iban(Iban.valueOf(resultSet.getString("iban")))
            .balance(resultSet.getBigDecimal("balance"))
            .build();

        if (resultSet.next()) {
          throw new StorageException("More than one record found for iban: " + iban);
        }

        return Optional.of(fetchedAccount);
      }
    } catch (SQLException sqe) {
      logger.error(EXCEPTION_MSG_ACCOUNT, sqe);
      throw new StorageException(EXCEPTION_MSG_ACCOUNT, sqe);
    }
  }

  @Override
  public Account insertAccount(Account account) {
    String sql = "insert into account (currency, iban, beneficiary, balance) values (?, ?, ?, ?)";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, account.getAccountCurrency().getCurrencyCode());
      stmt.setString(2, account.getIban().getIbanCode());
      stmt.setString(3, account.getBeneficiary());
      stmt.setBigDecimal(4, account.getBalance());

      stmt.executeUpdate();

      logger.debug("account {} created", account);
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        return findAccountByIban(account.getIban()).orElseThrow(() -> new RuntimeException("created account was not found"));
      }
    } catch (SQLException sqe) {
      logger.error(EXCEPTION_MSG_ACCOUNT, sqe);
      throw new StorageException(EXCEPTION_MSG_ACCOUNT, sqe);
    }
  }

  @Override
  public void updateAccountInDb(@NonNull BigDecimal accountId, @NonNull BigDecimal amount) {
    String sql = "update account set balance=? where id=?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setBigDecimal(1, amount);
      stmt.setBigDecimal(2, accountId);
      int rowsUpdated = stmt.executeUpdate();

      checkState(rowsUpdated == 1, "update is not changed record in DB");
      logger.debug("account with id {} updated with amount {}", accountId, amount);
    } catch (SQLException sqe) {
      logger.error(EXCEPTION_MSG_UPDATE, sqe);
      throw new StorageException(EXCEPTION_MSG_UPDATE, sqe);
    }
  }
}
