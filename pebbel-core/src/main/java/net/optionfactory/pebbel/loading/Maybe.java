package net.optionfactory.pebbel.loading;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Like Optional<T> but allows null values.
 *
 * @author rferranti
 * @param <T>
 */
public class Maybe<T> {

    private final T value;
    private final boolean empty;

    public Maybe(T value, boolean empty) {
        this.value = value;
        this.empty = empty;
    }

    public T get() {
        if (empty) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(empty, value);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof Maybe == false) {
            return false;
        }
        final Maybe<?> other = (Maybe<?>) rhs;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        if (empty) {
            return "Nothing";
        }
        return String.format("Just(%s)", value);
    }

    public <U> Maybe<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return empty ? Maybe.nothing() : Maybe.just(mapper.apply(value));
    }

    public T orElse(T alt) {
        return empty ? alt : value;
    }

    public boolean isPresent() {
        return !empty;
    }

    public static <T> Maybe<T> just(T value) {
        return new Maybe<>(value, false);
    }

    public static <T> Maybe<T> nothing() {
        return new Maybe<>(null, true);
    }

}
