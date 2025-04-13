package utilities.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import utilities.IngredientsFormatter;


final public class FormatterFactory {
    @Nullable
    private static IIngredientsFormatter ingredientsFormatter;

    @NonNull
    public static IIngredientsFormatter createIngredientsFormat() {
        if (ingredientsFormatter == null) {
            ingredientsFormatter = new IngredientsFormatter();
        }
        return ingredientsFormatter;
    }
}