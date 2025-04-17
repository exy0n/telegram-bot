package ru.exyon.telegrambot.unittests.models;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.*;
import ru.exyon.telegrambot.enums.MessageType;
import ru.exyon.telegrambot.models.Message;

public class MessageTest {

    //Positive
    @Test
    public void Message_commandMessageFromCallbackData() {
        Long chatId = 1L;
        String data = "/someText";
        String firstname = "firstname";
        String username = "username";
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(data);
        User user = new User();
        user.setUserName(username);
        user.setFirstName(firstname);
        callbackQuery.setFrom(user);
        update.setCallbackQuery(callbackQuery);
        InaccessibleMessage inaccessibleMessage = new InaccessibleMessage();
        Chat chat = new Chat();
        chat.setId(chatId);
        inaccessibleMessage.setChat(chat);
        callbackQuery.setMessage(inaccessibleMessage);

        Message messageActual = new Message(update);

        Message messageExpected = new Message()
                .setChatId(chatId)
                .setText(data)
                .setType(MessageType.COMMAND)
                .setFirstname(firstname)
                .setUsername(username);
        Assertions.assertThat(messageActual).isEqualTo(messageExpected);
    }

    @Test
    public void Message_dataMessageFromUser() {
        Long chatId = 1L;
        String data = "89197212233";
        String firstname = "firstname";
        String username = "username";
        org.telegram.telegrambots.meta.api.objects.Message telegramMessage = new org.telegram.telegrambots.meta.api.objects.Message();
        telegramMessage.setText(data);
        User user = new User();
        user.setUserName(username);
        user.setFirstName(firstname);
        telegramMessage.setFrom(user);
        Chat chat = new Chat();
        chat.setId(chatId);
        telegramMessage.setChat(chat);
        Update update = new Update();
        update.setMessage(telegramMessage);

        Message messageActual = new Message(update);

        Message messageExpected = new Message()
                .setChatId(chatId)
                .setText(data)
                .setType(MessageType.DATA)
                .setFirstname(firstname)
                .setUsername(username);
        Assertions.assertThat(messageActual).isEqualTo(messageExpected);
    }

    //Negative
    @Test
    public void Message_wrongUpdateNullMessage() {
        Message messageActual = new Message(new Update());

        Message messageExpected = new Message()
                .setChatId(0L)
                .setText("/initError")
                .setType(MessageType.COMMAND)
                .setFirstname("")
                .setUsername("");
        Assertions.assertThat(messageActual).isEqualTo(messageExpected);
    }

    @Test
    public void Message_wrongUpdateEmptyMessage() {
        Update update = new Update();
        org.telegram.telegrambots.meta.api.objects.Message telegramMessage = new org.telegram.telegrambots.meta.api.objects.Message();
        telegramMessage.setText("");
        update.setMessage(telegramMessage);
        Message messageActual = new Message(update);

        Message messageExpected = new Message()
                .setChatId(0L)
                .setText("/initError")
                .setType(MessageType.COMMAND)
                .setFirstname("")
                .setUsername("");
        Assertions.assertThat(messageActual).isEqualTo(messageExpected);
    }

}
