package com.penguinpush.cullergrader.expression;

/**
 * Enumeration of all token types supported by the expression language.
 * Tokens are ordered by their precedence level for parsing.
 */
public enum TokenType {
    // Literals and keywords
    TRUE,           // true
    FALSE,          // false
    NUMBER,         // 123, 45.67

    // Variables
    INDEX,          // index (0-based photo position)
    LENGTH,         // length (total photos in group)
    DELTA_TIME,     // deltaTime (seconds since previous photo)
    SIMILARITY,     // similarity (% similarity to previous photo)
    MAX_GROUP_SIMILARITY,  // maxGroupSimilarity (max similarity in group)
    MIN_DISTANCE_TO_SELECTED,  // minDistanceToSelected (min % distance to selected photos)

    // Grouping
    LPAREN,         // (
    RPAREN,         // )

    // Unary operators
    NOT,            // !

    // Multiplicative operators (higher precedence)
    MULTIPLY,       // *
    DIVIDE,         // /
    MODULO,         // %

    // Additive operators
    PLUS,           // +
    MINUS,          // -

    // Comparison operators
    LT,             // <
    LE,             // <=
    GT,             // >
    GE,             // >=

    // Equality operators
    EQ,             // ==
    NE,             // !=

    // Logical operators
    AND,            // &&
    OR,             // ||

    // Ternary operator
    QUESTION,       // ?
    COLON,          // :

    // End of input
    EOF             // End of file/expression
}
