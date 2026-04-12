package gui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import log.Logger;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

public class MenuBarFactory {
    private final MainApplicationFrame parentFrame;

    public MenuBarFactory(MainApplicationFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createExitMenu());

        return menuBar;
    }

    private JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("Режим отображения");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription("Управление режимом отображения приложения");

        menu.add(createMenuItem("Системная схема", KeyEvent.VK_S, e ->
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName())));
        menu.add(createMenuItem("Универсальная схема", KeyEvent.VK_U, e ->
                setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())));

        return menu;
    }

    private JMenu createTestMenu() {
        JMenu menu = new JMenu("Тесты");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Тестовые команды");

        menu.add(createMenuItem("Сообщение в лог", KeyEvent.VK_L, e ->
                Logger.debug("Новая строка")));

        return menu;
    }

    private JMenuItem createExitMenu() {
        JMenuItem menu = new JMenuItem("Выход");
        menu.setMnemonic(KeyEvent.VK_X);
        menu.getAccessibleContext().setAccessibleDescription("Завершить работу приложения");
        menu.addActionListener(event -> {
            WindowEvent closingEvent = new WindowEvent(parentFrame, WindowEvent.WINDOW_CLOSING);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
        });
        return menu;
    }

    private JMenuItem createMenuItem(String text, int mnemonic, ActionListener listener) {
        JMenuItem item = new JMenuItem(text, mnemonic);
        item.getAccessibleContext().setAccessibleDescription(text);
        item.addActionListener(listener);
        return item;
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(parentFrame);
        } catch (Exception e) {
            Logger.error("Не удалось установить Look and Feel: " + e.getMessage());
        }
    }
}
