package com.penguinpush.cullergrader.media;

import com.penguinpush.cullergrader.config.AppConstants;
import static com.penguinpush.cullergrader.utils.Logger.logMessage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class PhotoGroup extends GridMedia {
    private final List<Photo> photos = new ArrayList<>();
    private final LinkedHashSet<Photo> selectedTakes = new LinkedHashSet<>();
    private float maxGroupSimilarity = 0.0f;

    @Override
    public BufferedImage getThumbnail() {
        // Always use first photo in group for thumbnail
        return photos.isEmpty() ? null : photos.get(0).getThumbnail();
    }

    @Override
    public String getName() {
        // Always use first photo in group for name
        return photos.isEmpty() ? "(empty group)" : photos.get(0).getFile().getName() + " (group)";
    }
    
    @Override
    public int getSize() {
        return photos.size();
    }
    
    @Override
    public String getTooltip() {
        return Integer.toString(getSize());
    }
    
    public void addPhoto(Photo photo) {
        photos.add(photo);
        photo.setGroup(this);
    }
    
    public void addPhotos(List<Photo> photos) {
        for (Photo photo : photos) {
            this.photos.add(photo);
            photo.setGroup(this);
        }
    }
    
    public boolean removePhoto(Photo photo) {
        boolean removed = photos.remove(photo);

        if (removed) {
            photo.setGroup(null);
            // Remove from selected takes (allow 0 selections)
            selectedTakes.remove(photo);
        }

        return removed;
    }

    public List<Photo> getPhotos() {
        return Collections.unmodifiableList(photos); // read-only list
    }

    // New selection methods
    public Set<Photo> getSelectedTakes() {
        return Collections.unmodifiableSet(selectedTakes);
    }

    public boolean isSelected(Photo photo) {
        return selectedTakes.contains(photo);
    }

    public boolean addSelectedTake(Photo photo) {
        if (photos.contains(photo)) {
            return selectedTakes.add(photo);
        }
        return false;
    }

    public boolean removeSelectedTake(Photo photo) {
        return selectedTakes.remove(photo);
    }

    public void toggleSelection(Photo photo) {
        if (isSelected(photo)) {
            removeSelectedTake(photo);
        } else {
            addSelectedTake(photo);
        }
    }

    public void clearSelections() {
        selectedTakes.clear();
    }

    public float getMaxGroupSimilarity() {
        return maxGroupSimilarity;
    }

    public void setMaxGroupSimilarity(float maxGroupSimilarity) {
        this.maxGroupSimilarity = maxGroupSimilarity;
    }

    public void applyDefaultSelectionStrategy() {
        String strategy = AppConstants.SELECTION_STRATEGY;

        try {
            com.penguinpush.cullergrader.expression.SelectionStrategyManager manager =
                new com.penguinpush.cullergrader.expression.SelectionStrategyManager();
            com.penguinpush.cullergrader.expression.ASTNode ast = manager.compileExpression(strategy);

            for (Photo photo : photos) {
                if (manager.shouldSelectPhoto(ast, photo, this, selectedTakes)) {
                    selectedTakes.add(photo);
                }
            }

        } catch (Exception e) {
            // Log error and fallback to first photo
            logMessage("Expression error in strategy '" + strategy + "': " + e.getMessage() + ", falling back to 'first'");

            if (!photos.isEmpty()) {
                selectedTakes.add(photos.get(0));
            }
        }
    }

    /**
     * Optimized version that reuses a pre-compiled expression.
     * This avoids recompiling the same expression for every group.
     *
     * @param manager The strategy manager
     * @param compiledStrategy The pre-compiled expression AST (can be null)
     */
    public void applyDefaultSelectionStrategy(
            com.penguinpush.cullergrader.expression.SelectionStrategyManager manager,
            com.penguinpush.cullergrader.expression.ASTNode compiledStrategy) {

        // Fallback to original method if parameters are null
        if (manager == null || compiledStrategy == null) {
            applyDefaultSelectionStrategy();
            return;
        }

        try {
            for (Photo photo : photos) {
                if (manager.shouldSelectPhoto(compiledStrategy, photo, this, selectedTakes)) {
                    selectedTakes.add(photo);
                }
            }

        } catch (Exception e) {
            logMessage("Evaluation error: " + e.getMessage() + ", using fallback");
            if (!photos.isEmpty()) {
                selectedTakes.add(photos.get(0));
            }
        }
    }
}
