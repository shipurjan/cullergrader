package com.penguinpush.cullergrader.logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.penguinpush.cullergrader.config.AppConstants;
import com.penguinpush.cullergrader.media.Photo;
import com.penguinpush.cullergrader.media.PhotoGroup;
import static com.penguinpush.cullergrader.utils.Logger.logMessage;

public class FileUtils {

    public static List<PhotoGroup> loadFolder(File path, GroupingEngine groupingEngine, float timestampThreshold, float similarityThreshold) {
        List<Photo> photos = groupingEngine.photoListFromFolder(path);

        return groupingEngine.generateGroups(photos, timestampThreshold, similarityThreshold);
    }

    public static void exportBestTakes(List<PhotoGroup> photoGroups, File targetFolder) {
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        Map<String, Integer> filenameCounts = new HashMap<>();

        for (PhotoGroup group : photoGroups) {
            Set<Photo> selectedPhotos = group.getSelectedTakes();

            // Skip groups with 0 selections
            if (selectedPhotos.isEmpty()) {
                continue;
            }

            for (Photo photo : selectedPhotos) {
                File sourceFile = photo.getFile();
                String originalName = sourceFile.getName();
                String baseName = getBaseName(originalName);
                String extension = getExtension(originalName);

                // Handle filename collisions
                String finalName = originalName;
                if (filenameCounts.containsKey(originalName)) {
                    int count = filenameCounts.get(originalName);
                    finalName = baseName + "_" + count + extension;
                    filenameCounts.put(originalName, count + 1);
                } else {
                    filenameCounts.put(originalName, 1);
                }

                File destinationFile = new File(targetFolder, finalName);

                try {
                    Files.copy(sourceFile.toPath(), destinationFile.toPath(),
                              StandardCopyOption.REPLACE_EXISTING);
                    logMessage("copied file: " + sourceFile.getAbsolutePath() + " → " + finalName);
                } catch (IOException e) {
                    logMessage("couldn't copy file: " + sourceFile.getAbsolutePath());
                }
            }
        }
    }

    private static String getBaseName(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(0, lastDot) : filename;
    }

    private static String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    public static void exportGroupsJson(List<PhotoGroup> photoGroups, File jsonFile,
                                         float timeThreshold, float similarityThreshold) {
        // Create parent directories if needed
        File parentDir = jsonFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Build JSON structure
        Map<String, Object> root = new HashMap<>();

        // Add version and full config for reproducibility
        root.put("version", AppConstants.VERSION);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("DARK_THEME", AppConstants.DARK_THEME);
        configMap.put("CACHE_FILE", AppConstants.CACHE_FILE);
        configMap.put("LOG_DIRECTORY", AppConstants.LOG_DIRECTORY);
        configMap.put("DEFAULT_FOLDER_PATH", AppConstants.DEFAULT_FOLDER_PATH);
        configMap.put("HASHING_ENABLED", AppConstants.HASHING_ENABLED);
        configMap.put("EXECUTOR_TIMEOUT_MINUTES", AppConstants.EXECUTOR_TIMEOUT_MINUTES);
        configMap.put("MAX_THREADS_RATIO", AppConstants.MAX_THREADS_RATIO);
        configMap.put("HASHED_WIDTH", AppConstants.HASHED_WIDTH);
        configMap.put("HASHED_HEIGHT", AppConstants.HASHED_HEIGHT);
        configMap.put("TIME_THRESHOLD_SECONDS", AppConstants.TIME_THRESHOLD_SECONDS);
        configMap.put("SELECTION_STRATEGY", AppConstants.SELECTION_STRATEGY);
        configMap.put("SIMILARITY_THRESHOLD_PERCENT", AppConstants.SIMILARITY_THRESHOLD_PERCENT);
        configMap.put("IMAGE_PREVIEW_CACHE_SIZE_MB", AppConstants.IMAGE_PREVIEW_CACHE_SIZE_MB);
        root.put("config", configMap);

        root.put("totalGroups", photoGroups.size());
        root.put("totalPhotos", photoGroups.stream().mapToInt(PhotoGroup::getSize).sum());
        root.put("selectedPhotos", photoGroups.stream().mapToInt(g -> g.getSelectedTakes().size()).sum());
        root.put("exportTimestamp", System.currentTimeMillis());

        List<Map<String, Object>> groupsList = new ArrayList<>();
        for (PhotoGroup group : photoGroups) {
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("groupIndex", group.getIndex());
            groupMap.put("photoCount", group.getSize());
            groupMap.put("maxGroupSimilarity", group.getMaxGroupSimilarity());

            Set<Photo> selectedTakes = group.getSelectedTakes();
            List<String> selectedFilenames = new ArrayList<>();
            for (Photo photo : selectedTakes) {
                selectedFilenames.add(photo.getFile().getName());
            }
            groupMap.put("selectedTakes", selectedFilenames);

            List<Map<String, Object>> photosList = new ArrayList<>();
            for (Photo photo : group.getPhotos()) {
                Map<String, Object> photoMap = new HashMap<>();
                photoMap.put("filename", photo.getFile().getName());
                photoMap.put("path", photo.getPath());
                photoMap.put("timestamp", photo.getTimestamp());
                photoMap.put("hash", photo.getHash());
                photoMap.put("isSelected", photo.isSelected());

                List<Float> metrics = photo.getMetrics();
                if (metrics.size() >= 2) {
                    photoMap.put("deltaTimeSeconds", metrics.get(0));
                    photoMap.put("similarityPercent", metrics.get(1));
                }

                photosList.add(photoMap);
            }
            groupMap.put("photos", photosList);
            groupsList.add(groupMap);
        }

        // Add groups array last for better readability
        root.put("groups", groupsList);

        // Write JSON file with pretty printing
        try (FileWriter writer = new FileWriter(jsonFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, writer);
            logMessage("Exported group information to: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            logMessage("Failed to export JSON: " + e.getMessage());
            throw new RuntimeException("Failed to export JSON", e);
        }
    }
}
