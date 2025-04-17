package ru.exyon.telegrambot.repositories;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import ru.exyon.telegrambot.models.Data;

@Repository
public interface DataRepository extends ListCrudRepository<Data, String>, DataInfoRepository {
}
