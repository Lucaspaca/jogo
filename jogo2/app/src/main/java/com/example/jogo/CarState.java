package com.example.jogo;

public class CarState {
    private String name;
    private double x;
    private double y;
    private double speed;
    private int sensorRange;
    private double d;
    private int carWidth;
    private int carHeight;
    private int penalty;
    private boolean isSafetyCar;

    public CarState(String name, double x, double y, double speed, int penalty) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.penalty = penalty;
        this.isSafetyCar = isSafetyCar;
        // Adicionar outros parâmetros conforme necessário
    }

    // Getters e setters
    public String getName() {
        return this.name;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getSpeed() {
        return speed;
    }

    public int getSensorRange() {
        return sensorRange;
    }

    public double getD() {
        return d;
    }

    public int getCarWidth() {
        return carWidth;
    }

    public int getCarHeight() {
        return carHeight;
    }

    public int getPenalty() {
        return penalty;
    }

    public boolean isSafetyCar() {
        return isSafetyCar;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setSensorRange(int sensorRange) {
        this.sensorRange = sensorRange;
    }

    public void setD(double d) {
        this.d = d;
    }

    public void setCarWidth(int carWidth) {
        this.carWidth = carWidth;
    }

    public void setCarHeight(int carHeight) {
        this.carHeight = carHeight;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public void setSafetyCar(boolean safetyCar) {
        isSafetyCar = safetyCar;
    }
}
