package lt.revolut.backendtest.common.lock;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will test multi-tread update. CountCheck should be protected by locks to return valid result
 */
public class LockManagerTest {

  private static final Logger logger = LoggerFactory.getLogger(LockManagerTest.class);
  private CountCheck countCheck = new CountCheck();

  @Test
  public void executeLockedResourceSuccess() throws InterruptedException {
    LockManager lockManager = new LockManagerImpl();
    countCheck.setValue(0);

    Runnable threadRunable = () -> {
      String threadName = Thread.currentThread().getName();
      logger.debug("started thread {}", threadName);

      //Executes thread and change count value
      Runnable task = () -> {
        int countValue = countCheck.getCount();
        countCheck.setValue(countValue + 1);
        logger.debug("Tread {} count: {}", threadName, countCheck.getCount());
      };

      lockManager.executeLockedResource(BigDecimal.valueOf(1), task);
    };

    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Thread thread = new Thread(threadRunable);
      threads.add(thread);
      logger.debug("Starting thread: {}", i);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join(5000);
    }

    assertEquals(10, countCheck.getCount());
  }

  @Test
  public void executeLockedResources() throws InterruptedException {
    LockManager lockManager = new LockManagerImpl();
    countCheck.setValue(0);

    Runnable threadRunable = () -> {
      String threadName = Thread.currentThread().getName();
      logger.debug("started thread {}", threadName);

      Runnable task = () -> {
        int newValue = countCheck.getCount() + 1;
        countCheck.setValue(newValue);
        logger.debug("Tread {} count: {}", threadName, countCheck.getCount());
      };

      lockManager.executeLockedResources(BigDecimal.valueOf(1), BigDecimal.valueOf(2), task);
    };

    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Thread thread = new Thread(threadRunable);
      threads.add(thread);
      logger.debug("Starting thread: {}", i);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join(5000);
    }

    assertEquals(10, countCheck.getCount());
  }
}

/**
 * Helper class to check multi-threaded unsafe operation
 */
class CountCheck {

  private Integer count;

  public void setValue(int number) {
    count = new Integer(number);
  }

  public int getCount() {
    return count.intValue();
  }
}