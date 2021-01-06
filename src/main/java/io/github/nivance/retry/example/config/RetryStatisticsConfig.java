package io.github.nivance.retry.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.stats.DefaultStatisticsRepository;
import org.springframework.retry.stats.StatisticsListener;
import org.springframework.retry.stats.StatisticsRepository;

/**
 * @author nivance
 */
@Configuration
public class RetryStatisticsConfig {

    @Bean
    public StatisticsRepository repository() {
        return new DefaultStatisticsRepository();
    }

    @Bean
    public StatisticsListener statisticsListener(StatisticsRepository repository) {
        return new StatisticsListener(repository);
    }

}
