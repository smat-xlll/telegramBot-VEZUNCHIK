import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    static void main(String[] args) throws Exception{
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(new VezunchikBot());
        BotLogger.info("Bot VEZUNCHIK started!");
    }
}
