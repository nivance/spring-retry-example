package io.github.nivance.retry.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import io.github.nivance.retry.example.service.CircuitBreakerService;
import io.github.nivance.retry.example.service.RetryableService;
import lombok.AllArgsConstructor;

/**
 * @author nivance
 */
@AllArgsConstructor
@RestController
public class RetryController {

    private RetryableService retryableService;
    private CircuitBreakerService circuitBreakerService;

    @GetMapping("/retryable")
    public int requestService() {
        retryableService.request();
        return 1;
    }

    @GetMapping("/circuitBreaker")
    public int callExternalService() {
        return circuitBreakerService.call();
    }



    @GetMapping("/unstable/{status}")
    public int unstableApi(@PathVariable int status) {
        if (INTERNAL_SERVER_ERROR.value() == status) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR);
        }
        if (UNAUTHORIZED.value() == status) {
            throw new ResponseStatusException(UNAUTHORIZED);
        }
        return status;
    }
}
