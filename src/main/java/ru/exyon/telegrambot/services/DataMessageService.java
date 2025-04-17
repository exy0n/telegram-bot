package ru.exyon.telegrambot.services;


import lombok.AllArgsConstructor;
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
import java.util.Optional;

/**
 * Сервис обработки вводимых данных
 */
@Service
@Slf4j
@AllArgsConstructor
public class DataMessageService implements MessageService, MessageButtons {
    private final BotConfig botConfig;
    private final StepsService stepsService;
    private final DataService dataService;
    private final VariablesResolverService variablesResolverService;

    @Override
    public MessageType getMessageType() {
        return MessageType.DATA;
    }

    /**
     * Выполняется обработка сообщения c вводными данными: <br>
     * 1. Сохраняются введенные данные в БД <br>
     * 2. Считывается следующий(!) шаг из БД на основе заполненного поля next в текущем шаге активного диалога <br>
     * 3. Полученный шаг сохраняется в диалоге в активных диалогах <br>
     * 4. Заменяются переменные в тексте <br>
     * 5. Формируется сообщение для отправки и выполняется отправка <br>
     * @param  message полученное сообщение
     * @param  dialog текущий активный диалог
     * @param  buildAndSendMessageFunction функциональный интерфейс формирования и отправки сообщения
     * @return отправленное сообщение, пустое если не удалось отправить сообщение или не найден нужный шаг
     */
    @Override
    public SendMessage processMessage(Message message, Dialog dialog, MessageFunction<Long, String, List<InlineKeyboardButton>,
            SendMessage> buildAndSendMessageFunction) {
        dataService.save(message, dialog);
        //После сохранения, переходим к следующему шагу
        Optional<Step> nextStepOptional = stepsService.get(dialog.getStep().getNext());
        if (nextStepOptional.isEmpty()) {
            log.error("Username: {}. Next step not found, next step: {}", dialog.getUsername(), message.getText());
            buildAndSendMessageFunction.apply(message.getChatId(), "Следующий шаг не найден", List.of(getStartButton(botConfig.getStartStep())));
            return new SendMessage();
        }
        Step nextStep = nextStepOptional.get();

        TelegramBotService.getActiveDialogs().put(message.getChatId(), dialog.setStep(nextStep));

        return buildAndSendMessageFunction.apply(message.getChatId(), variablesResolverService.resolveAllVariables(dialog), nextStep.getButtonsList());
    }

}
