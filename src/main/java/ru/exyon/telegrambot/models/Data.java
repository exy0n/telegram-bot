package ru.exyon.telegrambot.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("data")
@Accessors(chain = true)
@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class Data {
    @Id
    private UUID id;
    @JsonAlias({"dialog_id", "dialogId"})
    private UUID dialogId;
    private String username;
    private String firstname;
    @JsonAlias({"step_id", "stepId"})
    private String stepId;
    private String data;
    private LocalDateTime created;

    public Data(Message message, Dialog dialog) {
        this.dialogId = dialog.getId();
        this.username = message.getUsername();
        this.firstname = message.getFirstname();
        this.stepId = dialog.getStep().getId();
        this.data = message.getText();
        this.created = LocalDateTime.now();
    }
}
