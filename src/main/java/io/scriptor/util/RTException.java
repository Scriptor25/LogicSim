package io.scriptor.util;

import org.jetbrains.annotations.NotNull;

public class RTException extends RuntimeException {

    public RTException() {
        super();
    }

    public RTException(final @NotNull String message) {
        super(message);
    }

    public RTException(final @NotNull String format, final @NotNull Object @NotNull ... args) {
        super(format.formatted(args));
    }

    public RTException(final @NotNull Throwable cause) {
        super(cause);
    }

    public RTException(final @NotNull Throwable cause, final @NotNull String message) {
        super(message, cause);
    }

    public RTException(final @NotNull Throwable cause, final @NotNull String format, final @NotNull Object @NotNull ... args) {
        super(format.formatted(args), cause);
    }
}
