package ru.exyon.telegrambot.core;

public interface MessageFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}
