package lt.revolut.backendtest.common.lock;

public interface LockManager {

  void executeLockedResource(Object id, Runnable runnable);

  void executeLockedResources(Object id1, Object id2, Runnable runnable);
}
