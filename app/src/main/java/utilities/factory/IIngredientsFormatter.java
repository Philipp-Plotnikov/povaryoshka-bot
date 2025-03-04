package utilities.factory;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public interface IIngredientsFormatter {

    @NonNull
    List<String> formatInput(@NonNull String input);

    @NonNull
    String formatOutput(@NonNull List<String> input);
}