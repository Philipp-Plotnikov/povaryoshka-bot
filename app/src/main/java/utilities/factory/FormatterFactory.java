package utilities.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import utilities.FormatIngredients;

public class FormatterFactory {

    @Nullable
    private static IngredientsFormatter ingredientsFormatter;

    private FormatterFactory() {}

    @NonNull
    public static IngredientsFormatter getIngredientsFormat() {
        if (ingredientsFormatter == null) {
            ingredientsFormatter = new FormatIngredients();
        }
        return ingredientsFormatter;
    }
}