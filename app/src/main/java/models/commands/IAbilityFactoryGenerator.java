package models.commands;

import org.checkerframework.checker.nullness.qual.NonNull;

import telegram.abilities.factory.IAbilityFactory;


@FunctionalInterface
public interface IAbilityFactoryGenerator {
    @NonNull
    IAbilityFactory generate() throws Exception;
}