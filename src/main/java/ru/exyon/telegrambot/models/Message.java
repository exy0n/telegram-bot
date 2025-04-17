package ru.exyon.telegrambot.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.exyon.telegrambot.enums.MessageType;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Message {
    private String text;
    private String username;
    private String firstname;
    private Long chatId;
    private MessageType type;

    public Message(Update update) {
        //Если получено сообщение от пользователя
        if (update.hasMessage() && update.getMessage().hasText()) {
            this.text = update.getMessage().getText().trim();
            this.username = update.getMessage().getFrom().getUserName();
            this.firstname = update.getMessage().getFrom().getFirstName();
            this.chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) { //Если получен callback после нажатия кнопки
            this.text = update.getCallbackQuery().getData().trim();
            this.username = update.getCallbackQuery().getFrom().getUserName();
            this.firstname = update.getCallbackQuery().getFrom().getFirstName();
            this.chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            log.error("Cant init message values from update {}", update);
            this.text = "/initError";
            this.username = "";
            this.firstname = "";
            this.chatId = 0L;
        }
        this.type = text.startsWith("/") ? MessageType.COMMAND : MessageType.DATA;
    }

    public void toStartCommand(String startStep) {
        setText(startStep);
        setType(MessageType.COMMAND);
    }
}
