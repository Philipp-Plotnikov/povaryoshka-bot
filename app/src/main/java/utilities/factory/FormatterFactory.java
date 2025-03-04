package utilities.factory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import utilities.IngredientsFormatter;

public class FormatterFactory {

    @Nullable
    private static IIngredientsFormatter iIngredientsFormatter;

    private FormatterFactory() {}

    @NonNull
    public static IIngredientsFormatter getIngredientsFormat() {
        if (iIngredientsFormatter == null) {
            iIngredientsFormatter = new IngredientsFormatter();
        }
        return iIngredientsFormatter;
    }
}