package models.ioformatter.base;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public interface FormatInput {
    @NonNull
    List<String> formatInput (@NonNull String input);
}
