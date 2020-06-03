package net.optionfactory.pebbel.results;

public class LanguageException extends RuntimeException {

    public final String type;

    public LanguageException(String type, String reason) {
        super(reason);
        this.type = type;
    }
}
