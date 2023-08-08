package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final NotificationTaskService taskService;
    private final TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NotificationTaskService taskService, TelegramBot telegramBot) {
        this.taskService = taskService;
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        logger.debug("method init started");
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        logger.debug("method process started");
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            String text = update.message().text();
            switch (text) {
                case "/start": {
                    taskService.printStartMessage(update.message().chat().id());
                    break;
                }
                case "/info": {
                    taskService.printInfoMessage(update.message().chat().id());
                    break;
                }
                default:
                    taskService.createTask(update.message().chat().id(), text);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}