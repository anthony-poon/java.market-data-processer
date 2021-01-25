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
}
