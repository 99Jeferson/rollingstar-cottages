package com.rollingstar.cottages.model;

import java.util.List;

public class ForecastRequestDTO {
    private int forecastDaysRequested;
    private List<HistoricalRecordDTO> historicalRecords;

    public ForecastRequestDTO() {}

    public ForecastRequestDTO(int forecastDaysRequested, List<HistoricalRecordDTO> historicalRecords) {
        this.forecastDaysRequested = forecastDaysRequested;
        this.historicalRecords = historicalRecords;
    }

    public int getForecastDaysRequested() { return forecastDaysRequested; }
    public void setForecastDaysRequested(int forecastDaysRequested) { this.forecastDaysRequested = forecastDaysRequested; }

    public List<HistoricalRecordDTO> getHistoricalRecords() { return historicalRecords; }
    public void setHistoricalRecords(List<HistoricalRecordDTO> historicalRecords) { this.historicalRecords = historicalRecords; }
}