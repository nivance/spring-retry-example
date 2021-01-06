package io.github.nivance.retry.example.service;

import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nivance
 */
@Slf4j
@Service
public class CircuitBreakerService {

    @CircuitBreaker(maxAttempts = 2, openTimeout = 5000L, resetTimeout = 10000L, label = "bircuitBreaker")
    public int call() {
        log.info("Calling call method...");
        if (Math.random() > 0.5) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }
            throw new RuntimeException("Exception happened");
        }
        log.info("Success");
        return 1;
    }


    @Recover
    private int recover() {
        log.error("Use recover, Fallback for call invoked");
        return 0;
    }

}
