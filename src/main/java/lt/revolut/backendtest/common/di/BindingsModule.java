package lt.revolut.backendtest.common.di;

import com.google.inject.AbstractModule;
import lt.revolut.backendtest.account.service.AccountService;
import lt.revolut.backendtest.account.service.AccountServiceImpl;
import lt.revolut.backendtest.account.service.MoneyTransferService;
import lt.revolut.backendtest.account.service.MoneyTransferServiceImpl;
import lt.revolut.backendtest.common.db.AppDataSource;
import lt.revolut.backendtest.common.db.AppDataSourceImpl;
import lt.revolut.backendtest.common.lock.LockManager;
import lt.revolut.backendtest.common.lock.LockManagerImpl;

public class BindingsModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AppDataSource.class).to(AppDataSourceImpl.class);
    bind(AccountService.class).to(AccountServiceImpl.class);
    bind(MoneyTransferService.class).to(MoneyTransferServiceImpl.class);
    bind(LockManager.class).to(LockManagerImpl.class);
  }
}
