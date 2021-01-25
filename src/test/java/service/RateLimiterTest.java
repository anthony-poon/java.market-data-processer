package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class RateLimiterTest {
    @Test
    public void testCanExecute() throws Exception {
        int timeFrame = 1;
        int maxJobCount = 1;
        RateLimiter<Boolean> limiter = new RateLimiter<>(maxJobCount, timeFrame);
        Assertions.assertTrue(limiter.queue(() -> true).get());
    }

    @Test
    public void testCanLimitedCall() throws Exception {
        int timeFrame = 1000;
        int maxJobCount = 10;
        RateLimiter<Void> limiter = new RateLimiter<>(maxJobCount, timeFrame);
        long start = System.currentTimeMillis();
        List<Long> rtn = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < 100; i++) {
            limiter.queue(() -> {
                rtn.add(System.currentTimeMillis() - start);
                return null;
            });
        }
        for (int i = 0; i < rtn.size(); i++) {
            long delay = rtn.get(i);
            Assertions.assertTrue(Math.floorDiv(i, 10) * timeFrame <= delay);
            Assertions.assertTrue(delay <= Math.floorDiv(i, 10) * timeFrame + timeFrame);
        }
    }
}