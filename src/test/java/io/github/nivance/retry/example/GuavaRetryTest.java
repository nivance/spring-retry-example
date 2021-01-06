//package io.github.retry.example;
//
//import java.util.concurrent.Callable;
//
//import org.junit.jupiter.api.Test;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class GuavaRetryTest {
//
//    private Callable<String> callableWithResult() {
//        return new Callable<String>() {
//            int counter = 0;
//
//            public String call() throws Exception {
//                counter++;
//                log.info("do sth : {}", counter);
//                if (counter < 5) {
//                    return "sorry";
//                }
//                return "good";
//            }
//        };
//    }
//
//    private Callable<String> callableWithException() {
//        return new Callable<String>() {
//            int counter = 0;
//
//            public String call() throws Exception {
//                counter++;
//                log.info("do sth : {}", counter);
//                if (counter < 5) {
//                    throw new RuntimeException("sorry");
//                }
//                return "good";
//            }
//        };
//    }
//
//    private <T> T run(Retryer<T> retryer, Callable<T> callable) {
//        try {
//            return retryer.call(callable);
//        } catch (RetryException | ExecutionException e) {
//            log.trace(ExceptionUtils.getFullStackTrace(e));
//            log.warn(e.getMessage());
//        }
//        return null;
//    }
//
//    /**
//     * 根据结果判断是否重试
//     */
//    @Test
//    public void retryWithResult() {
//        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
//                .retryIfResult(result -> !result.contains("good"))
//                .withStopStrategy(StopStrategies.neverStop())
//                .build();
//        run(retryer, callableWithResult());
//    }
//
//    /**
//     * 根据异常判断是否重试
//     */
//    @Test
//    public void retryWithException() {
//        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.neverStop())
//                .build();
//        log.info("result : " + run(retryer, callableWithException()));
//    }
//
//    /**
//     * 设定无限重试
//     */
//    @Test
//    public void retryNeverStop() {
//        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.neverStop())
//                .build();
//        log.info("result : " + run(retryer, callableWithException()));
//    }
//
//    /**
//     * 设定最大的重试次数
//     */
//    @Test
//    public void retryStopAfterAttempt() {
//        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
//                .withWaitStrategy(WaitStrategies.fixedWait(100, TimeUnit.MILLISECONDS))
//                .build();
//        log.info("result : " + run(retryer, callableWithException()));
//    }
//
//    /**
//     * 设定重试等待固定时长策略
//     */
//    @Test
//    public void retryWaitFixStrategy() {
//        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.neverStop())
//                .withWaitStrategy(WaitStrategies.fixedWait(100, TimeUnit.MILLISECONDS))
//                .build();
//        log.info("result : " + run(retryer, callableWithException()));
//    }
//
//    /**
//     * 设定重试等待策略：设定初始等待时长值，并设定固定增长步长，但不设定最大等待时长
//     */
//    @Test
//    public void retryWaitIncreaseStrategy() {
//        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.neverStop())
//                .withWaitStrategy(WaitStrategies.incrementingWait(200, TimeUnit.MILLISECONDS, 100, TimeUnit.MILLISECONDS))
//                .build();
//        log.info("result : " + run(retryer, callableWithException()));
//    }
//
//    /**
//     * 设定重试等待策略：根据multiplier值按照指数级增长等待时长，并设定最大等待时长
//     */
//    @Test
//    public void retryWaitExponentialStrategy() {
//        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.neverStop())
//                .withWaitStrategy(WaitStrategies.exponentialWait(100, 1000, TimeUnit.MILLISECONDS))
//                .build();
//        log.info("result : " + run(retryer, callableWithException()));
//    }
//
//    /**
//     * 设定重试等待策略：根据multiplier值按照斐波那契数列增长等待时长，并设定最大等待时长
//     * 斐波那契数列：1、1、2、3、5、8、13、21、34、……
//     */
//    @Test
//    public void retryWaitFibonacciStrategy() {
//        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.neverStop())
//                .withWaitStrategy(WaitStrategies.fibonacciWait(100, 1000, TimeUnit.MILLISECONDS))
//                .build();
//        log.info("result : " + run(retryer, callableWithException()));
//    }
//
//    /**
//     * 设定重试等待策略：设定组合模式的等待策略
//     */
//    @Test
//    public void retryWaitJoinStrategy() {
//        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.neverStop())
//                .withWaitStrategy(WaitStrategies.join(WaitStrategies.exponentialWait(25, 500, TimeUnit.MILLISECONDS)
//                        , WaitStrategies.fixedWait(50, TimeUnit.MILLISECONDS)))
//                .build();
//        log.info("result : " + run(retryer, callableWithException()));
//    }
//
//    /*******************如果是采用指定的retry策略，则可以如下简化*************************/
//    private <T> T runWithFixRetry(Callable<T> callable) {
//        Retryer<T> retryer = RetryerBuilder.<T>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.neverStop())
//                .build();
//        return run(retryer, callable);
//    }
//
//    @Test
//    public void retryWithFixRetry() {
//        log.info("result : " + runWithFixRetry(new Callable<Map>() {
//            int counter = 0;
//
//            public Map call() throws Exception {
//                counter++;
//                log.info("do sth : {}", counter);
//                if (counter < 5) {
//                    throw new RuntimeException("sorry");
//                }
//                Map<String, String> result = new HashMap<>(1);
//                result.put("1", "a");
//                return result;
//            }
//        }));
//    }
//
//    /*******************通过RetryListener可以添加重试过程的细节处理*************************/
//    private RetryListener myRetryListener() {
//        return new RetryListener() {
//            @Override
//            public <T> void onRetry(Attempt<T> attempt) {
//                // 第几次重试,(注意:第一次重试其实是第一次调用)
//                log.info("[retry]time=" + attempt.getAttemptNumber());
//
//                // 距离第一次重试的延迟
//                log.info(",delay=" + attempt.getDelaySinceFirstAttempt());
//
//                // 重试结果: 是异常终止, 还是正常返回
//                log.info(",hasException=" + attempt.hasException());
//                log.info(",hasResult=" + attempt.hasResult());
//
//                // 是什么原因导致异常
//                if (attempt.hasException()) {
//                    log.info(",causeBy=" + attempt.getExceptionCause().toString());
//                } else {
//                    // 正常返回时的结果
//                    log.info(",result=" + attempt.getResult());
//                }
//
//                // 增加了额外的异常处理代码
//                try {
//                    T result = attempt.get();
//                    log.info(",rude get=" + result);
//                } catch (ExecutionException e) {
//                    log.error("this attempt produce exception." + e.getCause().toString());
//                }
//            }
//        };
//    }
//
//    private RetryListener myRetryListener2() {
//        return new RetryListener() {
//            @Override
//            public <T> void onRetry(Attempt<T> attempt) {
//                log.info("myRetryListener2 : [retry]time=" + attempt.getAttemptNumber());
//            }
//        };
//    }
//
//    private <T> T runWithFixRetryAndListener(Callable<T> callable) {
//        Retryer<T> retryer = RetryerBuilder.<T>newBuilder()
//                .retryIfRuntimeException()
//                .withStopStrategy(StopStrategies.neverStop())
//                .withRetryListener(myRetryListener())
//                .withRetryListener(myRetryListener2())
//                .build();
//        return run(retryer, callable);
//    }
//
//    @Test
//    public void retryWithRetryListener() {
//        log.info("result : " + runWithFixRetryAndListener(callableWithException()));
//    }
//}
