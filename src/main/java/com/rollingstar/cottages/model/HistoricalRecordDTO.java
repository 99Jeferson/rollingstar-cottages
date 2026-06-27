package com.rollingstar.cottages.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HistoricalRecordDTO {
    private LocalDate date;
    private BigDecimal cottageRevenue;
    private BigDecimal barRevenue;
    private Integer totalGuests;
    private Integer stockItemsSold;

    public HistoricalRecordDTO() {}

    public HistoricalRecordDTO(LocalDate date, BigDecimal cottageRevenue, BigDecimal barRevenue, Integer totalGuests, Integer stockItemsSold) {
        this.date = date;
        this.cottageRevenue = cottageRevenue;
        this.barRevenue = barRevenue;
        this.totalGuests = totalGuests;
        this.stockItemsSold = stockItemsSold;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getCottageRevenue() { return cottageRevenue; }
    public void setCottageRevenue(BigDecimal cottageRevenue) { this.cottageRevenue = cottageRevenue; }

    public BigDecimal getBarRevenue() { return barRevenue; }
    public void setBarRevenue(BigDecimal barRevenue) { this.barRevenue = barRevenue; }

    public Integer getTotalGuests() { return totalGuests; }
    public void setTotalGuests(Integer totalGuests) { this.totalGuests = totalGuests; }

    public Integer getStockItemsSold() { return stockItemsSold; }
    public void setStockItemsSold(Integer stockItemsSold) { this.stockItemsSold = stockItemsSold; }
}