package ru.exyon.telegrambot.repositories;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import ru.exyon.telegrambot.models.Step;

@Repository
public interface StepsRepository extends ListCrudRepository<Step, String> {
}
