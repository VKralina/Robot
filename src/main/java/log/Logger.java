package log;

public final class Logger
{
    private static final LogWindowSource defaultLogSource;
    static {
        defaultLogSource = new LogWindowSource(100);
    }
    
    private Logger()
    {
    }

    public static void debug(String strMessage)
    {
        defaultLogSource.append(LogLevel.Debug, strMessage);
    }

    public static void error(String strMessage) {
        defaultLogSource.append(LogLevel.Error, strMessage);
    }

    public static void info(String strMessage) {
        defaultLogSource.append(LogLevel.Info, strMessage);
    }

    public static void error(String strMessage, Exception e) {
        error(strMessage + " - " + e.getMessage());
    }

    public static LogWindowSource getDefaultLogSource()
    {
        return defaultLogSource;
    }
}
