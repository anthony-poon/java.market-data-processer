package service;

import model.MarketData;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.logging.Logger;

class DataAggregator {
    private final Logger logger = Logger.getLogger(DataAggregator.class.getName());
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock = lock.readLock();
    private int count;
    private final RateLimiter<Void> rateLimiter = new RateLimiter<>(1, 1000);
    public void update(MarketData data) throws Exception {
        rateLimiter.queue(() -> {
            try {
                writeLock.lock();
                count++;
                Thread.sleep(500);
            } finally {
                writeLock.unlock();
            }
            return null;
        });
    }

    public int getCount() {
        try{
            readLock.lock();
            return count;
        } finally {
            readLock.unlock();
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
        }).get();
    }

    public void await() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}
