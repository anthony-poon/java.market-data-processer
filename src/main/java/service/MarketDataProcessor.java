package service;

import model.MarketData;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;
import java.util.logging.Logger;

class DataAggregator {
    private final Lock lock = new ReentrantLock();
    private final Condition isEmpty = lock.newCondition();
    private final Logger logger = Logger.getLogger(DataAggregator.class.getName());
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicInteger queueSize = new AtomicInteger(0);
    private final RateLimiter<Void> rateLimiter = new RateLimiter<>(1, 1000);
    private final BlockingQueue<Future<Void>> queue = new LinkedBlockingQueue<>();
    public void update(MarketData data) throws Exception {
        queueSize.incrementAndGet();
        rateLimiter.queue(() -> {
            count.incrementAndGet();
            Thread.sleep(500);
            if (queueSize.decrementAndGet() == 0){
                try {
                    lock.tryLock();
                    isEmpty.signal();
                } finally {
                    lock.unlock();
                }
            }
            return null;
        });
    }

    public int getCount() throws InterruptedException {
        lock.lock();
        try {
            while (queueSize.get() != 0) {
                isEmpty.await();
            }
        } finally {
            lock.unlock();
        }
        return count.get();
    }
}

public class MarketDataProcessor {
    private final Logger logger = Logger.getLogger(MarketDataProcessor.class.getName());
    private final Map<String, DataAggregator> db = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final RateLimiter<Integer> rateLimiter = new RateLimiter<>(100, 1000);

    public void onMessage(MarketData data) {
        executor.submit(() -> {
            DataAggregator aggregator = db.computeIfAbsent(data.getSymbol(), k -> new DataAggregator());
            aggregator.update(data);
            return null;
        });
    }

    public int publishAggregatedMarketData(MarketData data) throws Exception {
        return rateLimiter.queue(() -> {
            DataAggregator aggregator = db.computeIfAbsent(data.getSymbol(), k -> new DataAggregator());
            return aggregator.getCount();
        }).get();
    }

    public void await() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}
