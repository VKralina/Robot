package model;

import java.awt.*;

public class TargetModel {
    private volatile int x = 150;
    private volatile int y = 100;

    public void setTarget(int newX, int newY){
        this.x = newX;
        this.y = newY;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }
}
