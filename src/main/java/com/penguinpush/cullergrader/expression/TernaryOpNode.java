package com.penguinpush.cullergrader.expression;

/**
 * AST node representing a ternary conditional operation (condition ? trueValue : falseValue).
 */
public class TernaryOpNode extends ASTNode {
    private final ASTNode condition;
    private final ASTNode trueValue;
    private final ASTNode falseValue;

    public TernaryOpNode(ASTNode condition, ASTNode trueValue, ASTNode falseValue) {
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    @Override
    public Object evaluate(EvaluationContext context) throws ExpressionException {
        Object conditionResult = condition.evaluate(context);

        if (!(conditionResult instanceof Boolean)) {
            throw new ExpressionException("Ternary condition must be boolean, got " + conditionResult.getClass().getSimpleName());
        }

        if ((Boolean) conditionResult) {
            return trueValue.evaluate(context);
        } else {
            return falseValue.evaluate(context);
        }
    }

    @Override
    public String toString() {
        return "Ternary(" + condition + " ? " + trueValue + " : " + falseValue + ")";
    }
}
