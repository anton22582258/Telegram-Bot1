package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.ReminderRepository;


import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private static String REGEX = "([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)|([a-zA-Z])";
    private final Pattern pattern = Pattern.compile(REGEX);
    private final TelegramBot telegramBot;
    private final ReminderRepository reminderRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, ReminderRepository reminderRepository) {
        this.telegramBot = telegramBot;
        this.reminderRepository = reminderRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            try {
                logger.info("Processing update: {}", update);
                if (update.message() == null) {
                    return;
                }
                String text = update.message().text();
                Long chatId = update.message().chat().id();
                Matcher matcher = pattern.matcher(text);
                if (matcher.matches()) {
                    String dateStr = matcher.group(1);
                    LocalDateTime date = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    NotificationTask task = new NotificationTask();
                    task.setMessageText(matcher.group(3));
                    task.setChatId(chatId);
                    task.setLocalDateTime(date);
                    reminderRepository.save(task);
                } else if (text.equals("/start")) {
                    telegramBot.execute(new SendMessage(chatId, "Привет! Я бот. Как я могу помочь вам?"));
                }
            } catch (Exception e) {
                logger.error("Failed to process update {}", update, e);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(fixedDelay = 5000)
    public void timer() {
        reminderRepository.findAllByLocalDateTimeLessThan(LocalDateTime.now()).forEach(
                task -> {
                    telegramBot.execute(new SendMessage(task.getChatId(), task.getMessageText()));
                    reminderRepository.delete(task);
                }
        );
    }
}