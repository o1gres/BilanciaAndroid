package com.weight.scale.gson;

/**
 * Created by Sergio Cordedda on 03/09/2022
 */
public class GasData {

    Integer weight;
    Integer percentage;
    Integer settedSize;
    String  time;

    public GasData() {
        //Empty constructor
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public Integer getSettedSize() {
        return settedSize;
    }

    public void setSettedSize(Integer settedSize) {
        this.settedSize = settedSize;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
