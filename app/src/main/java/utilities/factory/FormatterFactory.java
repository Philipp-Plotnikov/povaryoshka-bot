package utilities.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import utilities.IngredientsFormatter;


public class FormatterFactory {
    @Nullable
    private static IIngredientsFormatter ingredientsFormatter;

    @NonNull
    public static IIngredientsFormatter getIngredientsFormat() {
        if (ingredientsFormatter == null) {
            ingredientsFormatter = new IngredientsFormatter();
        }
        return ingredientsFormatter;
    }
}