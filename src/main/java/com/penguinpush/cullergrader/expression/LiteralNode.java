package com.penguinpush.cullergrader.expression;

/**
 * AST node representing a literal value (number or boolean).
 */
public class LiteralNode extends ASTNode {
    private final Object value;

    public LiteralNode(Object value) {
        this.value = value;
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        return value;
    }

    @Override
    public String toString() {
        return "Literal(" + value + ")";
    }
}
