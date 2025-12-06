package com.mydb.models;

public class Table {
    private String name;
    private int rowCount;
    private String engine;
    
    public Table(String name) {
        this.name = name;
    }
    
    public Table(String name, int rowCount, String engine) {
        this.name = name;
        this.rowCount = rowCount;
        this.engine = engine;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getRowCount() {
        return rowCount;
    }
    
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
    
    public String getEngine() {
        return engine;
    }
    
    public void setEngine(String engine) {
        this.engine = engine;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
    