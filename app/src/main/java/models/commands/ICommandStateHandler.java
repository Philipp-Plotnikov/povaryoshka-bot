package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.telegram.telegrambots.meta.api.objects.Update;

import models.dtos.UserContextDTO;

@FunctionalInterface
public interface ICommandStateHandler {
    void handle(@NonNull final Update update, @NonNull final UserContextDTO userContextDTO);
}
