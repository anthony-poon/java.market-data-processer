package service;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

class RateLimiter<V>  {
    private final Queue<Long> timestamps = new LinkedList<>();
    private final int maxJobCount;
    private final long timeFrame;
    private final ExecutorService executors = Executors.newFixedThreadPool(5);
    public RateLimiter(int maxJobCount, long timeFrame) {
        this.maxJobCount = maxJobCount;
        this.timeFrame = timeFrame;
    }

    public Future<V> queue(Callable<V> task) throws Exception {
        if (timestamps.size() > 0 && timestamps.size() >= maxJobCount) {
            long timestamp = timestamps.poll();
            long timeDiff = System.currentTimeMillis() - timestamp;
            if (timeDiff < timeFrame) {
                Thread.sleep(timeFrame - timeDiff);
            }
        }
        timestamps.add(System.currentTimeMillis());
        return executors.submit(task);
    }
}
