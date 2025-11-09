import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfig {
    private final Properties props = new Properties();
    private final int PAGE_SIZE = 6;
    private final int HEROES_LIMIT = 5;

    public BotConfig(){
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {

            if (input == null){
                throw new RuntimeException("application.properties не найден!");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final int getHeroesLimit(){
        return HEROES_LIMIT;
    }
    public final String getBotToken(){
        return props.getProperty("bot.token");
    }
    public final String getBotUsername(){
        return props.getProperty("bot.username");
    }
    public final String getSmatChatId(){
        return props.getProperty("smat.chatId");
    }
    public final String getRuslanChatId(){
        return props.getProperty("ruslan.chatId");
    }
    public String getAdminChatId() { return getSmatChatId(); }
    public final int getPageSize(){
        return PAGE_SIZE;
    }
}
