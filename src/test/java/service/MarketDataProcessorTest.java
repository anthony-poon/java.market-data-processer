package service;

import model.MarketData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Thread.sleep(1800);
        int count = processor.publishAggregatedMarketData(data);
        Assertions.assertEquals(4, count);
    }
}
