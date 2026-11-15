package com.erp.brazil.common.infrastructure.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Distributed lock implementation using Redisson (Redis).
 * Provides reentrant locks with automatic expiration and renewal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_LEASE_TIME = 30; // seconds
    private static final long DEFAULT_WAIT_TIME = 10; // seconds

    /**
     * Acquires a lock and executes the given action.
     *
     * @param lockName the lock name
     * @param action the action to execute
     * @param <T> the result type
     * @return the action result
     * @throws InterruptedException if interrupted while waiting
     */
    public <T> T execute(String lockName, Supplier<T> action) throws InterruptedException {
        return execute(lockName, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, action);
    }

    /**
     * Acquires a lock with custom timeout and executes the given action.
     *
     * @param lockName the lock name
     * @param waitTime the maximum time to wait for the lock
     * @param leaseTime the lock lease time (auto-renewal)
     * @param action the action to execute
     * @param <T> the result type
     * @return the action result
     * @throws InterruptedException if interrupted while waiting
     */
    public <T> T execute(String lockName, long waitTime, long leaseTime, Supplier<T> action)
            throws InterruptedException {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockName);
        boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        if (!acquired) {
            throw new LockAcquisitionException("Could not acquire lock: " + lockName +
                    " within " + waitTime + " seconds");
        }
        try {
            log.debug("Lock acquired: {}", lockName);
            return action.get();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released: {}", lockName);
            }
        }
    }

    /**
     * Acquires a lock and executes the given action (void).
     *
     * @param lockName the lock name
     * @param action the action to execute
     * @throws InterruptedException if interrupted while waiting
     */
    public void execute(String lockName, Runnable action) throws InterruptedException {
        execute(lockName, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Acquires a lock with custom timeout and executes the given action (void).
     *
     * @param lockName the lock name
     * @param waitTime the maximum time to wait for the lock
     * @param leaseTime the lock lease time
     * @param action the action to execute
     * @throws InterruptedException if interrupted while waiting
     */
    public void execute(String lockName, long waitTime, long leaseTime, Runnable action)
            throws InterruptedException {
        execute(lockName, waitTime, leaseTime, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Tries to acquire a lock without blocking.
     *
     * @param lockName the lock name
     * @param leaseTime the lock lease time
     * @return true if lock was acquired, false otherwise
     * @throws InterruptedException if interrupted
     */
    public boolean tryLock(String lockName, long leaseTime) throws InterruptedException {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockName);
        return lock.tryLock(leaseTime, TimeUnit.SECONDS);
    }

    /**
     * Tries to acquire a lock with wait time.
     *
     * @param lockName the lock name
     * @param waitTime the maximum time to wait
     * @param leaseTime the lock lease time
     * @return true if lock was acquired, false otherwise
     * @throws InterruptedException if interrupted
     */
    public boolean tryLock(String lockName, long waitTime, long leaseTime) throws InterruptedException {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockName);
        return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
    }

    /**
     * Releases a lock if held by current thread.
     *
     * @param lockName the lock name
     * @return true if lock was released, false if not held
     */
    public boolean unlock(String lockName) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockName);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            return true;
        }
        return false;
    }

    /**
     * Checks if a lock is currently held by any thread.
     *
     * @param lockName the lock name
     * @return true if locked
     */
    public boolean isLocked(String lockName) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockName);
        return lock.isLocked();
    }

    /**
     * Checks if a lock is held by the current thread.
     *
     * @param lockName the lock name
     * @return true if held by current thread
     */
    public boolean isHeldByCurrentThread(String lockName) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockName);
        return lock.isHeldByCurrentThread();
    }

    /**
     * Executes a callable with a lock.
     *
     * @param lockName the lock name
     * @param callable the callable to execute
     * @param <T> the result type
     * @return the callable result
     * @throws Exception if the callable throws or lock acquisition fails
     */
    public <T> T executeCallable(String lockName, Callable<T> callable) throws Exception {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + lockName);
        boolean acquired = lock.tryLock(DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS);
        if (!acquired) {
            throw new LockAcquisitionException("Could not acquire lock: " + lockName +
                    " within " + DEFAULT_WAIT_TIME + " seconds");
        }
        try {
            log.debug("Lock acquired: {}", lockName);
            return callable.call();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released: {}", lockName);
            }
        }
    }

    /**
     * Exception thrown when lock acquisition fails.
     */
    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}