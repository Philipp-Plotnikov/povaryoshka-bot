package models.ioformatter.base;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public interface FormatOutput {
    @NonNull
    String formatOutput (@NonNull List<String> listOfStrings);
}
