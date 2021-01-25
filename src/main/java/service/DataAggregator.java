package service;

import model.MarketData;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

class DataAggregator {
    private final Lock lock = new ReentrantLock();
    private final Condition isEmpty = lock.newCondition();
    private final MarketData data = new MarketData();
    private final Logger logger = Logger.getLogger(DataAggregator.class.getName());
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicInteger queueSize = new AtomicInteger(0);
    private final RateLimiter<Void> rateLimiter = new RateLimiter<>(1, 1000);
    private final BlockingQueue<Future<Void>> queue = new LinkedBlockingQueue<>();

    public void update(MarketData data) throws Exception {
        queueSize.incrementAndGet();
        rateLimiter.queue(() -> {
            synchronized (this) {
                this.data.aggregate(data);
            }
            Thread.sleep(500);
            if (queueSize.decrementAndGet() == 0) {
                try {
                    lock.tryLock();
                    isEmpty.signalAll();
                } finally {
                    lock.unlock();
                }
            }
            return null;
        });
    }

    public MarketData getData() throws InterruptedException {
        lock.lock();
        try {
            while (queueSize.get() != 0) {
                isEmpty.await();
            }
        } finally {
            lock.unlock();
        }
        return this.data;
    }

    public void await() throws InterruptedException {
        rateLimiter.await();
    }
}
