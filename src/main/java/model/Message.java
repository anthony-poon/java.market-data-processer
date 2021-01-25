package model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Message {
    private List<MarketData> dataList = new ArrayList<>();
    public void add(MarketData data) {
        dataList.add(data);
    }
}
