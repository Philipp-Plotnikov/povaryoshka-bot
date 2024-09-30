CREATE TABLE IF NOT EXISTS public.recipe (
    user_id bigint NOT NULL,
    dish_name text NOT NULL,
    recipe text,
    PRIMARY KEY (user_id, dish_name)
);

CREATE TABLE IF NOT EXISTS public.ingredient (
    user_id bigint NOT NULL,
    dish_name text NOT NULL,
    ingredient text,
    PRIMARY KEY (user_id, dish_name),
    FOREIGN KEY (user_id, dish_name) REFERENCES recipe (user_id, dish_name) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.feedback (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id bigint NOT NULL,
    created_at timestamp with timezone AT TIME ZONE 'UTC' DEFAULT now(),
    feedback text
)