package ru.exyon.telegrambot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.exyon.telegrambot.models.Step;
import ru.exyon.telegrambot.repositories.StepsRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepsService {
    private final StepsRepository stepsRepository;

    public Optional<Step> get(String id) {
        return stepsRepository.findById(id);
    }
}
