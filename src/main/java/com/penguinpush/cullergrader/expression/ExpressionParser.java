package com.penguinpush.cullergrader.expression;

import java.util.List;

/**
 * Recursive descent parser for expression language.
 * Converts a list of tokens into an Abstract Syntax Tree (AST).
 *
 * Operator precedence (lowest to highest):
 * 1. Ternary (? :)
 * 2. OR (||)
 * 3. AND (&&)
 * 4. Equality (==, !=)
 * 5. Comparison (<, >, <=, >=)
 * 6. Addition (+, -)
 * 7. Multiplication (*, /, %)
 * 8. Unary (!)
 * 9. Primary (literals, variables, parentheses)
 */
public class ExpressionParser {
    private List<Token> tokens;
    private int current = 0;

    public ASTNode parse(List<Token> tokens) throws ExpressionException {
        this.tokens = tokens;
        this.current = 0;

        ASTNode result = parseTernary();

        // Ensure we consumed all tokens
        if (!isAtEnd()) {
            Token unexpected = peek();
            throw new ExpressionException("Unexpected token: " + unexpected.getLexeme(), unexpected.getPosition());
        }

        return result;
    }

    // Ternary: condition ? trueValue : falseValue
    private ASTNode parseTernary() throws ExpressionException {
        ASTNode expr = parseOr();

        if (match(TokenType.QUESTION)) {
            ASTNode trueValue = parseTernary();
            if (!match(TokenType.COLON)) {
                throw new ExpressionException("Expected ':' after true value in ternary expression", peek().getPosition());
            }
            ASTNode falseValue = parseTernary();
            return new TernaryOpNode(expr, trueValue, falseValue);
        }

        return expr;
    }

    // OR: expr || expr
    private ASTNode parseOr() throws ExpressionException {
        ASTNode left = parseAnd();

        while (match(TokenType.OR)) {
            TokenType operator = previous().getType();
            ASTNode right = parseAnd();
            left = new BinaryOpNode(operator, left, right);
        }

        return left;
    }

    // AND: expr && expr
    private ASTNode parseAnd() throws ExpressionException {
        ASTNode left = parseEquality();

        while (match(TokenType.AND)) {
            TokenType operator = previous().getType();
            ASTNode right = parseEquality();
            left = new BinaryOpNode(operator, left, right);
        }

        return left;
    }

    // Equality: expr == expr, expr != expr
    private ASTNode parseEquality() throws ExpressionException {
        ASTNode left = parseComparison();

        while (match(TokenType.EQ, TokenType.NE)) {
            TokenType operator = previous().getType();
            ASTNode right = parseComparison();
            left = new BinaryOpNode(operator, left, right);
        }

        return left;
    }

    // Comparison: expr < expr, expr > expr, expr <= expr, expr >= expr
    private ASTNode parseComparison() throws ExpressionException {
        ASTNode left = parseAddition();

        while (match(TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE)) {
            TokenType operator = previous().getType();
            ASTNode right = parseAddition();
            left = new BinaryOpNode(operator, left, right);
        }

        return left;
    }

    // Addition: expr + expr, expr - expr
    private ASTNode parseAddition() throws ExpressionException {
        ASTNode left = parseMultiplication();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            TokenType operator = previous().getType();
            ASTNode right = parseMultiplication();
            left = new BinaryOpNode(operator, left, right);
        }

        return left;
    }

    // Multiplication: expr * expr, expr / expr, expr % expr
    private ASTNode parseMultiplication() throws ExpressionException {
        ASTNode left = parseUnary();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            TokenType operator = previous().getType();
            ASTNode right = parseUnary();
            left = new BinaryOpNode(operator, left, right);
        }

        return left;
    }

    // Unary: !expr
    private ASTNode parseUnary() throws ExpressionException {
        if (match(TokenType.NOT)) {
            TokenType operator = previous().getType();
            ASTNode operand = parseUnary();
            return new UnaryOpNode(operator, operand);
        }

        return parsePrimary();
    }

    // Primary: literals, variables, parentheses
    private ASTNode parsePrimary() throws ExpressionException {
        // Boolean literals
        if (match(TokenType.TRUE)) {
            return new LiteralNode(true);
        }
        if (match(TokenType.FALSE)) {
            return new LiteralNode(false);
        }

        // Numbers
        if (match(TokenType.NUMBER)) {
            return new LiteralNode(previous().getLiteral());
        }

        // Variables
        if (match(TokenType.INDEX, TokenType.LENGTH, TokenType.DELTA_TIME, TokenType.SIMILARITY, TokenType.MAX_GROUP_SIMILARITY)) {
            return new VariableNode(previous().getType());
        }

        // Parentheses
        if (match(TokenType.LPAREN)) {
            ASTNode expr = parseTernary();
            if (!match(TokenType.RPAREN)) {
                throw new ExpressionException("Expected ')' after expression", peek().getPosition());
            }
            return expr;
        }

        // Unexpected token
        Token token = peek();
        throw new ExpressionException("Unexpected token: " + token.getLexeme(), token.getPosition());
    }

    // Helper methods

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
