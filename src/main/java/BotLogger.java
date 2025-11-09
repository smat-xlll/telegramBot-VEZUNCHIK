import org.telegram.telegrambots.meta.api.objects.Message;

public class BotLogger {
    public enum Level{
        INFO, DEBUG, WARN, ERROR
    }

    private static void log(Level level, String message){
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        StackTraceElement caller = null;
        for (StackTraceElement element : stack) {
            if (!element.getClassName().equals(BotLogger.class.getName())
                    && !element.getMethodName().equals("getStackTrace")) {
                caller = element;
                break;
            }
        }

        String logMessage = String.format("[%s] [%s.%s.%s] — %s",
                level,
                caller.getClassName(),
                caller.getMethodName(),
                caller.getLineNumber(),
                message
                );

        System.out.println(logMessage);
    }
    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }


    public static void logMessage(Message message){
        if (message == null){
            info("Message is null");
            return;
        }

        String chatId = String.valueOf(message.getChatId());
        String user = message.getFrom() != null ? message.getFrom().getUserName() : "unknown";
        String text = message.hasText() ? message.getText() : "<non-text message>";

        info("Message from @" + user + " (chatId: " + chatId + "): " + text);
    }
}
