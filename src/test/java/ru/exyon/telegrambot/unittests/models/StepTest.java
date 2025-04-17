package ru.exyon.telegrambot.unittests.models;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.exyon.telegrambot.models.Step;

import java.util.ArrayList;
import java.util.List;

public class StepTest {

    //Positive
    @Test
    public void getButtonsList_withButtons() {
        String buttonText = "Далее";
        String buttonCallbackData = "/1";
        Step step = new Step();
        step.setText("Hello <<firstname>>!");
        step.setButtons("""
                [
                    {
                        "text": "%s",
                        "callbackData": "%s"
                    }
                ]
                """.formatted(buttonText, buttonCallbackData));

        List<InlineKeyboardButton> buttonsListActual = step.getButtonsList();

        List<InlineKeyboardButton> buttonsListExpected = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setCallbackData(buttonCallbackData);
        buttonsListExpected.add(button);

        Assertions.assertThat(buttonsListActual).isEqualTo(buttonsListExpected);
    }

    @Test
    public void getButtonsList_emptyButtons() {
        Step step = new Step();
        step.setText("Hello <<firstname>>!");
        step.setButtons("");

        List<InlineKeyboardButton> buttonsListActual = step.getButtonsList();

        Assertions.assertThat(buttonsListActual).isEqualTo(List.of());
    }

    @Test
    public void getButtonsList_wrongButtonsJson() {
        String buttonText = "Далее";
        String buttonCallbackData = "/1";
        Step step = new Step();
        step.setText("Hello <<firstname>>!");
        step.setButtons("""
                [
                    {
                        "text1": "%s",
                        "callbackData1": "%s"
                    }
                ]
                """.formatted(buttonText, buttonCallbackData));

        List<InlineKeyboardButton> buttonsListActual = step.getButtonsList();

        Assertions.assertThat(buttonsListActual).isEqualTo(List.of());
    }

}
