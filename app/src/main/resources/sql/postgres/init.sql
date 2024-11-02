DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'multi_state_command_types') THEN
        CREATE TYPE multi_state_command_types AS ENUM (
    		'create',
    		'get',
    		'update',
    		'delete',
    		'feedback',
    		'help'
		);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'command_states') THEN
        CREATE TYPE command_states AS ENUM (
            'dish_name',
            'ingredients',
            'recipe',
            'feedback'
        );
    END IF;
END
$$;

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
    PRIMARY KEY (user_id, dish_name, ingredient),
    FOREIGN KEY (user_id, dish_name) REFERENCES recipe (user_id, dish_name) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.user_context (
    user_id bigint NOT NULL,
    multi_state_command_type multi_state_command_types NOT NULL,
    command_state command_states NOT NULL,
    dish_name text,
    PRIMARY KEY (user_id),
    FOREIGN KEY (dish_name) REFERENCES recipe (dish_name) ON DELETE CASCADE 
);

CREATE TABLE IF NOT EXISTS public.feedback (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    user_id bigint NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    feedback text NOT NULL,
    PRIMARY KEY (id)
);
