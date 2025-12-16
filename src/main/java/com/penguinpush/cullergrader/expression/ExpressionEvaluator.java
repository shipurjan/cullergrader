package com.penguinpush.cullergrader.expression;

/**
 * Evaluates an AST (Abstract Syntax Tree) in a given context.
 * Ensures that the final result is a boolean value.
 */
public class ExpressionEvaluator {
    /**
     * Evaluates an AST and returns a boolean result.
     * @param root The root node of the AST
     * @param context The evaluation context containing variable values
     * @return The boolean result of the expression
     * @throws ExpressionException if evaluation fails or result is not boolean
     */
    public boolean evaluateBoolean(ASTNode root, EvaluationContext context) throws ExpressionException {
        Object result = root.evaluate(context);

        if (!(result instanceof Boolean)) {
            throw new ExpressionException("Expression must evaluate to boolean, got " + result.getClass().getSimpleName());
        }

        return (Boolean) result;
    }
}
