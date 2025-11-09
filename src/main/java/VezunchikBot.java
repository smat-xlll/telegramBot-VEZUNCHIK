import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VezunchikBot extends TelegramLongPollingBot {
    private final BotConfig config = new BotConfig();
    private List<Hero> heroes = new ArrayList<>(List.of(
            new Hero("Мерилин Монро"),
            new Hero("Дмитрий Нагиев"),
            new Hero("Дмитрий Нагиев2"),
            new Hero("Дмитрий Нагиев3"),
            new Hero("Дмитрий Нагиев4"),
            new Hero("Дмитрий Нагиев5"),
            new Hero("Дмитрий Нагиев6")
    ));
    private Map<Long,Integer> userPage = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            handleTextMessage(update);
        }
        if (update.hasCallbackQuery()){
            handleCallback(update);
        }
    }

    private void handleCallback(Update update) {
        CallbackQuery callback = update.getCallbackQuery();
        long chatId = callback.getMessage().getChatId();
        String data = callback.getData();
        String username = callback.getFrom().getUserName();

        if (data.startsWith("hero_")){
            String[] parts = data.split("_");
            String heroName = parts[1];
            sendMessage(chatId, "Вы выбрали: " + heroName);
            sendMessageToAdmin("@" + username + " выбрал: " + heroName);
        }
        else if (data.startsWith("pageNav_")){
            String[] parts = data.split("_");
            int page = Integer.parseInt(parts[1]);
            userPage.put(chatId, page);
            sendHeroesPage(chatId, page, "Выберите персонажа:");
        }
    }

    private void handleTextMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        BotLogger.logMessage(update.getMessage());
        String text = update.getMessage().getText();

        switch (text){
            case "/start":
                sendHeroesPage(chatId, 0, "Кого хотите видеть на празднике?");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + text);
        }
    }

    private void sendHeroesPage(long chatId, int page, String text){
        SendMessage selector = new SendMessage();
        selector.setChatId(chatId);
        selector.setText(text);

        int from = page * config.getPageSize();
        int to = Math.min(from + config.getPageSize(), heroes.size());

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = from; i < to; i++){
            List<InlineKeyboardButton> row = new ArrayList<>();
            Hero hero = heroes.get(i);
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(hero.getName());
            button.setCallbackData("hero_" + hero.getName());
            row.add(button);
            rows.add(row);
        }


        rows.add(getNavRowButtons(page, to, heroes.size()));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);
        selector.setReplyMarkup(markup);

        try {
            execute(selector);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private List<InlineKeyboardButton> getNavRowButtons(int currentPage, int lastIndexOnPage, int lastIndex){
        List<InlineKeyboardButton> navRow = new ArrayList<>();

        if (currentPage > 0){
            InlineKeyboardButton previous = new InlineKeyboardButton();
            previous.setText("Назад");
            previous.setCallbackData("pageNav_" + (currentPage-1));
            navRow.add(previous);
        }
        if (lastIndexOnPage < lastIndex){
            InlineKeyboardButton next = new InlineKeyboardButton();
            next.setText("Вперед");
            next.setCallbackData("pageNav_" + (currentPage+1));
            navRow.add(next);
        }

        return navRow;
    }

    private void sendMessageToAdmin(String text){
        sendMessage(Long.parseLong(config.getAdminChatId()), text);
    }

    private void sendMessage(long chatId, String text){
        try {
            execute(new SendMessage(String.valueOf(chatId), text));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }
    public String getBotToken(){
        return config.getBotToken();
    }
}
