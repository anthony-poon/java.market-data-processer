package service;

import model.MarketData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataAggregatorTest {
    @Test
    public void testWillAwaitQueueClear() throws Exception {
        DataAggregator aggregator = new DataAggregator();
        aggregator.update(new MarketData());
        List<Callable<Void>> callables = Arrays.asList(
                () -> {
                    aggregator.update(new MarketData());
                    return null;
                },
                () -> {
                    aggregator.update(new MarketData());
                    return null;
                },
                () -> {
                    int count = aggregator.getCount();
                    Assertions.assertEquals(count, 5);
                    return null;
                },
                () -> {
                    aggregator.update(new MarketData());
                    return null;
                },
                () -> {
                    aggregator.update(new MarketData());
                    return null;
                }
        );
        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.invokeAll(callables);
    }
}
