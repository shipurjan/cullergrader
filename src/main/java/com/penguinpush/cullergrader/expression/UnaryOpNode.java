package com.penguinpush.cullergrader.expression;

/**
 * AST node representing a unary operation (currently only NOT).
 */
public class UnaryOpNode extends ASTNode {
    private final TokenType operator;
    private final ASTNode operand;

    public UnaryOpNode(TokenType operator, ASTNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public Object evaluate(EvaluationContext context) throws ExpressionException {
        Object value = operand.evaluate(context);

        if (operator == TokenType.NOT) {
            if (!(value instanceof Boolean)) {
                throw new ExpressionException("NOT operator requires boolean operand, got " + value.getClass().getSimpleName());
            }
            return !(Boolean) value;
        }

        throw new ExpressionException("Unknown unary operator: " + operator);
    }

    @Override
    public String toString() {
        return "Unary(" + operator + " " + operand + ")";
    }
}
