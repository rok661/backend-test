package lt.revolut.backendtest.common.properties;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemProperties {

  private static Properties properties;

  public static void init() {
    loadProperties("application.properties");
  }

  public static void initTest() {
    loadProperties("application-test.properties");
  }

  private static void loadProperties(String propFileName) {
    try {
      Properties propertiesToLoad = new Properties();
      InputStream inputStream = SystemProperties.class.getClassLoader().getResourceAsStream(propFileName);

      if (inputStream != null) {
        propertiesToLoad.load(inputStream);
      } else {
        throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
      }
      properties = propertiesToLoad;
    } catch (IOException ioexception) {
      throw new IllegalStateException("Can't load system properties in file:" + propFileName, ioexception);
    }
  }

  public static String getProperty(@NonNull String name) {
    checkNotNull(properties, "Properties are not initailized");
    return properties.getProperty(name);
  }
}