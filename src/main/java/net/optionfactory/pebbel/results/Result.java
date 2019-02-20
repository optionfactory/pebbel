package net.optionfactory.pebbel.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Result of a computation. Can be _either_ a value or an error.
 *
 * @author rferranti
 * @param <V> the result type
 */
public class Result<V> {

    private final List<Problem> errors;
    private final V value;


    public Result(List<Problem> errors, V value) {
        this.errors = errors;
        this.value = value;
    }

    public static <ValueType> Result<ValueType> errors(List<Problem> error) {
        return new Result<>(error, null);
    }

    public static <ValueType> Result<ValueType> error(Problem error) {
        final List<Problem> errors = new ArrayList<>();
        errors.add(error);
        return new Result<>(errors, null);
    }

    public static <ValueType> Result<ValueType> value(ValueType result) {
        return new Result<>(Collections.emptyList(), result);
    }

    public List<Problem> getErrors() {
        return errors;
    }

    public boolean isError() {
        return !errors.isEmpty();
    }

    public V getValue() {
        return value;
    }

    public <R> Result<R> mapErrors() {
        if (errors.isEmpty()) {
            throw new IllegalStateException("cannot call mapErrors on a valued result");
        }
        return Result.errors(errors);
    }

    public static List<Problem> problems(Result<?> first, Result<?>... others) {
        return Stream.concat(Stream.of(first), Stream.of(others))
                .flatMap(r -> r.getErrors().stream())
                .collect(Collectors.toList());
    }
}
