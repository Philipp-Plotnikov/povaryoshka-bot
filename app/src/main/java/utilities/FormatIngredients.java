package utilities;

import org.checkerframework.checker.nullness.qual.NonNull;
import utilities.factory.IngredientsFormatter;

import java.util.Arrays;
import java.util.List;

public class FormatIngredients implements IngredientsFormatter {

    @NonNull
    @Override
    public List<String> formatInput(@NonNull String ingredients) {
        return Arrays.asList(ingredients
                .replaceAll("(\\r?\\n)+", "\n")
                .split("\n"));
    }

    @NonNull
    @Override
    public String formatOutput(@NonNull List<String> ingredients) {
        return String.join("\n", ingredients);
    }
}