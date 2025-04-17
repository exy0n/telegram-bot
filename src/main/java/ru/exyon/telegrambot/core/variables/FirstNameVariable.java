package ru.exyon.telegrambot.core.variables;

import org.springframework.stereotype.Component;
import ru.exyon.telegrambot.models.Dialog;

@Component
public class FirstNameVariable extends Variable {

    @Override
    public String getName() {
        return "firstname";
    }

    @Override
    public String getValue(Dialog dialog) {
        return dialog.getFirstname();
    }
}
