package lt.revolut.backendtest.common.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Singleton;
import lt.revolut.backendtest.common.properties.SystemProperties;

@Singleton
public class AppDataSourceImpl implements AppDataSource {

  private HikariDataSource ds;

  public AppDataSourceImpl() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(SystemProperties.getProperty("app.datasource.url"));
    config.setUsername(SystemProperties.getProperty("app.datasource.username"));
    config.setPassword(SystemProperties.getProperty("app.datasource.password"));

    ds = new HikariDataSource(config);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return ds.getConnection();
  }
}