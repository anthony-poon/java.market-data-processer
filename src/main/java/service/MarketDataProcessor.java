package service;

import lombok.Data;
import model.MarketData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

@Data
class DataAggregator {
    private long lastUpdate = 0;
    private int count;
    public synchronized void update(MarketData data) throws InterruptedException {
        long timeDiff = System.currentTimeMillis() - lastUpdate;
        if (timeDiff < 1000) {
            Thread.sleep(1000 - timeDiff);
        }
        count++;
        // Blocking IO
        Thread.sleep(100);
        lastUpdate = System.currentTimeMillis();
    }
}

public class MarketDataProcessor {
    private final Queue<Long> timestamps = new LinkedList<>();
    private final Map<String, DataAggregator> db = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    public void onMessage(MarketData data) {
        executor.submit(() -> {
            info("Updating " + data.getSymbol());
            DataAggregator aggregator = db.computeIfAbsent(data.getSymbol(), k -> new DataAggregator());
            aggregator.update(data);
            return null;
        });
    }

    public synchronized int publishAggregatedMarketData(MarketData data) throws InterruptedException {
        if (timestamps.size() >= 100) {
            long timestamp = timestamps.poll();
            long timeDiff = System.currentTimeMillis() - timestamp;
            if (timeDiff < 1000) {
                info("Delaying publish.");
                Thread.sleep(1000 - timeDiff);
            }
        }
        timestamps.add(System.currentTimeMillis());
        await();
        DataAggregator aggregator = db.get(data.getSymbol());
        return aggregator.getCount();
    }

    private void info(String message) {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String thread = Thread.currentThread().getName();
        System.err.println("["  + df.format(new Date()) + "]" + "[" + thread + "] "+ message);
    }

    public void await() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}
