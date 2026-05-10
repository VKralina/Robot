package gui;

import model.RobotModel;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel
{
    private final Timer m_timer = initTimer();

    private static Timer initTimer()
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    private final RobotModel robotModel;

    private volatile int m_targetPositionX = 150;
    private volatile int m_targetPositionY = 100;

    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.001;

    public GameVisualizer(RobotModel model)
    {
        this.robotModel = model;
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onRedrawEvent();
            }
        }, 0, 50);
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onModelUpdateEvent();
            }
        }, 0, 10);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                setTargetPosition(e.getPoint());
                repaint();
            }
        });
        setDoubleBuffered(true);
    }

    protected void setTargetPosition(Point p)
    {
        m_targetPositionX = p.x;
        m_targetPositionY = p.y;
    }

    protected void onRedrawEvent()
    {
        EventQueue.invokeLater(this::repaint);
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

    // логика движения
    protected void onModelUpdateEvent()
    {
        double distanceToTarget = distance(m_targetPositionX, m_targetPositionY,
                robotModel.getX(), robotModel.getY());
        if (distanceToTarget < 0.5) {
            return;
        }

        double angleToTarget = angleTo(robotModel.getX(), robotModel.getY(),
                m_targetPositionX, m_targetPositionY);

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

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
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

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        drawRobot(g2d, round(robotModel.getX()), round(robotModel.getY()), robotModel.getDirection());
        drawTarget(g2d, m_targetPositionX, m_targetPositionY);
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction)
    {
        AffineTransform t = AffineTransform.getRotateInstance(direction, x, y);
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, x, y, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, x + 10, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x + 10, y, 5, 5);
    }

    private void drawTarget(Graphics2D g, int x, int y)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
    }
    }
