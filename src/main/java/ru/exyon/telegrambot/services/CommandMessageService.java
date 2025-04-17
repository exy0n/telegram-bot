package ru.exyon.telegrambot.services;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.exyon.telegrambot.core.MessageButtons;
import ru.exyon.telegrambot.core.MessageFunction;
import ru.exyon.telegrambot.enums.MessageType;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;
import ru.exyon.telegrambot.models.Step;

import java.util.List;
import java.util.Optional;

/**
 * Сервис обработки команд
 */
@Service
@Slf4j
@AllArgsConstructor
public class CommandMessageService implements MessageService, MessageButtons {
    private final StepsService stepsService;
    private final VariablesResolverService variablesResolverService;

    @Override
    public MessageType getMessageType() {
        return MessageType.COMMAND;
    }

    /**
     * Выполняется обработка командного сообщения: <br>
     * 1. Считывается шаг из БД по введеной команде <br>
     * 2. Полученный шаг сохраняется в диалоге в активных диалогах <br>
     * 3. Заменяются переменные в тексте <br>
     * 4. Формируется сообщение для отправки и выполняется отправка <br>
     * @param  message полученное сообщение
     * @param  dialog текущий активный диалог
     * @param  buildAndSendMessageFunction функциональный интерфейс формирования и отправки сообщения
     * @return отправленное сообщение, пустое если не удалось отправить сообщение или не найден нужный шаг
     */
    @Override
    public SendMessage processMessage(Message message, Dialog dialog, MessageFunction<Long, String, List<InlineKeyboardButton>,
            SendMessage> buildAndSendMessageFunction) {
        Optional<Step> stepOptional = stepsService.get(message.getText());
        if (stepOptional.isEmpty()) {
            log.warn("Username: {}. Entered step not found, step: {}", dialog.getUsername(), message.getText());
            buildAndSendMessageFunction.apply(message.getChatId(), "Указанный шаг не найден, повторите ввод", List.of());
            return new SendMessage();
        }
        Step step = stepOptional.get();

        TelegramBotService.getActiveDialogs().put(message.getChatId(), dialog.setStep(step));

        return buildAndSendMessageFunction.apply(message.getChatId(), variablesResolverService.resolveAllVariables(dialog), step.getButtonsList());
    }
}
