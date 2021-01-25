package service;

import model.MarketData;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;
import java.util.logging.Logger;

public class MarketDataProcessor {
    private final Logger logger = Logger.getLogger(MarketDataProcessor.class.getName());
    private final Map<String, DataAggregator> db = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final RateLimiter<MarketData> rateLimiter = new RateLimiter<>(100, 1000);

    public void onMessage(MarketData data) {
        executor.submit(() -> {
            DataAggregator aggregator = db.computeIfAbsent(data.getSymbol(), k -> new DataAggregator());
            aggregator.update(data);
            return null;
        });
    }

    public MarketData onPublish(String symbol) throws Exception {
        return rateLimiter.queue(() -> {
            DataAggregator aggregator = db.computeIfAbsent(symbol, k -> new DataAggregator());
            return aggregator.getData();
        }).get();
    }

    public void publishAggregatedMarketData(MarketData data) {
    }

    public void await() throws InterruptedException {
        for (DataAggregator aggregator: db.values()) {
            aggregator.await();
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        rateLimiter.await();
    }
}
