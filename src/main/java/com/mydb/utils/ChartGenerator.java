package com.mydb.utils;

import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;

import java.util.*;

public class ChartGenerator {
    
    public static VBox generateBarChart(String title, List<String> columnNames, 
                                       ObservableList<ObservableList<String>> data) {
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        
        barChart.setTitle(title);
        xAxis.setLabel(columnNames.get(0));
        yAxis.setLabel(columnNames.size() > 1 ? columnNames.get(1) : "Count");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Data");
        
        for (ObservableList<String> row : data) {
            if (row.size() >= 2) {
                String xValue = row.get(0);
                try {
                    Number yValue = Double.parseDouble(row.get(1));
                    series.getData().add(new XYChart.Data<>(xValue, yValue));
                } catch (NumberFormatException e) {
                    // Skip non-numeric values
                }
            }
        }
        
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        
        VBox container = new VBox(barChart);
        VBox.setVgrow(barChart, javafx.scene.layout.Priority.ALWAYS);
        
        return container;
    }
    
    public static VBox generatePieChart(String title, List<String> columnNames,
                                       ObservableList<ObservableList<String>> data) {
        
        PieChart pieChart = new PieChart();
        pieChart.setTitle(title);
        
        for (ObservableList<String> row : data) {
            if (row.size() >= 2) {
                String label = row.get(0);
                try {
                    double value = Double.parseDouble(row.get(1));
                    pieChart.getData().add(new PieChart.Data(label, value));
                } catch (NumberFormatException e) {
                    // Skip non-numeric values
                }
            }
        }
        
        pieChart.setLegendVisible(true);
        
        VBox container = new VBox(pieChart);
        VBox.setVgrow(pieChart, javafx.scene.layout.Priority.ALWAYS);
        
        return container;
    }
    
    public static VBox generateLineChart(String title, List<String> columnNames,
                                        ObservableList<ObservableList<String>> data) {
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        
        lineChart.setTitle(title);
        xAxis.setLabel(columnNames.get(0));
        yAxis.setLabel(columnNames.size() > 1 ? columnNames.get(1) : "Value");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Trend");
        
        for (ObservableList<String> row : data) {
            if (row.size() >= 2) {
                String xValue = row.get(0);
                try {
                    Number yValue = Double.parseDouble(row.get(1));
                    series.getData().add(new XYChart.Data<>(xValue, yValue));
                } catch (NumberFormatException e) {
                    // Skip non-numeric values
                }
            }
        }
        
        lineChart.getData().add(series);
        lineChart.setCreateSymbols(true);
        
        VBox container = new VBox(lineChart);
        VBox.setVgrow(lineChart, javafx.scene.layout.Priority.ALWAYS);
        
        return container;
    }
    
    public static VBox generateAreaChart(String title, List<String> columnNames,
                                        ObservableList<ObservableList<String>> data) {
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        
        areaChart.setTitle(title);
        xAxis.setLabel(columnNames.get(0));
        yAxis.setLabel(columnNames.size() > 1 ? columnNames.get(1) : "Value");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Area");
        
        for (ObservableList<String> row : data) {
            if (row.size() >= 2) {
                String xValue = row.get(0);
                try {
                    Number yValue = Double.parseDouble(row.get(1));
                    series.getData().add(new XYChart.Data<>(xValue, yValue));
                } catch (NumberFormatException e) {
                    // Skip non-numeric values
                }
            }
        }
        
        areaChart.getData().add(series);
        
        VBox container = new VBox(areaChart);
        VBox.setVgrow(areaChart, javafx.scene.layout.Priority.ALWAYS);
        
        return container;
    }
    
    public static VBox generateScatterChart(String title, List<String> columnNames,
                                           ObservableList<ObservableList<String>> data) {
        
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        
        scatterChart.setTitle(title);
        xAxis.setLabel(columnNames.get(0));
        yAxis.setLabel(columnNames.size() > 1 ? columnNames.get(1) : "Value");
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Points");
        
        for (ObservableList<String> row : data) {
            if (row.size() >= 2) {
                try {
                    Number xValue = Double.parseDouble(row.get(0));
                    Number yValue = Double.parseDouble(row.get(1));
                    series.getData().add(new XYChart.Data<>(xValue, yValue));
                } catch (NumberFormatException e) {
                    // Skip non-numeric values
                }
            }
        }
        
        scatterChart.getData().add(series);
        
        VBox container = new VBox(scatterChart);
        VBox.setVgrow(scatterChart, javafx.scene.layout.Priority.ALWAYS);
        
        return container;
    }
}
