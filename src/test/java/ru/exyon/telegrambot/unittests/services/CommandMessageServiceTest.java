package ru.exyon.telegrambot.unittests.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.exyon.telegrambot.config.BotConfig;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;
import ru.exyon.telegrambot.models.Step;
import ru.exyon.telegrambot.services.CommandMessageService;
import ru.exyon.telegrambot.services.StepsService;
import ru.exyon.telegrambot.services.TelegramBotService;
import ru.exyon.telegrambot.services.VariablesResolverService;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BotConfig.class)
public class CommandMessageServiceTest {

    @Autowired
    private BotConfig botConfigValues;
    @Mock
    private BotConfig botConfig;
    @Mock
    private StepsService stepsService;
    @Mock
    private VariablesResolverService variablesResolverService;
    @InjectMocks
    private CommandMessageService commandMessageService;

    //Positive
    @Test
    public void processMessage_startStepWithoutButtons() {
        Message message = new Message();
        message.setChatId(1L);
        message.setText(botConfigValues.getStartStep());
        message.setFirstname("Vova");
        Step step = new Step();
        step.setText("Hello <<firstname>>!");
        Dialog dialog = new Dialog();
        dialog.setId(UUID.randomUUID());
        dialog.setStep(new Step().setText("old text"));
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(message.getChatId(), dialog);
        Mockito.when(stepsService.get(Mockito.any())).thenReturn(Optional.of(step));
        Mockito.when(botConfig.getStartStep()).thenReturn(botConfigValues.getStartStep());
        Mockito.when(variablesResolverService.resolveAllVariables(Mockito.any(Dialog.class))).thenReturn("Hello Vova!");

        SendMessage sendMessageActual = commandMessageService.processMessage(message, dialog, (a, b, c) -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(a));
            sendMessage.setText(b);
            return sendMessage;
        });

        Assertions.assertThat(TelegramBotService.getActiveDialogs().values().stream().findFirst().get().getStep().getText())
                .isEqualTo("Hello <<firstname>>!");

        SendMessage sendMessageExpected = new SendMessage();
        sendMessageExpected.setChatId(message.getChatId());
        sendMessageExpected.setText("Hello Vova!");
        Assertions.assertThat(sendMessageActual).isEqualTo(sendMessageExpected);
    }


    @Test
    public void processMessage_otherThanStartStep() {
        Message message = new Message();
        message.setChatId(1L);
        message.setText("/1");
        Step step = new Step();
        step.setText("Информация о магазине:");
        UUID dialogId = UUID.randomUUID();
        Dialog dialog = new Dialog();
        dialog.setId(dialogId);
        dialog.setStep(new Step().setText("old text"));
        Mockito.when(stepsService.get(Mockito.anyString())).thenReturn(Optional.of(step));
        Mockito.when(botConfig.getStartStep()).thenReturn(botConfigValues.getStartStep());
        Mockito.when(variablesResolverService.resolveAllVariables(Mockito.any(Dialog.class)))
                .thenReturn(step.getText());

        SendMessage sendMessageActual = commandMessageService.processMessage(message, dialog, (a, b, c) -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(a));
            sendMessage.setText(b);
            return sendMessage;
        });

        SendMessage sendMessageExpected = new SendMessage();
        sendMessageExpected.setChatId(message.getChatId());
        sendMessageExpected.setText(step.getText());
        Assertions.assertThat(sendMessageActual).isEqualTo(sendMessageExpected);

        Dialog dialogExpected = new Dialog();
        dialogExpected.setId(dialogId);
        dialogExpected.setStep(new Step().setText(step.getText()));
        Assertions.assertThat(TelegramBotService.getActiveDialogs().values().stream().findFirst().get())
                .isEqualTo(dialogExpected);
    }

    //Negative
    @Test
    public void processMessage_noStepInDB() {
        Message message = new Message();
        message.setChatId(1L);
        message.setText(botConfigValues.getStartStep());
        message.setFirstname("Vova");
        Step step = new Step();
        step.setText("Hello <<firstname>>!");
        Dialog dialog = new Dialog();
        dialog.setId(UUID.randomUUID());
        dialog.setStep(new Step().setText("old text"));
        Mockito.when(stepsService.get(Mockito.anyString())).thenReturn(Optional.empty()); //!!!

        SendMessage sendMessageActual = commandMessageService.processMessage(message, dialog, (a, b, c) -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(a));
            sendMessage.setText(b);
            return sendMessage;
        });

        Assertions.assertThat(sendMessageActual).isEqualTo(new SendMessage());
    }

}
