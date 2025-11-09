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
    private Map<Long,Order> userOrder = new HashMap<>();

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

            Order order = getOrder(chatId);

            if (heroName.equals("skip")){
                acceptOrder(order, chatId, username);
                return;
            }

            Hero found = findHeroByName(heroName);

            if (order.hasHero(heroName)){
               order.removeHero(found);
            }
            else{
                order.addHero(found);
            }

            if (order.hasRequiredCount(config.getHeroesLimit())){
                acceptOrder(order, chatId, username);
                return;
            }

            sendHeroesPage(chatId, 0);
        }
        else if (data.startsWith("pageNav_")){
            String[] parts = data.split("_");
            int page = Integer.parseInt(parts[1]);
            userPage.put(chatId, page);
            sendHeroesPage(chatId, page);
        }
    }

    private void acceptOrder(Order order, long chatId, String username) {
        String orderInfo = order.getInfo();
        sendMessage(chatId, "Вы выбрали: " + orderInfo);
        sendMessageToAdmin("@" + username + " выбрал: " + orderInfo);
        order.clear();
    }

    private void handleTextMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        BotLogger.logMessage(update.getMessage());
        String text = update.getMessage().getText();

        switch (text){
            case "/start":
                BotLogger.info("Sended command /start");
                sendHeroesPage(chatId, 0);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + text);
        }
    }

    private void sendHeroesPage(long chatId, int page){
        Order order = getOrder(chatId);

        SendMessage selector = new SendMessage();
        selector.setChatId(chatId);
        String text = "Выберите персонажа:";
        selector.setText(text);

        int from = page * config.getPageSize();
        int to = Math.min(from + config.getPageSize(), heroes.size());

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        BotLogger.info("Buttons started creating...");
        for (int i = from; i < to; i++){
            Hero hero = heroes.get(i);

            String heroName = hero.getName();
            if (heroName == null || heroName.isBlank()) {
                BotLogger.warn("Hero name is null or empty, skipping");
                continue;
            }

            BotLogger.info(hero.getName());
            List<InlineKeyboardButton> row = new ArrayList<>();
            try {
                InlineKeyboardButton button = new InlineKeyboardButton();

                if (order != null && order.hasHero(heroName)){
                    button.setText(hero.getName() + " ✅");
                    button.setCallbackData("hero_" + hero.getName());
                    BotLogger.info("Order already has " + hero.getName());
                } else {
                    button.setText(hero.getName());
                    button.setCallbackData("hero_" + hero.getName());
                    BotLogger.info(hero.getName() + " button added!");
                }

                row.add(button);
                rows.add(row);
            } catch (Exception e) {
                BotLogger.error("Error while creating button for hero: " + heroName);
                System.out.println(e);
            }
        }
        BotLogger.info("Buttons created and added to rows");

        boolean canSkip = !getOrder(chatId).hasNoHeroes();

        if (canSkip) {
            List<InlineKeyboardButton> skipRow = new ArrayList<>();
            skipRow.add(getHeroSkipButton());
            rows.add(skipRow);
        }

        List<InlineKeyboardButton> navRow = getNavRowButtons(page, to, heroes.size());
        if (!navRow.isEmpty()) {
            rows.add(navRow);
        }


        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);
        selector.setReplyMarkup(markup);


        BotLogger.info("Rows size: " + rows.size());
        for (List<InlineKeyboardButton> r : rows) {
            BotLogger.info("Row: " + r);
        }

        try {
            execute(selector);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private Hero findHeroByName(String heroName){
        Hero found = null;
        for (Hero h : heroes){
            if (h.getName().equals(heroName)){
                found = h;
                break;
            }
        }
        if (found == null){
            BotLogger.warn("Hero not found");
        }// warn
        return found;
    }

    private InlineKeyboardButton getHeroSkipButton(){
        InlineKeyboardButton skipButton = new InlineKeyboardButton();
        skipButton.setText("Мне хватит 👍");
        skipButton.setCallbackData("hero_skip");
        return skipButton;
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

    private Order getOrder(long chatId){
        Order order;
        if (userOrder.containsKey(chatId)) {
            order = userOrder.get(chatId);
        }
        else {
            order = new Order();
            userOrder.put(chatId, order);
        }
        return order;
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }
    public String getBotToken(){
        return config.getBotToken();
    }
}
