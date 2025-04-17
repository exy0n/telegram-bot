package ru.exyon.telegrambot.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.exyon.telegrambot.config.BotConfig;

import java.util.ArrayList;
import java.util.List;

@Table("steps")
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Step {
    @Id
    private String id;
    private String text;
    private String buttons;
    @JsonAlias({"is_need_to_save", "isNeedToSave"})
    private boolean isNeedToSave;
    private String next;
    private String regexp;

    public List<InlineKeyboardButton> getButtonsList() {
        if (buttons == null || buttons.isEmpty()) {
            return List.of();
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Button> buttonsList;
        try {
            buttonsList = mapper.readValue(buttons, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Cant parse buttons for step {}", this);
            e.printStackTrace();
            return List.of();
        }
        return buttonsList.stream().map(button -> {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(button.getText());
            inlineKeyboardButton.setCallbackData(button.getCallbackData());
            return inlineKeyboardButton;
        }).toList();
    }

    public Step toInitStep(BotConfig botConfig) {
        this.id = botConfig.getInitStep();
        this.text = "InitStep";
        this.next = botConfig.getStartStep();
        return this;
    }
}
