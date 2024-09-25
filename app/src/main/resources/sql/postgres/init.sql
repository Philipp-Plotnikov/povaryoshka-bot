-- Think to replace public with ? to dynamically replace
CREATE TABLE IF NOT EXISTS public.recipe (
    user_id bigint NOT NULL,
    dish_name text NOT NULL,
    recipe text NOT NULL,
    PRIMARY KEY (user_id, dish_name)
);

-- Think about indexes
-- Do I need to put primary or it is by default ?
CREATE TABLE IF NOT EXISTS public.ingredient (
    user_id bigint,
    dish_name text,
    ingredient text NOT NULL,
    PRIMARY KEY (user_id, dish_name),
    FOREIGN KEY (user_id, dish_name) REFERENCES recipe (user_id, dish_name) ON DELETE CASCADE
);