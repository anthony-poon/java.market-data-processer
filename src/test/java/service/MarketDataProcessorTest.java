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
        data.setAsk(10);
        processor.onMessage(data);
        data.setAsk(20);
        processor.onMessage(data);
        data.setAsk(30);
        processor.onMessage(data);
        data.setAsk(40);
        processor.onMessage(data);
        MarketData aggregated = processor.onPublish("ABC");
        Assertions.assertEquals(40, aggregated.getAsk());
    }

    @Test
    public void testCanPublishAgainAfterUpdate() throws Exception {
        MarketDataProcessor processor = new MarketDataProcessor();
        MarketData data = new MarketData();
        data.setSymbol("ABC");
        data.setAsk(10);
        processor.onMessage(data);
        data.setAsk(20);
        processor.onMessage(data);
        data.setAsk(30);
        processor.onMessage(data);
        data.setAsk(40);
        processor.onMessage(data);

        MarketData assert1 = processor.onPublish("ABC");
        Assertions.assertEquals(40, assert1.getAsk());
    }

//    @Test
//    public void testCanPublishNothing() throws Exception {
//        MarketDataProcessor processor = new MarketDataProcessor();
//        MarketData data = new MarketData();
//        data.setSymbol("ABC");
//        Assertions.assertEquals(0, processor.publishAggregatedMarketData(data));
//    }
}
