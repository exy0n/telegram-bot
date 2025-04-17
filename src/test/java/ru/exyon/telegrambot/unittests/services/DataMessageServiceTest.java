package ru.exyon.telegrambot.unittests.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.exyon.telegrambot.config.BotConfig;
import ru.exyon.telegrambot.enums.MessageType;
import ru.exyon.telegrambot.models.DataInfo;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;
import ru.exyon.telegrambot.models.Step;
import ru.exyon.telegrambot.services.*;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class DataMessageServiceTest {
    @Mock
    private BotConfig botConfig;
    @Mock
    private StepsService stepsService;
    @Mock
    private DataService dataService;
    @Mock
    private VariablesResolverService variablesResolverService;
    @InjectMocks
    private DataMessageService dataMessageService;

    //Positive
    @Test
    public void processMessage_correctInputWithReplace() {
        Message message = new Message()
                .setChatId(1L)
                .setText("asdasdasd")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/2")
                .setText("Введите дату и причину посещения:")
                .setNext("/3")
                .setNeedToSave(true);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);
        Step nextStep = new Step()
                .setId("/3")
                .setText("<<savedData>>") //!!!
                .setNext("/4")
                .setNeedToSave(false);
        DataInfo dataInfo = new DataInfo()
                .setDate("01.02.2025")
                .setReason("просто посмотреть");
        TelegramBotService.getActiveDialogs().clear();
        Mockito.doReturn(null).when(dataService).save(Mockito.any(Message.class), Mockito.any(Dialog.class));
        Mockito.when(stepsService.get(Mockito.anyString())).thenReturn(Optional.of(nextStep));
        Mockito.when(variablesResolverService.resolveAllVariables(Mockito.any(Dialog.class)))
                .thenReturn(dataInfo.toString()); //!!!

        SendMessage sendMessageActual = dataMessageService.processMessage(message, dialog, (a, b, c) -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(a));
            sendMessage.setText(b);
            return sendMessage;
        });

        Mockito.verify(dataService, Mockito.times(1)).save(Mockito.any(Message.class), Mockito.any(Dialog.class));
        Dialog expectedDialog = new Dialog()
                .setId(dialog.getId())
                .setStep(nextStep);
        Assertions.assertThat(TelegramBotService.getActiveDialogs().values().stream().findFirst().get())
                .isEqualTo(expectedDialog);

        SendMessage sendMessageExpected = new SendMessage();
        sendMessageExpected.setChatId(message.getChatId());
        sendMessageExpected.setText(dataInfo.toString());
        Assertions.assertThat(sendMessageActual).isEqualTo(sendMessageExpected);
    }

    @Test
    public void processMessage_correctInputWithoutReplace() {
        Message message = new Message()
                .setChatId(1L)
                .setText("asdasdasd")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/2")
                .setText("Введите дату и причину посещения:")
                .setNext("/3")
                .setNeedToSave(true);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);
        Step nextStep = new Step()
                .setId("/3")
                .setText("Спасибо!") //!!!
                .setNext("/4")
                .setNeedToSave(false);
        TelegramBotService.getActiveDialogs().clear();
        Mockito.doReturn(null).when(dataService).save(Mockito.any(Message.class), Mockito.any(Dialog.class));
        Mockito.when(stepsService.get(Mockito.anyString())).thenReturn(Optional.of(nextStep));
        Mockito.when(variablesResolverService.resolveAllVariables(Mockito.any(Dialog.class))).thenReturn(nextStep.getText()); //!!!

        SendMessage sendMessageActual = dataMessageService.processMessage(message, dialog, (a, b, c) -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(a));
            sendMessage.setText(b);
            return sendMessage;
        });

        Mockito.verify(dataService, Mockito.times(1)).save(message, dialog);
        Dialog expectedDialog = new Dialog()
                .setId(dialog.getId())
                .setStep(nextStep);
        Assertions.assertThat(TelegramBotService.getActiveDialogs().values().stream().findFirst().get())
                .isEqualTo(expectedDialog);
        SendMessage sendMessageExpected = new SendMessage();
        sendMessageExpected.setChatId(message.getChatId());
        sendMessageExpected.setText(nextStep.getText());
        Assertions.assertThat(sendMessageActual).isEqualTo(sendMessageExpected);
    }

    //Negative
    @Test
    public void processMessage_emptyNextStep() {
        Message message = new Message()
                .setChatId(1L)
                .setText("asdasdasd")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/2")
                .setText("Введите дату и причину посещения:")
                .setNext("")
                .setNeedToSave(true);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);
        TelegramBotService.getActiveDialogs().clear();
        Mockito.doReturn(null).when(dataService).save(Mockito.any(Message.class), Mockito.any(Dialog.class));
        Mockito.when(stepsService.get(Mockito.anyString())).thenReturn(Optional.empty()); //!!!

        SendMessage sendMessageActual = dataMessageService.processMessage(message, dialog, (a, b, c) -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(a));
            sendMessage.setText(b);
            return sendMessage;
        });

        Mockito.verify(dataService, Mockito.times(1)).save(Mockito.any(Message.class), Mockito.any(Dialog.class));
        Assertions.assertThat(TelegramBotService.getActiveDialogs().size()).isEqualTo(0);
        Assertions.assertThat(sendMessageActual).isEqualTo(new SendMessage());
    }

    @Test
    public void processMessage_savedDataIsEmpty() {
        Message message = new Message()
                .setChatId(1L)
                .setText("asdasdasd")
                .setType(MessageType.DATA);
        Step lastStep = new Step()
                .setId("/2")
                .setText("Введите дату и причину посещения:")
                .setNext("/3")
                .setNeedToSave(true);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(lastStep);
        Step nextStep = new Step()
                .setId("/3")
                .setText("<<savedData>>")
                .setNext("/4")
                .setNeedToSave(false);
        TelegramBotService.getActiveDialogs().clear();
        Mockito.doReturn(null).when(dataService).save(Mockito.any(Message.class), Mockito.any(Dialog.class));
        Mockito.when(stepsService.get(Mockito.anyString())).thenReturn(Optional.of(nextStep));
        Mockito.when(variablesResolverService.resolveAllVariables(Mockito.any(Dialog.class)))
                .thenReturn("нет данных"); //!!!

        SendMessage sendMessageActual = dataMessageService.processMessage(message, dialog, (a, b, c) -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(a));
            sendMessage.setText(b);
            return sendMessage;
        });

        Mockito.verify(dataService, Mockito.times(1)).save(message, dialog);
        Dialog expectedDialog = new Dialog()
                .setId(dialog.getId())
                .setStep(nextStep);
        Assertions.assertThat(TelegramBotService.getActiveDialogs().values().stream().findFirst().get()).isEqualTo(expectedDialog);
        SendMessage sendMessageExpected = new SendMessage();
        sendMessageExpected.setChatId(message.getChatId());
        sendMessageExpected.setText("нет данных");
        Assertions.assertThat(sendMessageActual).isEqualTo(sendMessageExpected);
    }
}
