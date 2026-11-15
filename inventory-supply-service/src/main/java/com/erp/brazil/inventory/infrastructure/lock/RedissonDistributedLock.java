package com.erp.brazil.inventory.infrastructure.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class RedissonDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedissonDistributedLock.class);

    private final RedissonClient redissonClient;

    public RedissonDistributedLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Executes a task with a distributed lock.
     * Uses Redisson's watchdog for automatic lease renewal (default 30 seconds).
     *
     * @param lockKey The lock key (e.g., "lock:inventory:{itemId}:{warehouseId}")
     * @param leaseTime Lease time in seconds
     * @param timeUnit Time unit for lease time
     * @param task The task to execute
     * @param <T> Return type
     * @return Task result
     */
    public <T> T executeWithLock(String lockKey, long leaseTime, TimeUnit timeUnit, Callable<T> task) {
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            acquired = lock.tryLock(leaseTime, timeUnit);
            if (!acquired) {
                throw new LockAcquisitionException("Could not acquire lock: " + lockKey + " within " + leaseTime + " " + timeUnit.name().toLowerCase());
            }

            log.debug("Acquired lock: {}", lockKey);
            return task.call();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockInterruptedException("Lock acquisition interrupted for: " + lockKey, e);
        } catch (Exception e) {
            if (e instanceof LockAcquisitionException || e instanceof LockInterruptedException) {
                throw e;
            }
            throw new LockExecutionException("Error executing task with lock: " + lockKey, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released lock: {}", lockKey);
            }
        }
    }

    /**
     * Executes a task with a distributed lock (Supplier version for void returns).
     */
    public void executeWithLock(String lockKey, long leaseTime, TimeUnit timeUnit, Runnable task) {
        executeWithLock(lockKey, leaseTime, timeUnit, () -> {
            task.run();
            return null;
        });
    }

    /**
     * Executes a task with multiple distributed locks acquired in order to avoid deadlocks.
     * All locks are acquired before executing the task, and released in reverse order.
     *
     * @param lockKeys List of lock keys (must be in consistent order across all callers)
     * @param leaseTime Lease time in seconds
     * @param timeUnit Time unit for lease time
     * @param task The task to execute
     * @param <T> Return type
     * @return Task result
     */
    public <T> T executeWithMultipleLocks(List<String> lockKeys, long leaseTime, TimeUnit timeUnit, Callable<T> task) {
        List<RLock> locks = lockKeys.stream()
                .map(redissonClient::getLock)
                .toList();

        boolean acquired = false;
        try {
            // Acquire all locks in order
            for (RLock lock : locks) {
                boolean lockAcquired = lock.tryLock(leaseTime, timeUnit);
                if (!lockAcquired) {
                    throw new LockAcquisitionException("Could not acquire lock: " + lock.getName() + " within " + leaseTime + " " + timeUnit.name().toLowerCase());
                }
                log.debug("Acquired lock: {}", lock.getName());
            }

            acquired = true;
            return task.call();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockInterruptedException("Lock acquisition interrupted", e);
        } catch (Exception e) {
            if (e instanceof LockAcquisitionException || e instanceof LockInterruptedException) {
                throw e;
            }
            throw new LockExecutionException("Error executing task with multiple locks", e);
        } finally {
            if (acquired) {
                // Release in reverse order
                for (int i = locks.size() - 1; i >= 0; i--) {
                    RLock lock = locks.get(i);
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.debug("Released lock: {}", lock.getName());
                    }
                }
            } else {
                // Release any locks that were acquired before failure
                for (RLock lock : locks) {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.debug("Released lock (cleanup): {}", lock.getName());
                    }
                }
            }
        }
    }

    /**
     * Checks if a lock is currently held by any thread.
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    /**
     * Gets the remaining time to live for a lock.
     */
    public long getLockRemainingTime(String lockKey, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.remainTimeToLive(timeUnit);
    }

    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }

    public static class LockInterruptedException extends RuntimeException {
        public LockInterruptedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class LockExecutionException extends RuntimeException {
        public LockExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}