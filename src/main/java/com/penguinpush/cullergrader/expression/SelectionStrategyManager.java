package com.penguinpush.cullergrader.expression;

import com.penguinpush.cullergrader.config.AppConstants;
import com.penguinpush.cullergrader.logic.HashUtils;
import com.penguinpush.cullergrader.media.Photo;
import com.penguinpush.cullergrader.media.PhotoGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.penguinpush.cullergrader.utils.Logger.logMessage;

/**
 * Main facade for the expression system.
 * Manages aliases, caches compiled expressions, and coordinates tokenization, parsing, and evaluation.
 */
public class SelectionStrategyManager {
    // Predefined aliases for backwards compatibility
    private static final Map<String, String> ALIASES = new HashMap<String, String>();

    static {
        ALIASES.put("first", "index == 0");
        ALIASES.put("last", "index == length - 1");
        ALIASES.put("first_and_last", "index == 0 || index == length - 1");
        ALIASES.put("all", "true");
        ALIASES.put("none", "false");
    }

    // Cache of compiled expressions (key: expression string, value: AST)
    private final Map<String, ASTNode> compiledExpressions = new HashMap<String, ASTNode>();

    /**
     * Compiles an expression string (or resolves an alias) to an AST.
     * Results are cached for performance.
     *
     * @param strategy The strategy string (alias or expression)
     * @return The compiled AST
     * @throws ExpressionException if parsing fails
     */
    public ASTNode compileExpression(String strategy) throws ExpressionException {
        // Check cache first
        if (compiledExpressions.containsKey(strategy)) {
            return compiledExpressions.get(strategy);
        }

        // Resolve alias (case-insensitive)
        String expression = ALIASES.get(strategy.toLowerCase());
        if (expression == null) {
            expression = strategy;  // Not an alias, use as-is
        }

        // Tokenize
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokens = tokenizer.tokenize(expression);

        // Parse
        ExpressionParser parser = new ExpressionParser();
        ASTNode ast = parser.parse(tokens);

        // Cache the result
        compiledExpressions.put(strategy, ast);

        return ast;
    }

    /**
     * Evaluates a compiled expression for a specific photo.
     *
     * @param ast The compiled expression AST
     * @param photo The photo to evaluate
     * @param group The group containing the photo
     * @param selectedPhotos The set of already-selected photos (for stateful variables)
     * @return true if the photo should be selected, false otherwise
     */
    public boolean shouldSelectPhoto(ASTNode ast, Photo photo, PhotoGroup group, Set<Photo> selectedPhotos) {
        try {
            // Build evaluation context from photo metadata
            int index = photo.getIndex();
            int length = group.getSize();

            // Get deltaTime and similarity from metrics
            List<Float> metrics = photo.getMetrics();
            float deltaTime = metrics.size() > 0 ? metrics.get(0) : 0.0f;
            float similarity = metrics.size() > 1 ? metrics.get(1) : 0.0f;
            float maxGroupSimilarity = group.getMaxGroupSimilarity();

            // Calculate minDistanceToSelected
            float minDistanceToSelected = calculateMinDistanceToSelected(photo, selectedPhotos);

            EvaluationContext context = new EvaluationContext(
                index, length, deltaTime, similarity, maxGroupSimilarity, minDistanceToSelected
            );

            // Evaluate
            ExpressionEvaluator evaluator = new ExpressionEvaluator();
            return evaluator.evaluateBoolean(ast, context);

        } catch (ExpressionException e) {
            logMessage("Evaluation error for photo at index " + photo.getIndex() + ": " + e.getMessage());
            return false;  // Don't select on error
        }
    }

    /**
     * Computes the minimum Hamming distance (as percentage) from a photo to all selected photos.
     * Returns 100.0 (maximum distance) if no photos are selected yet.
     *
     * @param photo The photo to evaluate
     * @param selectedPhotos The set of already-selected photos
     * @return Minimum distance percentage (0-100)
     */
    private float calculateMinDistanceToSelected(Photo photo, Set<Photo> selectedPhotos) {
        if (selectedPhotos.isEmpty()) {
            return 100.0f;  // First photo has maximum "distance" from empty set
        }

        String currentHash = photo.getHash();
        int hashLength = AppConstants.HASHED_WIDTH * AppConstants.HASHED_HEIGHT * 3;

        float minDistance = 100.0f;

        for (Photo selectedPhoto : selectedPhotos) {
            int hammingDistance = HashUtils.hammingDistance(currentHash, selectedPhoto.getHash());
            float distancePercent = 100.0f * hammingDistance / hashLength;
            minDistance = Math.min(minDistance, distancePercent);
        }

        return minDistance;
    }

    /**
     * Gets the expression that an alias expands to (for documentation/debugging).
     *
     * @param alias The alias name
     * @return The expression, or null if not an alias
     */
    public static String getAliasExpansion(String alias) {
        return ALIASES.get(alias.toLowerCase());
    }

    /**
     * Checks if a strategy is a predefined alias.
     *
     * @param strategy The strategy name
     * @return true if it's an alias, false otherwise
     */
    public static boolean isAlias(String strategy) {
        return ALIASES.containsKey(strategy.toLowerCase());
    }
}
