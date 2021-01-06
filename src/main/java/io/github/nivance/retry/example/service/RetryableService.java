package io.github.nivance.retry.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nivance
 */
@Slf4j
@Service
public class RetryableService {

    @Autowired
    private RestTemplate restTemplate;

    private static final int DELAY_TIME = 1000;

    @Retryable(value = RemoteAccessException.class, backoff = @Backoff(DELAY_TIME), label = "retryable", listeners = {"statisticsListener"})
    public void request() {
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://localhost:8080/unstable/500", String.class);
        } catch (Exception e) {
            log.info("Try get unstable api failed");
            throw new RemoteAccessException("500", e);
        }
    }

    @Recover
    private void recover(RemoteAccessException e) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://localhost:8080/unstable/200", String.class);
        log.info(String.format("Use recover, Response is %s", responseEntity.getBody()));
    }


}
