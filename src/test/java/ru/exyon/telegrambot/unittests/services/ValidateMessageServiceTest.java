package ru.exyon.telegrambot.unittests.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.exyon.telegrambot.config.BotConfig;
import ru.exyon.telegrambot.enums.MessageType;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;
import ru.exyon.telegrambot.models.Step;
import ru.exyon.telegrambot.services.ValidateMessageService;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ValidateMessageServiceTest {
    @Mock
    private BotConfig botConfig;

    @InjectMocks
    private ValidateMessageService validateMessageService;

    //Positive
    @Test
    public void isValid_commandMessagePassAllValidations() {
        Message message = new Message()
                .setChatId(1L)
                .setText("/2")
                .setType(MessageType.COMMAND);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Привет!")
                .setNext("/2")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isTrue();
    }

    @Test
    public void isValid_dataMessagePassAllValidations() {
        Message message = new Message()
                .setChatId(1L)
                .setText("89197212233")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Введите телефон:")
                .setNext("/2")
                .setNeedToSave(true)
                .setRegexp(".{10,}");
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isTrue();
    }

    //Negative
    @Test
    public void isValid_commandMessageReceivedWhileIsNeedToSaveTrue() {
        Message message = new Message()
                .setChatId(1L)
                .setText("/2")
                .setType(MessageType.COMMAND);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Введите телефон:")
                .setNext("/2")
                .setNeedToSave(true);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    public void isValid_dataMessageReceivedWhileIsNeedToSaveFalse() {
        Message message = new Message()
                .setChatId(1L)
                .setText("89197212233")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Информация о магазине:")
                .setNext("/2")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    public void isValid_commandMessageWhileEmptyNextStep() {
        Message message = new Message()
                .setChatId(1L)
                .setText("/2")
                .setType(MessageType.COMMAND);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Привет!")
                .setNext("")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    public void isValid_dataMessageWhileEmptyNextStep() {
        Message message = new Message()
                .setChatId(1L)
                .setText("asdasd")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Введите текст")
                .setNext("")
                .setNeedToSave(true);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    public void isValid_commandMessageWhileBlankNextStep() {
        Message message = new Message()
                .setChatId(1L)
                .setText("/2")
                .setType(MessageType.COMMAND);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Привет!")
                .setNext("   ")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    public void isValid_dataMessageWhileBlankNextStep() {
        Message message = new Message()
                .setChatId(1L)
                .setText("asdasd")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Введите текст")
                .setNext("   ")
                .setNeedToSave(true);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    public void isValid_wrongStepIdInNotRequiredStep() {
        Message message = new Message()
                .setChatId(1L)
                .setText("/3")
                .setType(MessageType.COMMAND);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Информация о магазине:")
                .setNext("/2#")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    public void isValid_commandMessageWrongStepIdInRequiredStep() {
        Message message = new Message()
                .setChatId(1L)
                .setText("/3")
                .setType(MessageType.COMMAND);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Информация о магазине:")
                .setNext("/2")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    public void isValid_dataMessageWrongStepIdInRequiredStep() {
        Message message = new Message()
                .setChatId(1L)
                .setText("asdasd")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Информация о магазине:")
                .setNext("/2")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    public void isValid_enteredDataNotMatchRegExp() {
        Message message = new Message()
                .setChatId(1L)
                .setText("8919721")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/1")
                .setText("Введите телефон:")
                .setNext("/2")
                .setNeedToSave(true)
                .setRegexp(".{10,}");
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);

        boolean isValid = validateMessageService.isValid(message, dialog, (a, b, c) -> new SendMessage());

        Assertions.assertThat(isValid).isFalse();
    }
}
