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
import ru.exyon.telegrambot.enums.MessageType;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;
import ru.exyon.telegrambot.models.Step;
import ru.exyon.telegrambot.services.RedirectService;
import ru.exyon.telegrambot.services.TelegramBotService;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BotConfig.class)
public class RedirectServiceTest {

    @Autowired
    private BotConfig botConfigValues;
    @Mock
    private BotConfig botConfig;

    @InjectMocks
    private RedirectService redirectService;

    //Positive
    @Test
    public void isRedirectNeeded_dialogNotStartedDataMessage(){
        Message message = new Message()
                .setChatId(1L)
                .setText("asdas")
                .setType(MessageType.DATA);
        Mockito.when(botConfig.getStartStep()).thenReturn(botConfigValues.getStartStep());

        boolean isRedirectNeeded = redirectService.isRedirectNeeded(message, new Dialog(), (m) -> new SendMessage());
        Assertions.assertThat(isRedirectNeeded).isTrue();

        Message expectedMessage = new Message()
                .setChatId(message.getChatId())
                .setText(botConfig.getStartStep())
                .setType(MessageType.COMMAND);
        Assertions.assertThat(message).isEqualTo(expectedMessage);
    }

    @Test
    public void isRedirectNeeded_dialogEnded(){
        Message message = new Message()
                .setChatId(1L)
                .setText("asdasd")
                .setType(MessageType.DATA);
        Step step = new Step()
                .setText("/5")
                .setNext("end");
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(step);
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(message.getChatId(), dialog);
        Mockito.when(botConfig.getStartStep()).thenReturn(botConfigValues.getStartStep());
        Mockito.when(botConfig.getEndMarker()).thenReturn(botConfigValues.getEndMarker());

        boolean isRedirectNeeded = redirectService.isRedirectNeeded(message, dialog, (m) -> new SendMessage());
        Assertions.assertThat(isRedirectNeeded).isTrue();

        Message expectedMessage = new Message()
                .setChatId(message.getChatId())
                .setText(botConfig.getStartStep())
                .setType(MessageType.COMMAND);
        Assertions.assertThat(message).isEqualTo(expectedMessage);

        Assertions.assertThat(TelegramBotService.getActiveDialogs().size()).isEqualTo(0);
    }

    @Test
    public void isRedirectNeeded_startCommandReceivedNotOnInitStep(){
        Message message = new Message()
                .setChatId(1L)
                .setText(botConfigValues.getStartStep())
                .setType(MessageType.COMMAND);
        Step step = new Step()
                .setId("/5")
                .setText("Hello")
                .setNext("/6");
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(step);
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(message.getChatId(), dialog);
        Mockito.when(botConfig.getStartStep()).thenReturn(botConfigValues.getStartStep());
        Mockito.when(botConfig.getEndMarker()).thenReturn(botConfigValues.getEndMarker());

        boolean isRedirectNeeded = redirectService.isRedirectNeeded(message, dialog, (m) -> new SendMessage());
        Assertions.assertThat(isRedirectNeeded).isTrue();

        Message expectedMessage = new Message()
                .setChatId(message.getChatId())
                .setText(botConfig.getStartStep())
                .setType(MessageType.COMMAND);
        Assertions.assertThat(message).isEqualTo(expectedMessage);

        Assertions.assertThat(TelegramBotService.getActiveDialogs().size()).isEqualTo(0);
    }

    //Negative
    @Test
    public void isRedirectNeeded_startCommandNotReceivedAndNotOnInitStep(){
        Message message = new Message()
                .setChatId(1L)
                .setText("/6")
                .setType(MessageType.COMMAND);
        Step step = new Step()
                .setText("/5")
                .setNext("/6");
        UUID dialogId = UUID.randomUUID();
        Dialog dialog = new Dialog()
                .setId(dialogId)
                .setStep(step);
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(message.getChatId(), dialog);
        Mockito.when(botConfig.getStartStep()).thenReturn(botConfigValues.getStartStep());
        Mockito.when(botConfig.getEndMarker()).thenReturn(botConfigValues.getEndMarker());

        boolean isRedirectNeeded = redirectService.isRedirectNeeded(message, dialog, (m) -> new SendMessage());
        Assertions.assertThat(isRedirectNeeded).isFalse();

        Message expectedMessage = new Message()
                .setChatId(message.getChatId())
                .setText(message.getText())
                .setType(MessageType.COMMAND);
        Assertions.assertThat(message).isEqualTo(expectedMessage);

        Assertions.assertThat(TelegramBotService.getActiveDialogs().values().stream().findFirst().get().getId())
                .isEqualTo(dialogId);
    }

    @Test
    public void isRedirectNeeded_startCommandReceivedOnInitStep(){
        Message message = new Message()
                .setChatId(1L)
                .setText(botConfigValues.getStartStep())
                .setType(MessageType.COMMAND);
        Step step = new Step().toInitStep(botConfigValues);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(step);
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(message.getChatId(), dialog);
        Mockito.when(botConfig.getStartStep()).thenReturn(botConfigValues.getStartStep());
        Mockito.when(botConfig.getEndMarker()).thenReturn(botConfigValues.getEndMarker());
        Mockito.when(botConfig.getInitStep()).thenReturn(botConfigValues.getInitStep());

        boolean isRedirectNeeded = redirectService.isRedirectNeeded(message, dialog, (m) -> new SendMessage());
        Assertions.assertThat(isRedirectNeeded).isFalse();

        Message expectedMessage = new Message()
                .setChatId(message.getChatId())
                .setText(message.getText())
                .setType(MessageType.COMMAND);
        Assertions.assertThat(message).isEqualTo(expectedMessage);

        Assertions.assertThat(TelegramBotService.getActiveDialogs().values().stream().findFirst().get().getId())
                .isEqualTo(dialog.getId());
    }

}
