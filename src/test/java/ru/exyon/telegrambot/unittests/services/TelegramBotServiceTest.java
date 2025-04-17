package ru.exyon.telegrambot.unittests.services;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.exyon.telegrambot.config.BotConfig;
import ru.exyon.telegrambot.config.ExecutorConfig;
import ru.exyon.telegrambot.core.MessageFunction;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;
import ru.exyon.telegrambot.services.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {BotConfig.class, ExecutorConfig.class})
@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TelegramBotServiceTest {
    @Autowired
    private BotConfig botConfigValues;
    @Mock
    private BotConfig botConfig;
    @Mock
    private CommandMessageService commandMessageService;
    @Mock
    private DataMessageService dataMessageService;
    @Mock
    private RedirectService redirectService;
    @Mock
    private ValidateMessageService validateMessageService;
    @Autowired
    private ThreadPoolTaskExecutor executorService;
    @Spy
    private List<MessageService> messageServices = new ArrayList<>();
    @InjectMocks
    @Spy
    private TelegramBotService telegramBotService;
    private final int waitSecondsUntilFutureEnds = 3;

    @BeforeAll
    public void beforeAll() {
        telegramBotService.setExecutorService(executorService);
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(redirectService, validateMessageService, commandMessageService, dataMessageService);
        if (messageServices.isEmpty()) {
            messageServices.add(commandMessageService);
            messageServices.add(dataMessageService);
        }
    }

    //Positive
    @Test
    public void runAsync_commandMessageFromUser() {
        org.telegram.telegrambots.meta.api.objects.Message telegramMessage = new org.telegram.telegrambots.meta.api.objects.Message();
        telegramMessage.setText("/1");
        User user = new User();
        user.setUserName("vova");
        user.setFirstName("Vova");
        telegramMessage.setFrom(user);
        Chat chat = new Chat();
        chat.setId(1L);
        telegramMessage.setChat(chat);
        Update update = new Update();
        update.setMessage(telegramMessage);
        Dialog dialog = new Dialog();
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(1L, dialog);
        Mockito.when(redirectService.isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class),
                Mockito.any(Consumer.class))).thenReturn(false);
        Mockito.when(validateMessageService.isValid(Mockito.any(Message.class), Mockito.any(Dialog.class),
                Mockito.any(MessageFunction.class))).thenReturn(true);
        Mockito.when(commandMessageService.getMessageType()).thenCallRealMethod();
        Mockito.lenient().when(dataMessageService.getMessageType()).thenCallRealMethod();

        CompletableFuture<Void> voidCompletableFuture = telegramBotService.runAsync(update);

        Awaitility.await().atMost(waitSecondsUntilFutureEnds, TimeUnit.SECONDS).until(voidCompletableFuture::isDone);

        Mockito.verify(redirectService, Mockito.times(1))
                .isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(Consumer.class));
        Mockito.verify(validateMessageService, Mockito.times(1))
                .isValid(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
        Mockito.verify(commandMessageService, Mockito.times(1))
                .processMessage(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
    }

    @Test
    public void runAsync_dataMessageFromUser() {
        org.telegram.telegrambots.meta.api.objects.Message telegramMessage = new org.telegram.telegrambots.meta.api.objects.Message();
        telegramMessage.setText("89197212233");
        User user = new User();
        user.setUserName("vova");
        user.setFirstName("Vova");
        telegramMessage.setFrom(user);
        Chat chat = new Chat();
        chat.setId(1L);
        telegramMessage.setChat(chat);
        Update update = new Update();
        update.setMessage(telegramMessage);
        Dialog dialog = new Dialog();
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(1L, dialog);
        Mockito.when(redirectService.isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class),
                Mockito.any(Consumer.class))).thenReturn(false);
        Mockito.when(validateMessageService.isValid(Mockito.any(Message.class), Mockito.any(Dialog.class),
                Mockito.any(MessageFunction.class))).thenReturn(true);
        Mockito.when(dataMessageService.getMessageType()).thenCallRealMethod();
        Mockito.lenient().when(commandMessageService.getMessageType()).thenCallRealMethod();

        CompletableFuture<Void> voidCompletableFuture = telegramBotService.runAsync(update);

        Awaitility.await().atMost(waitSecondsUntilFutureEnds, TimeUnit.SECONDS).until(voidCompletableFuture::isDone);

        Mockito.verify(redirectService, Mockito.times(1))
                .isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(Consumer.class));
        Mockito.verify(validateMessageService, Mockito.times(1))
                .isValid(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
        Mockito.verify(dataMessageService, Mockito.times(1))
                .processMessage(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
    }

    @Test
    public void runAsync_redirectNeeded() {
        org.telegram.telegrambots.meta.api.objects.Message telegramMessage = new org.telegram.telegrambots.meta.api.objects.Message();
        telegramMessage.setText("/1");
        User user = new User();
        user.setUserName("vova");
        user.setFirstName("Vova");
        telegramMessage.setFrom(user);
        Chat chat = new Chat();
        chat.setId(1L);
        telegramMessage.setChat(chat);
        Update update = new Update();
        update.setMessage(telegramMessage);
        Dialog dialog = new Dialog();
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(1L, dialog);
        Mockito.when(redirectService.isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class),
                Mockito.any(Consumer.class))).thenReturn(true);

        CompletableFuture<Void> voidCompletableFuture = telegramBotService.runAsync(update);

        Awaitility.await().atMost(waitSecondsUntilFutureEnds, TimeUnit.SECONDS).until(voidCompletableFuture::isDone);

        Mockito.verify(redirectService, Mockito.times(1))
                .isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(Consumer.class));
        Mockito.verify(validateMessageService, Mockito.times(0))
                .isValid(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
        Mockito.verify(commandMessageService, Mockito.times(0))
                .processMessage(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
    }

    @Test
    public void runAsync_invalidMessage() {
        org.telegram.telegrambots.meta.api.objects.Message telegramMessage = new org.telegram.telegrambots.meta.api.objects.Message();
        telegramMessage.setText("/1");
        User user = new User();
        user.setUserName("vova");
        user.setFirstName("Vova");
        telegramMessage.setFrom(user);
        Chat chat = new Chat();
        chat.setId(1L);
        telegramMessage.setChat(chat);
        Update update = new Update();
        update.setMessage(telegramMessage);
        Dialog dialog = new Dialog();
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(1L, dialog);
        Mockito.when(redirectService.isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class),
                Mockito.any(Consumer.class))).thenReturn(false);
        Mockito.when(validateMessageService.isValid(Mockito.any(Message.class), Mockito.any(Dialog.class),
                Mockito.any(MessageFunction.class))).thenReturn(false);

        CompletableFuture<Void> voidCompletableFuture = telegramBotService.runAsync(update);

        Awaitility.await().atMost(waitSecondsUntilFutureEnds, TimeUnit.SECONDS).until(voidCompletableFuture::isDone);

        Mockito.verify(redirectService, Mockito.times(1))
                .isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(Consumer.class));
        Mockito.verify(validateMessageService, Mockito.times(1))
                .isValid(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
        Mockito.verify(commandMessageService, Mockito.times(0))
                .processMessage(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
    }

    @Test
    public void buildAndSendMessage_withButtons() throws TelegramApiException {
        Long chatId = 1L;
        String textToSend = "/1";
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("В начало");
        button.setCallbackData(botConfigValues.getStartStep());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(1L);
        sendMessage.setText("");
        Mockito.doReturn(null).when(telegramBotService).execute(Mockito.any(SendMessage.class));

        SendMessage sendMessageActual = telegramBotService.buildAndSendMessage(chatId, textToSend, List.of(button));

        SendMessage sendMessageExpected = new SendMessage();
        sendMessageExpected.setChatId(chatId);
        sendMessageExpected.setText(textToSend);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button)));
        sendMessageExpected.setReplyMarkup(inlineKeyboardMarkup);
        sendMessageExpected.setParseMode("Markdown");
        Assertions.assertThat(sendMessageActual).isEqualTo(sendMessageExpected);
    }

    @Test
    public void buildAndSendMessage_emptyButtonsList() throws TelegramApiException {
        Long chatId = 1L;
        String textToSend = "/1";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(1L);
        sendMessage.setText("");
        Mockito.doReturn(null).when(telegramBotService).execute(Mockito.any(SendMessage.class));

        SendMessage sendMessageActual = telegramBotService.buildAndSendMessage(chatId, textToSend, List.of());

        SendMessage sendMessageExpected = new SendMessage();
        sendMessageExpected.setChatId(chatId);
        sendMessageExpected.setText(textToSend);
        sendMessageExpected.setParseMode("Markdown");

        Assertions.assertThat(sendMessageActual).isEqualTo(sendMessageExpected);
    }

    @Test
    public void buildAndSendMessage_nullButtonsList() throws TelegramApiException {
        Long chatId = 1L;
        String textToSend = "/1";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(1L);
        sendMessage.setText("");
        Mockito.doReturn(null).when(telegramBotService).execute(Mockito.any(SendMessage.class));

        SendMessage sendMessageActual = telegramBotService.buildAndSendMessage(chatId, textToSend, null);

        SendMessage sendMessageExpected = new SendMessage();
        sendMessageExpected.setChatId(chatId);
        sendMessageExpected.setText(textToSend);
        sendMessageExpected.setParseMode("Markdown");

        Assertions.assertThat(sendMessageActual).isEqualTo(sendMessageExpected);
    }

    //Negative
    @Test
    public void runAsync_cantSelectMessageService() {
        org.telegram.telegrambots.meta.api.objects.Message telegramMessage = new org.telegram.telegrambots.meta.api.objects.Message();
        telegramMessage.setText("/1");
        User user = new User();
        user.setUserName("vova");
        user.setFirstName("Vova");
        telegramMessage.setFrom(user);
        Chat chat = new Chat();
        chat.setId(1L);
        telegramMessage.setChat(chat);
        Update update = new Update();
        update.setMessage(telegramMessage);
        Dialog dialog = new Dialog();
        TelegramBotService.getActiveDialogs().clear();
        TelegramBotService.getActiveDialogs().put(1L, dialog);
        Mockito.when(redirectService.isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class),
                Mockito.any(Consumer.class))).thenReturn(false);
        Mockito.when(validateMessageService.isValid(Mockito.any(Message.class), Mockito.any(Dialog.class),
                Mockito.any(MessageFunction.class))).thenReturn(true);
        messageServices.clear();

        CompletableFuture<Void> voidCompletableFuture = telegramBotService.runAsync(update);

        Awaitility.await().atMost(waitSecondsUntilFutureEnds, TimeUnit.SECONDS).until(voidCompletableFuture::isDone);

        Mockito.verify(redirectService, Mockito.times(1))
                .isRedirectNeeded(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(Consumer.class));
        Mockito.verify(validateMessageService, Mockito.times(1))
                .isValid(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
        Mockito.verify(commandMessageService, Mockito.times(0))
                .processMessage(Mockito.any(Message.class), Mockito.any(Dialog.class), Mockito.any(MessageFunction.class));
    }

    @Test
    public void sendMessage_errorWhileSendMessage() throws TelegramApiException {
        Long chatId = 1L;
        String textToSend = "/1";
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(1L);
        sendMessage.setText("");
        Mockito.doThrow(TelegramApiException.class).when(telegramBotService).execute(Mockito.any(SendMessage.class));
        Logger logger = (Logger) LoggerFactory.getLogger(TelegramBotService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        telegramBotService.buildAndSendMessage(chatId, textToSend, List.of());

        List<ILoggingEvent> logsList = listAppender.list;
        Assertions.assertThat(logsList.get(0).getMessage())
                .isEqualTo("Cant send message to bot, message: {}");
        Assertions.assertThat(logsList.get(0).getLevel())
                .isEqualTo(Level.ERROR);
    }
}
