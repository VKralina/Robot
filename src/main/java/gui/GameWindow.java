package gui;

import java.awt.BorderLayout;
import model.RobotModel;
import model.TargetModel;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

public class GameWindow extends JInternalFrame
{
    private final GameVisualizer m_visualizer;

    public GameWindow(RobotModel model, TargetModel target)
    {
        super("Игровое поле", true, true, true, true);
        m_visualizer = new GameVisualizer(model,target);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        setSize(400, 400);
    }
}
