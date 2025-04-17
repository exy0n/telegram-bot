package ru.exyon.telegrambot.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataInfo {
    private String date;
    private String reason;

    public String toString() {
        return "*" + date + "* c целью: *" + reason + "*";
    }
}
