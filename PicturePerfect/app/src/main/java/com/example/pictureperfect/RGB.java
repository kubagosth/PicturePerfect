package com.example.pictureperfect;

public class RGB {

    private int red;
    private int green;
    private int blue;
    private int count;

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public void countUp() {
        this.count++;
    }

    public int getCount() {
        return count;
    }

    public RGB(int red, int green, int blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.count = 0;
    }
}
