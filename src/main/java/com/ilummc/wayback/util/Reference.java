package com.ilummc.wayback.util;

public class Reference<T> {

    private T value;

    public Reference(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public Reference<T> setValue(T value) {
        this.value = value;
        return this;
    }

    public static <T> Reference<T> of(T val) {
        return new Reference<>(val);
    }

}
