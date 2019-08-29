package lt.revolut.backendtest.common.di;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class AppInjector {

  private static Injector injector;

  public static void init(Module module) {
    injector = Guice.createInjector(module);
  }

  public static Injector getInstance() {
    return injector;
  }
}
