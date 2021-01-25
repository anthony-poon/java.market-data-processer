package model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MarketData {
    private String symbol;
    private long bid;
    private long ask;
    private long last;
    private LocalDateTime updateTime;

    public synchronized void aggregate(MarketData data) {
        this.bid = data.getBid();
        this.ask = data.getAsk();
        this.last = data.getLast();
        this.updateTime = data.getUpdateTime();
    }
}
