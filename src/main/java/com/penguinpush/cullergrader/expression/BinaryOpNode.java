package com.penguinpush.cullergrader.expression;

/**
 * AST node representing a binary operation (arithmetic, comparison, logical).
 * Implements short-circuit evaluation for AND and OR operators.
 */
public class BinaryOpNode extends ASTNode {
    private final TokenType operator;
    private final ASTNode left;
    private final ASTNode right;

    public BinaryOpNode(TokenType operator, ASTNode left, ASTNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object evaluate(EvaluationContext context) throws ExpressionException {
        // Short-circuit evaluation for logical operators
        if (operator == TokenType.AND) {
            Object leftVal = left.evaluate(context);
            if (!(leftVal instanceof Boolean)) {
                throw new ExpressionException("AND operator requires boolean operands");
            }
            if (!(Boolean) leftVal) {
                return false;  // Short-circuit: don't evaluate right
            }
            Object rightVal = right.evaluate(context);
            if (!(rightVal instanceof Boolean)) {
                throw new ExpressionException("AND operator requires boolean operands");
            }
            return (Boolean) rightVal;
        }

        if (operator == TokenType.OR) {
            Object leftVal = left.evaluate(context);
            if (!(leftVal instanceof Boolean)) {
                throw new ExpressionException("OR operator requires boolean operands");
            }
            if ((Boolean) leftVal) {
                return true;  // Short-circuit: don't evaluate right
            }
            Object rightVal = right.evaluate(context);
            if (!(rightVal instanceof Boolean)) {
                throw new ExpressionException("OR operator requires boolean operands");
            }
            return (Boolean) rightVal;
        }

        // For all other operators, evaluate both sides
        Object leftVal = left.evaluate(context);
        Object rightVal = right.evaluate(context);

        // Arithmetic operators
        switch (operator) {
            case PLUS:
                return add(leftVal, rightVal);
            case MINUS:
                return subtract(leftVal, rightVal);
            case MULTIPLY:
                return multiply(leftVal, rightVal);
            case DIVIDE:
                return divide(leftVal, rightVal);
            case MODULO:
                return modulo(leftVal, rightVal);
        }

        // Comparison operators
        switch (operator) {
            case LT:
                return compare(leftVal, rightVal) < 0;
            case LE:
                return compare(leftVal, rightVal) <= 0;
            case GT:
                return compare(leftVal, rightVal) > 0;
            case GE:
                return compare(leftVal, rightVal) >= 0;
            case EQ:
                return equals(leftVal, rightVal);
            case NE:
                return !equals(leftVal, rightVal);
        }

        throw new ExpressionException("Unknown binary operator: " + operator);
    }

    private Object add(Object left, Object right) throws ExpressionException {
        if (left instanceof Number && right instanceof Number) {
            if (left instanceof Float || right instanceof Float) {
                return ((Number) left).floatValue() + ((Number) right).floatValue();
            }
            return ((Number) left).intValue() + ((Number) right).intValue();
        }
        throw new ExpressionException("Cannot add " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private Object subtract(Object left, Object right) throws ExpressionException {
        if (left instanceof Number && right instanceof Number) {
            if (left instanceof Float || right instanceof Float) {
                return ((Number) left).floatValue() - ((Number) right).floatValue();
            }
            return ((Number) left).intValue() - ((Number) right).intValue();
        }
        throw new ExpressionException("Cannot subtract " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private Object multiply(Object left, Object right) throws ExpressionException {
        if (left instanceof Number && right instanceof Number) {
            if (left instanceof Float || right instanceof Float) {
                return ((Number) left).floatValue() * ((Number) right).floatValue();
            }
            return ((Number) left).intValue() * ((Number) right).intValue();
        }
        throw new ExpressionException("Cannot multiply " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private Object divide(Object left, Object right) throws ExpressionException {
        if (left instanceof Number && right instanceof Number) {
            float rightVal = ((Number) right).floatValue();
            if (rightVal == 0) {
                throw new ExpressionException("Division by zero");
            }
            return ((Number) left).floatValue() / rightVal;
        }
        throw new ExpressionException("Cannot divide " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private Object modulo(Object left, Object right) throws ExpressionException {
        if (left instanceof Number && right instanceof Number) {
            int rightVal = ((Number) right).intValue();
            if (rightVal == 0) {
                throw new ExpressionException("Modulo by zero");
            }
            return ((Number) left).intValue() % rightVal;
        }
        throw new ExpressionException("Cannot modulo " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private int compare(Object left, Object right) throws ExpressionException {
        if (left instanceof Number && right instanceof Number) {
            float leftVal = ((Number) left).floatValue();
            float rightVal = ((Number) right).floatValue();
            return Float.compare(leftVal, rightVal);
        }
        throw new ExpressionException("Cannot compare " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private boolean equals(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).floatValue() == ((Number) right).floatValue();
        }
        if (left instanceof Boolean && right instanceof Boolean) {
            return left.equals(right);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Binary(" + left + " " + operator + " " + right + ")";
    }
}
