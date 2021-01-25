package service;

import model.MarketData;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

class DataAggregator {
    private final Lock lock = new ReentrantLock();
    private int count;
    private final RateLimiter<Void> rateLimiter = new RateLimiter<>(1, 1000);
    public void update(MarketData data) throws Exception {
        try {
            lock.lock();
            rateLimiter.queue(() -> {
                synchronized (this) {
                    count++;
                }
                return null;
            });
        } finally {
            lock.unlock();
        }
    }

    public int getCount() {
        try{
            lock.lock();
            return count;
        } finally {
            lock.unlock();
        }
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
        });
    }

    public void await() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}
