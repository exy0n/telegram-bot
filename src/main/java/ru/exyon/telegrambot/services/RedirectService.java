package ru.exyon.telegrambot.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.exyon.telegrambot.config.BotConfig;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;
import ru.exyon.telegrambot.models.Step;

import java.util.Optional;
import java.util.function.Consumer;

@Service
@Slf4j
@AllArgsConstructor
public class RedirectService {
    private final BotConfig botConfig;

    public boolean isRedirectNeeded(Message message, Dialog dialog, Consumer<Message> consumer) {
        return isDialogNotStarted(message, dialog, consumer)
               || isDialogEnded(message, dialog, consumer)
               || isStartCommandReceived(message, dialog, consumer);
    }

    public boolean isDialogNotStarted(Message message, Dialog dialog, Consumer<Message> consumer) {
        if (dialog.getId() == null) {
            TelegramBotService.getActiveDialogs().put(message.getChatId(), new Dialog(new Step().toInitStep(botConfig), message));
            message.toStartCommand(botConfig.getStartStep());
            consumer.accept(message);
            log.info("Username: {}. Redirection: Redirect to {}, reason: dialog not started", message.getUsername(), botConfig.getStartStep());
            return true;
        }
        return false;
    }

    public boolean isDialogEnded(Message message, Dialog dialog, Consumer<Message> consumer) {
        if (dialog.getStep().getNext().equals(botConfig.getEndMarker())) {
            TelegramBotService.getActiveDialogs().remove(message.getChatId());
            message.toStartCommand(botConfig.getStartStep());
            consumer.accept(message);
            log.info("Username: {}. Redirection: Redirect to {}, reason: dialog ended", dialog.getUsername(), botConfig.getStartStep());
            return true;
        }
        return false;
    }

    public boolean isStartCommandReceived(Message message, Dialog dialog, Consumer<Message> consumer) {
        if (message.getText().equals(botConfig.getStartStep()) && !dialog.getStep().getId().equals(botConfig.getInitStep())) {
            TelegramBotService.getActiveDialogs().remove(message.getChatId());
            message.toStartCommand(botConfig.getStartStep());
            consumer.accept(message);
            log.info("Username: {}. Redirection: Redirect to {}, reason: {} command received", dialog.getUsername(),
                    botConfig.getStartStep(), botConfig.getStartStep());
            return true;
        }
        return false;
    }
}
