package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


import log.Logger;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается. 
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 *
 */
public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final WindowStateManager windowStateManager = new WindowStateManager();
    
    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);

        setContentPane(desktopPane);
        
        
        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400,  400);
        addWindow(gameWindow);

        setJMenuBar(new MenuBarFactory(this).generateMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);  //Убрано default поведение при закрытии окна

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit(e);
            }
        });
        loadWindowStates();
    }
    
    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }
    
    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private void handleExit(WindowEvent event) {
        Object[] options = {"Да", "Нет"};

        int result = JOptionPane.showOptionDialog(
                (Component) this,
                "Вы уверены, что хотите выйти из приложения?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == 0) {
            windowStateManager.saveWindowStates(desktopPane);
            System.exit(0);
        }
    }

    //загрузка окн
    private void loadWindowStates() {SwingUtilities.invokeLater(() -> {
        windowStateManager.loadWindowStates(desktopPane);
        });
    }
}