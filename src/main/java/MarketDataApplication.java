import model.MarketData;
import service.MarketDataProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MarketDataApplication {
    private static final int DATA_COUNT = 20;
    private static final int STOCK_COUNT = 10;
    public static void main(String[] args) throws InterruptedException, ExecutionException {
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
        for (int i = 0; i < 1000; i++) {
            processor.publishAggregatedMarketData(dataList.get(0));
        }
        processor.await();
    }
}
