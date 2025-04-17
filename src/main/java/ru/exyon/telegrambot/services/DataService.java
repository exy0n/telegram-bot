package ru.exyon.telegrambot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.exyon.telegrambot.models.*;
import ru.exyon.telegrambot.repositories.DataRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataService {
    private final DataRepository dataRepository;

    public Data save(Message message, Dialog dialog) {
        return dataRepository.save(new Data(message, dialog));
    }

    public Optional<DataInfo> getSavedData(Dialog dialog) {
        return dataRepository.findByDataContains(dialog.getId());
    }
}
