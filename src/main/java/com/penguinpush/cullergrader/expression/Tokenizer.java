package com.penguinpush.cullergrader.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tokenizer (Lexer) for expression language.
 * Converts expression string into a list of tokens.
 */
public class Tokenizer {
    private static final Map<String, TokenType> KEYWORDS = new HashMap<String, TokenType>();

    static {
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("index", TokenType.INDEX);
        KEYWORDS.put("length", TokenType.LENGTH);
        KEYWORDS.put("deltaTime", TokenType.DELTA_TIME);
        KEYWORDS.put("similarity", TokenType.SIMILARITY);
    }

    private String source;
    private int current = 0;
    private int start = 0;
    private List<Token> tokens = new ArrayList<Token>();

    public List<Token> tokenize(String expression) throws ExpressionException {
        this.source = expression;
        this.current = 0;
        this.start = 0;
        this.tokens = new ArrayList<Token>();

        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, current));
        return tokens;
    }

    private void scanToken() throws ExpressionException {
        char c = advance();

        switch (c) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                // Skip whitespace
                break;

            case '(':
                addToken(TokenType.LPAREN);
                break;
            case ')':
                addToken(TokenType.RPAREN);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '*':
                addToken(TokenType.MULTIPLY);
                break;
            case '/':
                addToken(TokenType.DIVIDE);
                break;
            case '%':
                addToken(TokenType.MODULO);
                break;
            case '?':
                addToken(TokenType.QUESTION);
                break;
            case ':':
                addToken(TokenType.COLON);
                break;

            case '!':
                addToken(match('=') ? TokenType.NE : TokenType.NOT);
                break;
            case '=':
                if (match('=')) {
                    addToken(TokenType.EQ);
                } else {
                    throw new ExpressionException("Unexpected character '=' (did you mean '=='?)", start);
                }
                break;
            case '<':
                addToken(match('=') ? TokenType.LE : TokenType.LT);
                break;
            case '>':
                addToken(match('=') ? TokenType.GE : TokenType.GT);
                break;
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    throw new ExpressionException("Unexpected character '&' (did you mean '&&'?)", start);
                }
                break;
            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                } else {
                    throw new ExpressionException("Unexpected character '|' (did you mean '||'?)", start);
                }
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new ExpressionException("Unexpected character '" + c + "'", start);
                }
        }
    }

    private void number() throws ExpressionException {
        while (isDigit(peek())) {
            advance();
        }

        // Check for decimal point
        if (peek() == '.' && isDigit(peekNext())) {
            advance();  // Consume the '.'
            while (isDigit(peek())) {
                advance();
            }

            // Parse as float
            String text = source.substring(start, current);
            try {
                float value = Float.parseFloat(text);
                addToken(TokenType.NUMBER, value);
            } catch (NumberFormatException e) {
                throw new ExpressionException("Invalid number format: " + text, start);
            }
        } else {
            // Parse as integer
            String text = source.substring(start, current);
            try {
                int value = Integer.parseInt(text);
                addToken(TokenType.NUMBER, value);
            } catch (NumberFormatException e) {
                throw new ExpressionException("Invalid number format: " + text, start);
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = KEYWORDS.get(text);

        if (type == null) {
            // Unknown identifier - will be caught by parser
            throw new RuntimeException("Unknown identifier: " + text);
        }

        addToken(type);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, start));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
