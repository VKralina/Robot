package gui;

import model.RobotModel;
import model.TargetModel;

import javax.swing.*;

public class RobotLogic extends JPanel {
    private final RobotModel robotModel;
    private final TargetModel targetModel;

    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.001;

    public RobotLogic(RobotModel robotModel, TargetModel targetModel) {
        this.robotModel = robotModel;
        this.targetModel = targetModel;
    }

    // логика движения
    protected void onModelUpdateEvent()
    {
        double distanceToTarget = distance(targetModel.getX(), targetModel.getY(),
                robotModel.getX(), robotModel.getY());
        if (distanceToTarget < 0.5) {
            return;
        }

        double angleToTarget = angleTo(robotModel.getX(), robotModel.getY(),
                targetModel.getX(), targetModel.getY());

        // насколько надо повернуться
        double diffAngle = asNormalizedRadians(angleToTarget - robotModel.getDirection());

        double velocity = maxVelocity;
        // торможение
        if (distanceToTarget < 50) {
            velocity = maxVelocity * (distanceToTarget / 50);
            if (velocity < 0.01) velocity = 0.01;
        }

        // угловая скорость
        double angularVelocity;
        double turnThreshold = 0.01;

        if (Math.abs(diffAngle) < turnThreshold) {
            angularVelocity = 0; //смотрим в нужную сторону
        } else {
            angularVelocity = (diffAngle > 0) ? maxAngularVelocity : -maxAngularVelocity;
        }

        moveRobot(velocity, angularVelocity, 10);
    }

    private void moveRobot(double velocity, double angularVelocity, double duration)
    {
        velocity = applyLimits(velocity, 0, maxVelocity);
        angularVelocity = applyLimits(angularVelocity, -maxAngularVelocity, maxAngularVelocity);

        double oldX = robotModel.getX();
        double oldY = robotModel.getY();
        double oldDirection = robotModel.getDirection();

        double newX, newY;

        if (Math.abs(angularVelocity) < 1e-10) { //прямолинейно
            newX = oldX + velocity * duration * Math.cos(oldDirection);
            newY = oldY + velocity * duration * Math.sin(oldDirection);
        } else { // по дуге
            newX = oldX + velocity / angularVelocity *
                    (Math.sin(oldDirection + angularVelocity * duration) - Math.sin(oldDirection));
            newY = oldY - velocity / angularVelocity *
                    (Math.cos(oldDirection + angularVelocity * duration) - Math.cos(oldDirection));
        }

        // ограничение координат границами окна
        int w = getWidth();
        int h = getHeight();
        if (w > 0 && h > 0) {
            newX = Math.max(0, Math.min(w, newX));
            newY = Math.max(0, Math.min(h, newY));
        }

        // сохранение состояния в модель
        robotModel.setPosition(newX, newY);
        double newDirection = asNormalizedRadians(robotModel.getDirection() + angularVelocity * duration);
        robotModel.setDirection(newDirection);
    }

    private static double asNormalizedRadians(double angle)
    {
        angle %= 2*Math.PI;
        // диапазон [-PI, PI] для выбора кратчайшего пути
        if (angle > Math.PI)
        {
            angle -= 2*Math.PI;
        }
        else if (angle < -Math.PI)
        {
            angle += 2*Math.PI;
        }
        return angle;
    }

    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    private static double applyLimits(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
