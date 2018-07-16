package net.optionfactory.pebbel.results;

import java.util.Optional;

public class Problem {

    public final String type;
    public final String reason;
    public final Object details;
    public final Optional<Throwable> cause;

    public Problem(String type, String reason, Object details, Optional<Throwable> cause) {
        this.type = type;
        this.reason = reason;
        this.details = details;
        this.cause = cause;
    }

    @Override
    public String toString() {
        return String.format("%s: %s (%s)", type, reason, details);
    }

    public static Problem of(String type, String reason, Object details) {
        return new Problem(type, reason, details, Optional.empty());
    }

    public static Problem of(String type, String reason, Object details, Throwable throwable) {
        return new Problem(type, reason, details, Optional.of(throwable));
    }
}
