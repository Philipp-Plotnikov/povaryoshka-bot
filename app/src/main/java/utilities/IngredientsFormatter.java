package utilities;

import org.checkerframework.checker.nullness.qual.NonNull;
import utilities.factory.IIngredientsFormatter;

import java.util.Arrays;
import java.util.List;

public class IngredientsFormatter implements IIngredientsFormatter {

    @Override
    @NonNull
    public List<String> formatInput(@NonNull String input) {
        return Arrays.asList(input
                .replaceAll("(\\r?\\n)+", "\n")
                .split("\n"));
    }

    @Override
    @NonNull
    public String formatOutput(@NonNull List<String> input) {
        return String.join("\n", input);
    }
}