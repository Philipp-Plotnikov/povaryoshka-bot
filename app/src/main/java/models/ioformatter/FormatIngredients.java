package models.ioformatter;

import models.ioformatter.base.Formatter;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.Arrays;
import java.util.List;

public class FormatIngredients extends Formatter {

    @Override
    @NonNull
    public List<String> formatInput (@NonNull String ingredients){
        return Arrays.asList(ingredients
                .replaceAll("(\\r?\\n)+", "\n")
                .split("\n"));
    }

    @Override
    @NonNull
    public String formatOutput (@NonNull List<String> ingredients){
        return String.join("\n", ingredients);
    }
}
