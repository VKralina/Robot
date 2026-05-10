package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.awt.Point;

public class RobotModel {
    private double x = 100;
    private double y = 100;
    private double direction = 0;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void setPosition(double newX, double newY) {
        double oldX = this.x, oldY = this.y;
        this.x = newX;
        this.y = newY;
        pcs.firePropertyChange("position",
                new Point.Double(oldX, oldY),
                new Point.Double(newX, newY)
        );
    }

    public void setDirection(double newDirection) {
        double oldDirection = this.direction;
        this.direction = newDirection;
        pcs.firePropertyChange("direction", oldDirection, newDirection);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getDirection() { return direction; }
}
