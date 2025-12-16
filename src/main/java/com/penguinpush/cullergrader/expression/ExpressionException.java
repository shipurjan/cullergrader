package com.penguinpush.cullergrader.expression;

/**
 * Exception thrown during expression parsing or evaluation.
 * Includes position information for better error messages.
 */
public class ExpressionException extends Exception {
    private final int position;

    public ExpressionException(String message) {
        super(message);
        this.position = -1;
    }

    public ExpressionException(String message, int position) {
        super(message + " at position " + position);
        this.position = position;
    }

    public ExpressionException(String message, Throwable cause) {
        super(message, cause);
        this.position = -1;
    }

    public int getPosition() {
        return position;
    }
}
