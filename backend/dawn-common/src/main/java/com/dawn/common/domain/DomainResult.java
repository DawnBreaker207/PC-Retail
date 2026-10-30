package com.dawn.common.domain;

public sealed interface DomainResult<T>
        permits DomainResult.Success, DomainResult.Failure {

    record Success<T>(T value) implements DomainResult<T> {}

    record Failure<T>(String code, String message) implements DomainResult<T> {}

    static <T> DomainResult<T> ok(T value) {
        return new Success<>(value);
    }

    static <T> DomainResult<T> fail(String code, String message) {
        return new Failure<>(code, message);
    }

    default boolean isSuccess() {
        return this instanceof Success;
    }

    default T valueOrThrow() {
        return switch (this) {
            case Success<T> s -> s.value();
            case Failure<T> f -> throw new IllegalStateException(
                    "DomainResult.Failure: " + f.code() + " — " + f.message());
        };
    }
}
