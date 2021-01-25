import model.MarketData;
import service.MarketDataProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class MarketDataApplication {
    private static final Logger logger = Logger.getLogger(MarketDataProcessor.class.getName());
    private static final int DATA_COUNT = 20;
    private static final int STOCK_COUNT = 10;
    public static void main(String[] args) throws Exception {
        MarketDataProcessor processor = new MarketDataProcessor();
        List<MarketData> dataList = new ArrayList<>();
        for (int i = 0; i < DATA_COUNT; i++) {
            int index = i % STOCK_COUNT;
            MarketData data = new MarketData();
            data.setSymbol(String.format("STOCK-%d", index));
            dataList.add(data);
        }
        for (MarketData data : dataList) {
            processor.onMessage(data);
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            processor.publishAggregatedMarketData(dataList.get(0));
            long now = System.currentTimeMillis();
            if (i % 100 == 0) {
                logger.info("Call #" + i + " => Elapsed: " + (now - start) + "ms");
            }
        }
        processor.await();
    }
}
