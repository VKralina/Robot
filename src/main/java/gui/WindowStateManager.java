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
        //игнорируем неизвестные поля
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Path configPath = Paths.get(CONFIG_DIR);
        try {
            if (!Files.exists(configPath)) { //есть ли вообще
                Files.createDirectories(configPath);
                Logger.info("Создана директория конфигурации: " + CONFIG_DIR);
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

    public void loadWindowStates(JDesktopPane desktopPane) {
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
        return className;
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
            Logger.error("Ошибка сохранения. Проверьте права доступа и свободное место. " + e.getMessage(), e);
        } catch (Exception e) {
            Logger.error("Неожиданная ошибка при сохранении: " + e.getMessage(), e);
        }
    }

    //выгрузка из файла
    private void loadFromFile() {
        try {
            Path filePath = Paths.get(CONFIG_FILE);
            if (Files.exists(filePath)) {
                byte[] bytes = Files.readAllBytes(filePath);
                windowStates = mapper.readValue(bytes,
                        mapper.getTypeFactory().constructMapType(HashMap.class, String.class, WindowState.class));
                Logger.debug("Загружен файл конфигурации: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            Logger.error("Ошибка чтения файла: " + e.getMessage(), e);
            resetDefaultStates();
        } catch (Exception e) {
            Logger.error("Неожиданная ошибка при загрузке состояний окон: " + e.getMessage(), e);
            resetDefaultStates();
        }
    }

    private void resetDefaultStates() {
        windowStates.clear();
        Logger.info("Состояния окон сброшены к настройкам по умолчанию");
    }


    private static class WindowState {
        private int x, y, width, height;
        private boolean iconified;

        public WindowState() {
        }

        public WindowState(JInternalFrame frame) {
            x = frame.getX();
            y = frame.getY();
            width = frame.getWidth();
            height = frame.getHeight();
            iconified = frame.isIcon();
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public boolean isIconified() {
            return iconified;
        }

        public void setIconified(boolean iconified) {
            this.iconified = iconified;
        }

        public void applyTo(JInternalFrame frame) {
            //значения по умолчанию
            int fX = (x > 0) ? x : 10;
            int fY = (y > 0) ? y : 10;
            int fWidth = (width > 50) ? width : 400;
            int fHeight = (height > 50) ? height : 300;

            try {
                frame.setBounds(fX, fY, fWidth, fHeight);
                if (frame.isIconifiable()) {
                    frame.setIcon(iconified);
                } else {
                    frame.setVisible(true);
                }
            } catch (PropertyVetoException e) {
                Logger.error("Не удалось установить состояние окна: " + e.getMessage(), e);
            }
        }
    }
}
