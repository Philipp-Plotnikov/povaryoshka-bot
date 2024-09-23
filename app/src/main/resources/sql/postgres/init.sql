CREATE TABLE IF EXISTS recipe (
    user_id bigint NOT NULL,
    dish_name text NOT NULL,
    recipe text NOT NULL,
    PRIMARY KEY (user_id, dish_name)
);

CREATE TABLE IF EXISTS ingredient (
    user_id bigint
    dish_name text
    ingredient text NOT NULL
    FOREIGN KEY (user_id, dish_name) REFERENCES recipe (user_id, dish_name)
);