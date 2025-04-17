package ru.exyon.telegrambot.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.exyon.telegrambot.config.BotConfig;
import ru.exyon.telegrambot.core.MessageButtons;
import ru.exyon.telegrambot.core.MessageFunction;
import ru.exyon.telegrambot.enums.MessageType;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;
import ru.exyon.telegrambot.models.Step;

import java.util.List;

@Service
@Slf4j
public class ValidateMessageService implements MessageButtons {
    private final BotConfig botConfig;

    public ValidateMessageService(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    public boolean isValid(Message message, Dialog dialog, MessageFunction<Long, String,
            List<InlineKeyboardButton>, SendMessage> buildAndSendMessageFunction) {

        Step lastStep = dialog.getStep();

        //Сначала частные условия
        //Если вручную ввели некорректный следующий шаг
        if (!lastStep.getNext().equals(message.getText())) {
            //Если были на необязательном шаге, предлагаем вернуться в начало
            //если в next есть знак # - необязательный переход на следующий шаг
            if (lastStep.getNext().contains("#")) {
                log.warn("Username: {}. Entered not required step not equals next step in db: {}", dialog.getUsername(), lastStep.getNext());
                buildAndSendMessageFunction.apply(message.getChatId(), "Пожалуйста, следуйте рекомендациям на прошлом шаге или начните диалог сначала",
                        List.of(getStartButton(botConfig.getStartStep())));
                return false;
            } else if (message.getType() == MessageType.COMMAND) { //для обязательного шага не предлагаем возврат в начало
                log.warn("Username: {}. Entered required step not equals next step in db: {}", dialog.getUsername(), lastStep.getNext());
                buildAndSendMessageFunction.apply(message.getChatId(), "Пожалуйста, следуйте рекомендациям на прошлом шаге", List.of());
                return false;
            }
        }

        //команды не должны использоваться, когда ожидается сохранение на прошлом шаге (isNeedToSave=true),
        //а при ввод данных, на прошлом шаге должен быть флаг isNeedToSave=true
        if (message.getType() == MessageType.COMMAND && lastStep.isNeedToSave()
            || message.getType() == MessageType.DATA && !lastStep.isNeedToSave()) {
            buildAndSendMessageFunction.apply(message.getChatId(), "Пожалуйста, следуйте рекомендациям на прошлом шаге", List.of());
            return false;
        }

        //обязательно должен быть заполнен next шаг
        if (lastStep.getNext().isBlank()) {
            log.error("Next step cant be blank: {}", lastStep);
            buildAndSendMessageFunction.apply(message.getChatId(), "Не удалось определить следующий шаг", List.of());
            return false;
        }

        //вводные данные не подходят под регулярное выражение
        if (message.getType() == MessageType.DATA && !message.getText().matches(lastStep.getRegexp())) {
            buildAndSendMessageFunction.apply(message.getChatId(), "Проверьте введенные данные и введите повторно", List.of());
            return false;
        }
        return true;
    }


}
