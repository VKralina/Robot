package gui;

import model.RobotModel;

import javax.swing.*;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.awt.Point;

public class CoordinatesWindow extends JInternalFrame {
    private final JLabel coordinatesLabel;
    private final RobotModel robotModel;

    public CoordinatesWindow(RobotModel model) {
        this.robotModel = model;
        setTitle("Координаты робота");
        setSize(200, 100);

        coordinatesLabel = new JLabel("X: 0.0, Y: 0.0");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(coordinatesLabel, BorderLayout.CENTER);
        add(panel);

        robotModel.addPropertyChangeListener("position", this::onPositionChanged);
    }

    private void onPositionChanged(PropertyChangeEvent evt) {
        Point.Double newPos = (Point.Double) evt.getNewValue();
        coordinatesLabel.setText(String.format("X: %.1f, Y: %.1f", newPos.x, newPos.y));
    }
}
