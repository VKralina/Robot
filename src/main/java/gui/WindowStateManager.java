package gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import java.beans.PropertyVetoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import log.Logger;

public class WindowStateManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.myapp/config/";
    private static final String CONFIG_FILE = CONFIG_DIR + "window-states.json";
    private Map<String, WindowState> windowStates = new HashMap<>(); //хранение состояний
    private ObjectMapper mapper = new ObjectMapper();

    public WindowStateManager() {
        Path configPath = Paths.get(CONFIG_DIR);
        try {
            if (!Files.exists(configPath)) { //есть ли вообще
                Files.createDirectories(configPath);
            }
        } catch (IOException e) {
            Logger.error("Не удалось создать директорию конфигурации: " + e.getMessage(), e);
        }
    }

    //сохр и загрузка
    public void saveWindowStates(JDesktopPane desktopPane) {
        windowStates.clear(); //очищение
        for (JInternalFrame frame : desktopPane.getAllFrames()) { //внутренние окна
            String uniqueId = getUniqueWindowId(frame);
            windowStates.put(uniqueId, new WindowState(frame));
        }
        saveToFile(); //запись в json
    }

    public void loadWindowStates(JDesktopPane desktopPane) throws IOException {
        loadFromFile(); //загрузка данных
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            String uniqueId = getUniqueWindowId(frame);
            WindowState state = windowStates.get(uniqueId);
            if (state != null) {
                state.applyTo(frame);
            }
        }
    }

    //формирование Id для windowStates
    private String getUniqueWindowId(JInternalFrame frame) {
        String className = frame.getClass().getSimpleName();
        String title = frame.getTitle().replaceAll("\\s+", "_");
        return className + "_" + title;
    }

    //сохранение в файл
    private void saveToFile() {
        try {
            Path configPath = Paths.get(CONFIG_DIR);
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath);
            }
            String json = mapper.writeValueAsString(windowStates);
            Files.write(Paths.get(CONFIG_FILE), json.getBytes());
        } catch (IOException e) {
            Logger.error("Ошибка сохранения: " + e.getMessage(), e);
        }
    }

    //выгрузка из файла
    private void loadFromFile() throws IOException {
        try {
            Path filePath = Paths.get(CONFIG_FILE);
            if (Files.exists(filePath)) {
                byte[] bytes = Files.readAllBytes(filePath);
                windowStates = mapper.readValue(bytes,
                        mapper.getTypeFactory().constructMapType(HashMap.class, String.class, WindowState.class));
            }
        }
        catch (IOException e) {
            Logger.error("Ошибка чтения файла: " + e.getMessage(), e);
        }
    }

    private static class WindowState {
        private int x, y, width, height;
        private boolean iconified;

        public WindowState() {}

        public WindowState(JInternalFrame frame) {
            x = frame.getX();
            y = frame.getY();
            width = frame.getWidth();
            height = frame.getHeight();
            iconified = frame.isIcon();
        }

        public int getX() { return x; }
        public void setX(int x) { this.x = x; }

        public int getY() { return y; }
        public void setY(int y) { this.y = y; }

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }

        public boolean isIconified() { return iconified; }
        public void setIconified(boolean iconified) { this.iconified = iconified; }

        public void applyTo(JInternalFrame frame) {
            frame.setBounds(x, y, width, height);
            try {
                if (iconified) {
                    frame.setIcon(true);
                } else {
                    frame.setIcon(false);
                }
            } catch (PropertyVetoException e) {
                Logger.error("Не удалось установить состояние окна: " + e.getMessage(), e);
            }
        }
    }
}
