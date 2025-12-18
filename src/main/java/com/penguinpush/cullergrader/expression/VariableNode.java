package com.penguinpush.cullergrader.expression;

/**
 * AST node representing a variable reference (index, length, deltaTime, similarity).
 */
public class VariableNode extends ASTNode {
    private final TokenType variable;

    public VariableNode(TokenType variable) {
        this.variable = variable;
    }

    @Override
    public Object evaluate(EvaluationContext context) throws ExpressionException {
        switch (variable) {
            case INDEX:
                return context.getIndex();
            case LENGTH:
                return context.getLength();
            case DELTA_TIME:
                return context.getDeltaTime();
            case SIMILARITY:
                return context.getSimilarity();
            case MAX_GROUP_SIMILARITY:
                return context.getMaxGroupSimilarity();
            case MIN_DISTANCE_TO_SELECTED:
                return context.getMinDistanceToSelected();
            default:
                throw new ExpressionException("Unknown variable: " + variable);
        }
    }

    @Override
    public String toString() {
        return "Variable(" + variable + ")";
    }
}
