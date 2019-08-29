package lt.revolut.backendtest.common.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LockManagerImpl implements LockManager {

  private static final Logger logger = LoggerFactory.getLogger(LockManagerImpl.class);

  private Map<Object, Lock> locks = new ConcurrentHashMap<>();

  @SuppressWarnings("squid:S2222")
  private void lock(@NonNull Object id) {
    Lock lock = getLock(id);
    lock.lock();
    logger.debug("Locked resource: {}", id);
  }

  private void unlock(@NonNull Object id) {
    Lock lock = getLock(id);
    lock.unlock();
    logger.debug("Unlocked resource: {}", id);
  }

  @Override
  public void executeLockedResource(@NonNull Object id, @NonNull Runnable runnable) {
    lock(id);
    try {
      runnable.run();
    } finally {
      unlock(id);
    }
  }

  @Override
  public void executeLockedResources(@NonNull Object id1, @NonNull Object id2, Runnable runnable) {
    lock(id1);
    lock(id2);
    try {
      runnable.run();
    } finally {
      unlock(id1);
      unlock(id2);
    }
  }

  private synchronized Lock getLock(@NonNull Object id) {
    return locks.computeIfAbsent(id, i -> new ReentrantLock());
  }
}
