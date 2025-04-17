package ru.exyon.telegrambot.core.variables;

import ru.exyon.telegrambot.models.Dialog;

public abstract class Variable {

    public abstract String getName();

    public abstract String getValue(Dialog dialog);
}
