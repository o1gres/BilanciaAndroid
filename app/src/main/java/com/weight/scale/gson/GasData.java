package com.weight.scale.gson;

/**
 * Created by Sergio Cordedda on 03/09/2022
 */
public class GasData {

    Float weight;
    Float percentage;
    Float settedSize;
    String  time;

    public GasData() {
        //Empty constructor
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Float getPercentage() {
        return percentage;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    public Float getSettedSize() {
        return settedSize;
    }

    public void setSettedSize(Float settedSize) {
        this.settedSize = settedSize;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
