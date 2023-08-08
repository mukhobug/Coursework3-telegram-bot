package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository taskRepository;
    private final TelegramBot telegramBot;
    private final Pattern taskReg = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}) (.*)");
    private final Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);

    public NotificationTaskService(NotificationTaskRepository taskRepository, TelegramBot telegramBot) {
        this.taskRepository = taskRepository;
        this.telegramBot = telegramBot;
    }

    public void printStartMessage(long chatId) {
        logger.debug("method printStartMessage started");
        sendMessage(chatId, "Hello I'm a reminder bot!\nhow to make a remind notification - /info");
    }

    public void printInfoMessage(long chatId) {
        logger.debug("method printNotifyMessage started");
        sendMessage(chatId, "To create a remind notification you need to write a message in the format \"dd.mm.yyyy hh:mm your remind\"");
    }

    public void createTask(long chatId, String message) {
        logger.debug("method createTask started");
        Matcher matcher = taskReg.matcher(message);
        if (matcher.matches()) {
            LocalDateTime time = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            if (time.isBefore(LocalDateTime.now())) {
                sendMessage(chatId, "Remind notification is expired");
                return;
            }

            NotificationTask task = new NotificationTask();
            task.setDateToSend(time);
            task.setChatId(chatId);
            task.setNotificationText(matcher.group(2));
            taskRepository.save(task);

            sendMessage(chatId, "Remind notification created");
        } else {
            sendMessage(chatId, "Wrong format check /info");
        }
    }

    @Scheduled(fixedRate = 60_000L)
    public void checkTask() {
        logger.debug("method checkTask started");
        List<NotificationTask> tasks = taskRepository.findNearest(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (tasks.isEmpty()) return;
        tasks.forEach(task -> {
            sendMessage(task.getChatId(), "Remind notification:\n" + task.getNotificationText());
            taskRepository.deleteById(task.getId());
        });
    }

    private void sendMessage(long chatId, String message) {
        logger.debug("method sendMessage started");
        SendMessage smg = new SendMessage(chatId, message);
        SendResponse response = telegramBot.execute(smg);
        if (!response.isOk()) {
            logger.error("message was not send: {}", response.errorCode());
        }
    }
}