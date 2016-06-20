package org.jsonstat.v2;

/**
 * Thrown when trying to add a dimension twice.
 */
public class DuplicateDimensionException extends RuntimeException {

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public DuplicateDimensionException(String message) {
        super(message);
    }
}
