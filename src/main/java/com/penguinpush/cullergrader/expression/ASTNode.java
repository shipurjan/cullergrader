package com.penguinpush.cullergrader.expression;

/**
 * Abstract base class for all AST (Abstract Syntax Tree) nodes.
 * Each node represents a part of the parsed expression and can evaluate itself.
 */
public abstract class ASTNode {
    /**
     * Evaluates this node in the given context.
     * @param context The runtime context containing variable values
     * @return The result of evaluation (Boolean, Integer, or Float)
     * @throws ExpressionException if evaluation fails
     */
    public abstract Object evaluate(EvaluationContext context) throws ExpressionException;
}
