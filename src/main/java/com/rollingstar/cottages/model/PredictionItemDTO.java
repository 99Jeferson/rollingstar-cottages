package com.rollingstar.cottages.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PredictionItemDTO {
    private LocalDate date;
    private BigDecimal predictedTotalProfit;
    private Integer predictedGuestDemand;
    private Integer predictedStockNeeded;

    public PredictionItemDTO() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getPredictedTotalProfit() { return predictedTotalProfit; }
    public void setPredictedTotalProfit(BigDecimal predictedTotalProfit) { this.predictedTotalProfit = predictedTotalProfit; }

    public Integer getPredictedGuestDemand() { return predictedGuestDemand; }
    public void setPredictedGuestDemand(Integer predictedGuestDemand) { this.predictedGuestDemand = predictedGuestDemand; }

    public Integer getPredictedStockNeeded() { return predictedStockNeeded; }
    public void setPredictedStockNeeded(Integer predictedStockNeeded) { this.predictedStockNeeded = predictedStockNeeded; }
}