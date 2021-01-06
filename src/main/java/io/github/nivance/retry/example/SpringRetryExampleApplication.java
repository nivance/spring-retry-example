package io.github.nivance.retry.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * @author nivance
 */
@EnableRetry
@SpringBootApplication
public class SpringRetryExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringRetryExampleApplication.class, args);
	}

}
