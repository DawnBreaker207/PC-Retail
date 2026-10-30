package com.dawn.payment.service;

import com.dawn.common.lock.AdvisoryLocks;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.port.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

    private static final long RECONCILE_LOCK_ID = 1004L;
    private static final int PENDING_TIMEOUT_HOURS = 1;

    private final PaymentRepository paymentRepository;
    private final AdvisoryLocks advisoryLocks;

    /** Runs at 01:00 AM daily. Expires stale PENDING payments and logs a summary. */
    @Scheduled(cron = "0 0 1 * * *")
    public void reconcile() {
        advisoryLocks.runIfLeader(RECONCILE_LOCK_ID, this::doReconcile);
    }

    @Transactional
    void doReconcile() {
        Instant cutoff = Instant.now().minus(PENDING_TIMEOUT_HOURS, ChronoUnit.HOURS);
        List<Payment> pending = paymentRepository.findByStatus(PaymentResultStatus.PENDING);

        int expired = 0;
        for (Payment p : pending) {
            if (p.getCreatedAt() != null && p.getCreatedAt().isBefore(cutoff)) {
                p.setStatus(PaymentResultStatus.FAILED);
                paymentRepository.save(p);
                expired++;
            }
        }

        log.info("[Reconciliation] pending={} expired={} cutoff={}",
                pending.size(), expired, cutoff);
    }
}
