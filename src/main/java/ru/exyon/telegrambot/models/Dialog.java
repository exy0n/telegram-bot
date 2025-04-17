package ru.exyon.telegrambot.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dialog {
    private UUID id;
    private String username;
    private String firstname;
    private Step step;

    public Dialog(Step step, Message message) {
        this.id = UUID.randomUUID();
        this.username = message.getUsername();
        this.firstname = message.getFirstname();
        this.step = step;
    }
}
