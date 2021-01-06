package io.github.nivance.retry.example.service;


import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.stats.StatisticsRepository;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RetryStatisticService {

    @Autowired
    private StatisticsRepository statisticsRepository;
    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    public void init() {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            statisticsRepository.findAll().forEach(s -> {
                log.info(s.toString());
            });
        }, 1, 30, TimeUnit.SECONDS);
    }

}
