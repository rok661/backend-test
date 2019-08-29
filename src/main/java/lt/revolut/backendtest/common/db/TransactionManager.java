package lt.revolut.backendtest.common.db;

import java.sql.Connection;
import java.sql.SQLException;
import lt.revolut.backendtest.common.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for database transaction management
 */
public class TransactionManager implements AppDataSource, AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
  private static final String ERROR_MSG = "Unknown error on transaction management";

  private Connection conn;
  private boolean transactionFinished = false;

  public TransactionManager(AppDataSource appDataSource) {
    try {
      this.conn = appDataSource.getConnection();
      conn.setAutoCommit(false);
    } catch (SQLException e) {
      logger.error("Can't get connection from datasource", e);
      throw new StorageException("Can't get connection from datasource", e);
    }
  }

  @Override
  public Connection getConnection() {
    return conn;
  }

  public void commit() {
    try {
      conn.commit();
      transactionFinished = true;
    } catch (
        SQLException sqe) {
      logger.error(ERROR_MSG, sqe);
      throw new StorageException(ERROR_MSG, sqe);
    }
  }

  public void rollback() {
    try {
      conn.rollback();
      transactionFinished = true;
    } catch (
        SQLException sqe) {
      logger.error(ERROR_MSG, sqe);
      throw new StorageException(ERROR_MSG, sqe);
    }
  }

  @Override
  public void close() {
    try {
      if (!transactionFinished) {
        conn.rollback();
      }
      conn.close();
    } catch (Exception e) {
      logger.error(ERROR_MSG, e);
      throw new StorageException(ERROR_MSG, e);
    }
  }
}
