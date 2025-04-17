package ru.exyon.telegrambot.core.variables;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.exyon.telegrambot.core.ExcludeFromJacocoGeneratedReport;
import ru.exyon.telegrambot.models.DataInfo;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;
import ru.exyon.telegrambot.models.Step;
import ru.exyon.telegrambot.services.DataService;

import java.util.Optional;

@Component
@AllArgsConstructor
public class SavedDataVariable extends Variable {
    private final DataService dataService;

    @Override
    public String getName() {
        return "savedData";
    }

    @Override
    @ExcludeFromJacocoGeneratedReport
    public String getValue(Dialog dialog) {
        Optional<DataInfo> savedDataOptional = dataService.getSavedData(dialog);
        if (savedDataOptional.isPresent()) {
            return savedDataOptional.get().toString();
        }
        return "нет данных";
    }
}