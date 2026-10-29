package net.montoyo.wd.registry;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Holds a value that has been registered into a vanilla registry.
 */
public class RegistryObject<T> implements Supplier<T> {
    private final T value;

    RegistryObject(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public T orElseGet(T other) {
        return value != null ? value : other;
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }
}
