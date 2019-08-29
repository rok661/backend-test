package lt.revolut.backendtest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lt.revolut.backendtest.common.db.AppDataSource;
import lt.revolut.backendtest.common.di.AppInjector;
import lt.revolut.backendtest.common.di.BindingsModule;
import lt.revolut.backendtest.common.properties.SystemProperties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.flywaydb.core.Flyway;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private static final Logger logger = LoggerFactory.getLogger(App.class);

  private static Server server;

  public static void main(String[] args) {
    prepareProperties();
    prepareDependencyInjections();
    prepareDB();
    startServer(true);
  }

  private static void prepareProperties() {
    SystemProperties.init();
  }

  private static void prepareDependencyInjections() {
    AppInjector.init(new BindingsModule());
  }

  public static void prepareDB() {
    AppDataSource dataSource = AppInjector.getInstance().getInstance(AppDataSource.class);

    try (Connection conn = dataSource.getConnection(); PreparedStatement preparedStatement = conn.prepareStatement("DROP ALL OBJECTS")) {
      preparedStatement.execute();
    } catch (SQLException e) {
      throw new IllegalStateException("Preparation of DB failed");
    }

    Flyway flyway = Flyway.configure().dataSource(SystemProperties.getProperty("app.datasource.url"), SystemProperties.getProperty("app.datasource.username"), SystemProperties.getProperty("app.datasource.password")).load();
    flyway.migrate();
  }

  public static void startServer(boolean join) {
    server = new Server(Integer.parseInt(SystemProperties.getProperty("app.server.port")));

    ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

    ctx.setContextPath("/");
    server.setHandler(ctx);

    ServletHolder servletHolder = ctx.addServlet(ServletContainer.class, "/restapi/*");
    servletHolder.setInitOrder(1);
    servletHolder.setInitParameter("jersey.config.server.provider.packages", "lt.revolut.backendtest");

    try {
      server.start();
      if (join) {
        server.join();
      }
    } catch (Exception e) {
      logger.error("Fatal - exception got on jetty server running", e);
    } finally {
      if (join) {
        server.destroy();
      }
    }
  }

  public static void stopServer() {
    if (!server.isStopped()) {
      try {
        server.stop();
      } catch (Exception e) {
        throw new IllegalStateException("Unknown jetty stopping error", e);
      }
    }
  }
}