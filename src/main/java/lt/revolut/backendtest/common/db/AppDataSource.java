package lt.revolut.backendtest.common.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface AppDataSource {

  Connection getConnection() throws SQLException;
}
