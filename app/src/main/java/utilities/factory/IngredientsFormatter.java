package utilities.factory;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public interface IngredientsFormatter {

    @NonNull
    List<String> formatInput(@NonNull String ingredients);

    @NonNull
    String formatOutput(@NonNull List<String> ingredients);
}