package ru.exyon.telegrambot.integration.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.exyon.telegrambot.models.*;
import ru.exyon.telegrambot.repositories.DataRepository;
import ru.exyon.telegrambot.services.DataService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest
public class DataServiceTest {
    @Autowired
    private DataService dataService;

    @Autowired
    private DataRepository dataRepository;

    @BeforeEach
    public void beforeEach() {
        dataRepository.deleteAll();
    }

    @Test
    public void save() {
        Message message = new Message()
                .setText("asdasdasd")
                .setUsername("vova")
                .setFirstname("Vova");
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setStep(new Step().setId("/1"));

        Data afterSaveData = dataService.save(message, dialog);

        Data expectedData = new Data()
                .setId(afterSaveData.getId())
                .setDialogId(dialog.getId())
                .setUsername(message.getUsername())
                .setFirstname(message.getFirstname())
                .setStepId(dialog.getStep().getId())
                .setData(message.getText());

        Optional<Data> savedDataOptional = dataRepository.findById(afterSaveData.getId().toString());
        Assertions.assertThat(savedDataOptional).isNotEmpty();
        Assertions.assertThat(savedDataOptional.get())
                .usingRecursiveComparison()
                .ignoringFields("created")
                .isEqualTo(expectedData);
    }

    @Test
    public void getSavedData() {
        Message message = new Message()
                .setText("просто посмотреть")
                .setUsername("vova")
                .setFirstname("Vova");
        UUID dialogId = UUID.randomUUID();
        Dialog dialog = new Dialog()
                .setId(dialogId)
                .setStep(new Step().setId("/1"));
        dataRepository.save(new Data(message, dialog));

        Message message2 = new Message()
                .setText("01.02.2025 11:12")
                .setUsername("vova")
                .setFirstname("Vova");
        Dialog dialog2 = new Dialog()
                .setId(dialogId)
                .setStep(new Step().setId("/2"));
        dataRepository.save(new Data(message2, dialog2));

        Optional<DataInfo> actualDataOptional = dataService.getSavedData(dialog);

        Assertions.assertThat(actualDataOptional).isNotEmpty();
        DataInfo expectedData = new DataInfo()
                .setReason(message.getText())
                .setDate(message2.getText());
        Assertions.assertThat(actualDataOptional.get()).isEqualTo(expectedData);
    }

}
