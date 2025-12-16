package com.penguinpush.cullergrader.expression;

/**
 * Represents a single token in an expression.
 * Contains the token type, original text (lexeme), literal value, and position for error reporting.
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object literal;  // For NUMBER tokens
    private final int position;     // Character position in source expression

    public Token(TokenType type, String lexeme, Object literal, int position) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.position = position;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        if (literal != null) {
            return type + " '" + lexeme + "' (" + literal + ")";
        }
        return type + " '" + lexeme + "'";
    }
}
