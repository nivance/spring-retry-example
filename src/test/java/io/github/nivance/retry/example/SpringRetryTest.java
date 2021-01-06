package io.github.nivance.retry.example;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryState;
import org.springframework.retry.RetryStatistics;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.policy.CircuitBreakerRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.stats.DefaultStatisticsRepository;
import org.springframework.retry.stats.StatisticsListener;
import org.springframework.retry.support.DefaultRetryState;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.nivance.retry.example.service.RetryableService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = SpringRetryTest.class)
@RunWith(SpringRunner.class)
public class SpringRetryTest {

    private void run(RetryTemplate retryTemplate) throws Exception {
        Integer result = retryTemplate.execute(new RetryCallback<Integer, Exception>() {
            int i = 0;

            // 重试操作
            @Override
            public Integer doWithRetry(RetryContext retryContext) throws Exception {
                retryContext.setAttribute("value", i);
                log.info("retry {} times.", retryContext.getRetryCount());
                return len(i++);
            }
        }, new RecoveryCallback<Integer>() { //兜底回调
            @Override
            public Integer recover(RetryContext retryContext) throws Exception {
                log.info("after retry {} times, recovery method called!", retryContext.getRetryCount());
                return Integer.MAX_VALUE;
            }
        });
        log.info("final result: {}", result);
    }

    private int len(int i) throws Exception {
        if (i < 5) throw new Exception(i + " le 5");
        return i;
    }

    /**
     * 重试策略：SimpleRetryPolicy固定重试次数
     *
     * @throws Exception
     */
    @Test
    public void retryFixTimes() throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(3);

        retryTemplate.setRetryPolicy(simpleRetryPolicy);

        run(retryTemplate);
    }

    /**
     * 重试策略：AlwaysRetryPolicy无限重试
     *
     * @throws Exception Exception
     */
    @Test
    public void retryAlwaysTimes() throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new AlwaysRetryPolicy());
        run(retryTemplate);
    }

    /**
     * 重试策略：TimeoutRetryPolicy，重试累计运行时长在设定的timeout范围内则重试，一旦超出则不再重试执行RecoveryCallback
     *
     * @throws Exception Exception
     */
    @Test
    public void retryTimeout() throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();

        TimeoutRetryPolicy timeoutRetryPolicy = new TimeoutRetryPolicy();
        timeoutRetryPolicy.setTimeout(1000);
        retryTemplate.setRetryPolicy(timeoutRetryPolicy);

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(400);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        run(retryTemplate);
    }


    /**
     * 重试策略：根据返回结果值实现重试
     *
     * @throws Exception Exception
     */
    @Test
    public void retryWithResult() throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new AlwaysRetryPolicy() {
            private static final long serialVersionUID = 1213824522266301314L;

            @Override
            public boolean canRetry(RetryContext context) {
                //小于1则重试
                return context.getAttribute("value") == null || Integer.parseInt(context.getAttribute("value").toString()) < 1;
            }
        });
        run(retryTemplate);
    }

    /**
     * 重试策略：启用熔断器重试策略
     */
    @Test
    public void retryCircuitBreakerTest() {
        RetryTemplate retryTemplate = new RetryTemplate();
        CircuitBreakerRetryPolicy retryPolicy =
                new CircuitBreakerRetryPolicy(new SimpleRetryPolicy(4));
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(300);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        retryPolicy.setOpenTimeout(1500);
        retryPolicy.setResetTimeout(2000);
        retryTemplate.setRetryPolicy(retryPolicy);

        long startTime = System.currentTimeMillis();

        IntStream.range(0, 10).forEach(index -> {
            try {
                Thread.sleep(100);
                RetryState state = new DefaultRetryState("circuit", false);
                String result = retryTemplate.execute(new RetryCallback<String, RuntimeException>() {
                    @Override
                    public String doWithRetry(RetryContext context) throws RuntimeException {
                        log.info("retry {} times", context.getRetryCount());
                        if (System.currentTimeMillis() - startTime > 1300 && System.currentTimeMillis() - startTime < 1500) {
                            return "success";
                        }
                        throw new RuntimeException("timeout");
                    }
                }, new RecoveryCallback<String>() {
                    @Override
                    public String recover(RetryContext context) throws Exception {
                        return "default";
                    }
                }, state);
                log.info("result: {}", result);
            } catch (Exception e) {
                log.error("error: {}", e.getMessage());
            }
        });
    }

    /**
     * spring-retry通过RetryListener实现拦截器模式，默认提供了StatisticsListener实现重试操作统计分析数据
     */
    @Test
    public void retryListeners() {
        RetryTemplate template = new RetryTemplate();
        DefaultStatisticsRepository repository = new DefaultStatisticsRepository();
        StatisticsListener listener = new StatisticsListener(repository);
        template.setListeners(new RetryListener[]{listener});

        for (int i = 0; i < 10; i++) {
            String result = template.execute(new RetryCallback<String, RuntimeException>() {
                @Override
                public String doWithRetry(RetryContext context) throws RuntimeException {
                    context.setAttribute(RetryContext.NAME, "method.key");
                    return "ok";
                }
            });
        }
        RetryStatistics statistics = repository.findOne("method.key");
        System.out.println(statistics);
    }


    /*******************采用注解方式实现*************************/
    @Autowired
    private RetryableService exampleService;

    @Test
    public void retryWithAnnotation() throws Exception {
        exampleService.request();
    }
}
