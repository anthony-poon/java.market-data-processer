package service;

import model.MarketData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MarketDataProcessorTest {
    @Test
    public void testPublishData() throws Exception {
        MarketDataProcessor processor = new MarketDataProcessor();
        MarketData data = new MarketData();
        data.setSymbol("ABC");
        processor.onMessage(data);
        processor.onMessage(data);
        processor.onMessage(data);
        processor.onMessage(data);
        int count = processor.publishAggregatedMarketData(data);
        Assertions.assertEquals(4, count);
    }

    @Test
    public void testCanPublishAgainAfterUpdate() throws Exception {
        MarketDataProcessor processor = new MarketDataProcessor();
        MarketData data = new MarketData();
        data.setSymbol("ABC");
        processor.onMessage(data);
        processor.onMessage(data);
        processor.onMessage(data);
        processor.onMessage(data);
        int count = processor.publishAggregatedMarketData(data);
        Assertions.assertEquals(4, count);
        count = processor.publishAggregatedMarketData(data);
        Assertions.assertEquals(4, count);
        processor.onMessage(data);
        processor.onMessage(data);
        count = processor.publishAggregatedMarketData(data);
        Assertions.assertEquals(6, count);
    }

    @Test
    public void testCanPublishNothing() throws Exception {
        MarketDataProcessor processor = new MarketDataProcessor();
        MarketData data = new MarketData();
        data.setSymbol("ABC");
        Assertions.assertEquals(0, processor.publishAggregatedMarketData(data));
    }
}
