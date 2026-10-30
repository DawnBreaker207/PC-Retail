package com.dawn.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * PostgreSQL advisory locks for distributed leader-election in scheduled jobs.
 * Uses pg_try_advisory_lock (session-level, non-blocking): returns false immediately
 * if another node holds the lock, so the scheduler skips the run on that node.
 *
 * <pre>{@code
 * advisoryLocks.runIfLeader(42L, () -> {
 *     // only one node runs this block
 *     exportService.generateDailyReport();
 * });
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdvisoryLocks {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Tries to acquire a session-level advisory lock with {@code lockId}.
     * Runs {@code task} only if the lock was acquired, then releases it.
     */
    public void runIfLeader(long lockId, Runnable task) {
        Boolean acquired = jdbcTemplate.queryForObject(
                "SELECT pg_try_advisory_lock(?)", Boolean.class, lockId);
        if (!Boolean.TRUE.equals(acquired)) {
            log.debug("Advisory lock {} held by another instance, skipping", lockId);
            return;
        }
        try {
            task.run();
        } finally {
            jdbcTemplate.execute("SELECT pg_advisory_unlock(" + lockId + ")");
        }
    }

    /**
     * Same as {@link #runIfLeader(long, Runnable)} but returns a result.
     * Returns {@code null} when the lock could not be acquired.
     */
    public <T> T callIfLeader(long lockId, Supplier<T> task) {
        Boolean acquired = jdbcTemplate.queryForObject(
                "SELECT pg_try_advisory_lock(?)", Boolean.class, lockId);
        if (!Boolean.TRUE.equals(acquired)) {
            log.debug("Advisory lock {} held by another instance, skipping", lockId);
            return null;
        }
        try {
            return task.get();
        } finally {
            jdbcTemplate.execute("SELECT pg_advisory_unlock(" + lockId + ")");
        }
    }
}
