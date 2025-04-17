package ru.exyon.telegrambot.services;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.exyon.telegrambot.core.MessageFunction;
import ru.exyon.telegrambot.enums.MessageType;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;

import java.util.List;

public interface MessageService {

    /**
     * Возвращает тип сообщения с которым работает реализованный сервис
     * @return тип сообщения
     */
    MessageType getMessageType();

    /**
     * Обрабатывает входное сообщение, алогритм зависит от типа сообщения
     * @param  message полученное сообщение
     * @param  dialog текущий активный диалог
     * @param  buildAndSendMessageFunction функциональный интерфейс формирования и отправки сообщения
     * @return отправленное сообщение, пустое если не удалось отправить сообщение или не найден нужный шаг
     */
    SendMessage processMessage(Message message, Dialog dialog,
                               MessageFunction<Long, String, List<InlineKeyboardButton>, SendMessage> buildAndSendMessageFunction);
}
