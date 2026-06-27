package com.rollingstar.cottages.model;

import java.util.List;

public class ForecastResponseDTO {
    private String status;
    private List<PredictionItemDTO> predictions;

    public ForecastResponseDTO() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<PredictionItemDTO> getPredictions() { return predictions; }
    public void setPredictions(List<PredictionItemDTO> predictions) { this.predictions = predictions; }
}