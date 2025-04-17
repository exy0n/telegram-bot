package ru.exyon.telegrambot.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.exyon.telegrambot.core.ExcludeFromJacocoGeneratedReport;
import ru.exyon.telegrambot.core.variables.Variable;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Step;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@AllArgsConstructor
public class VariablesResolverService {
    private final Set<Variable> variables;

    /**
     * Выполняет замену переменных на значения, в зависимости от реализации метода getValue(...) переменной. <br>
     * Переменные должны обрамляться символами << и >>, например << firstname >>
     * @param dialog текущий активный диалог
     * @return текст с замененными переменными, если в тексте не было переменных - оригинальный текст
     */
    public String resolveAllVariables(Dialog dialog) {
        Step step = dialog.getStep();
        String text = step.getText();
        StringBuilder resultTextStringBuilder = null;
        Matcher matcher = Pattern.compile("<<[^<>]+>>").matcher(text);
        while (matcher.find()) {
            if (resultTextStringBuilder == null) {
                resultTextStringBuilder = new StringBuilder();
            }
            Optional<Variable> variableOptional = variables.stream()
                    .filter(variable -> variable.getName().equals(matcher.group()
                                    .substring(2, matcher.group().length() - 2).trim())).findFirst();
            if (variableOptional.isPresent()) {
                matcher.appendReplacement(resultTextStringBuilder, variableOptional.get().getValue(dialog));
            } else {
                log.warn("Cant resolve variable: {}, step.id: {}", matcher.group(), step.getId());
            }
        }
        if (resultTextStringBuilder != null) {
            matcher.appendTail(resultTextStringBuilder);
            text = resultTextStringBuilder.toString();
        }
        return text;
    }
}