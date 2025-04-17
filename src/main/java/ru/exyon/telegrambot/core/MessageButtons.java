package ru.exyon.telegrambot.core;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public interface MessageButtons {
    default InlineKeyboardButton getStartButton(String startStep) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("В начало");
        inlineKeyboardButton.setCallbackData(startStep);
        return inlineKeyboardButton;
    }
}
