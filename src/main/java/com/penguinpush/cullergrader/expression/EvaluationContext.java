package com.penguinpush.cullergrader.expression;

/**
 * Holds the runtime variables available during expression evaluation.
 * These variables are populated from photo and group metadata.
 */
public class EvaluationContext {
    private final int index;          // 0-based photo position in group
    private final int length;         // Total photos in group
    private final float deltaTime;    // Seconds since previous photo
    private final float similarity;   // Similarity % to previous photo
    private final float maxGroupSimilarity;  // Max similarity in the group

    public EvaluationContext(int index, int length, float deltaTime, float similarity, float maxGroupSimilarity) {
        this.index = index;
        this.length = length;
        this.deltaTime = deltaTime;
        this.similarity = similarity;
        this.maxGroupSimilarity = maxGroupSimilarity;
    }

    public int getIndex() {
        return index;
    }

    public int getLength() {
        return length;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public float getSimilarity() {
        return similarity;
    }

    public float getMaxGroupSimilarity() {
        return maxGroupSimilarity;
    }

    @Override
    public String toString() {
        return String.format("EvaluationContext{index=%d, length=%d, deltaTime=%.2f, similarity=%.2f, maxGroupSimilarity=%.2f}",
                index, length, deltaTime, similarity, maxGroupSimilarity);
    }
}
