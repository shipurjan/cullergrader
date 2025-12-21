package com.penguinpush.cullergrader.media;

import static com.penguinpush.cullergrader.utils.Logger.logMessage;
import static com.penguinpush.cullergrader.utils.Logger.logToConsoleOnly;

import com.penguinpush.cullergrader.config.AppConstants;
import com.penguinpush.cullergrader.config.ExecutionMode;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;

public class PhotoUtils {

    // Simple fill-until-full cache for all image previews (strong references to avoid GC under memory pressure)
    // Caches SCALED previews at display resolution (240×160 default)
    // Cache size configured via IMAGE_PREVIEW_CACHE_SIZE_MB in config.json (default 1024 MB)
    private static final long MAX_CACHE_SIZE_BYTES = AppConstants.IMAGE_PREVIEW_CACHE_SIZE_MB * 1024L * 1024L;
    private static long currentCacheSizeBytes = 0;

    // Hit/miss tracking for monitoring cache effectiveness
    private static final AtomicLong cacheHits = new AtomicLong(0);
    private static final AtomicLong cacheMisses = new AtomicLong(0);

    // Simple fill-until-full cache with no eviction
    private static final Map<String, ImagePreviewEntry> imagePreviewCache =
        Collections.synchronizedMap(new LinkedHashMap<String, ImagePreviewEntry>(16, 0.75f, false));

    private static class ImagePreviewEntry {
        final long lastModified;
        final BufferedImage preview;  // Strong reference (no SoftReference)
        final long sizeBytes;

        ImagePreviewEntry(long lastModified, BufferedImage preview) {
            this.lastModified = lastModified;
            this.preview = preview;
            // Calculate actual byte size: width × height × bytes per pixel
            // TYPE_INT_RGB = 4 bytes per pixel, TYPE_3BYTE_BGR = 3 bytes per pixel, etc.
            int bytesPerPixel;
            switch (preview.getType()) {
                case BufferedImage.TYPE_INT_RGB:
                case BufferedImage.TYPE_INT_ARGB:
                case BufferedImage.TYPE_INT_ARGB_PRE:
                case BufferedImage.TYPE_INT_BGR:
                    bytesPerPixel = 4;
                    break;
                case BufferedImage.TYPE_3BYTE_BGR:
                    bytesPerPixel = 3;
                    break;
                case BufferedImage.TYPE_USHORT_565_RGB:
                case BufferedImage.TYPE_USHORT_555_RGB:
                case BufferedImage.TYPE_USHORT_GRAY:
                    bytesPerPixel = 2;
                    break;
                case BufferedImage.TYPE_BYTE_GRAY:
                case BufferedImage.TYPE_BYTE_BINARY:
                case BufferedImage.TYPE_BYTE_INDEXED:
                    bytesPerPixel = 1;
                    break;
                default:
                    // For custom or unknown types, estimate 4 bytes per pixel
                    bytesPerPixel = 4;
            }
            this.sizeBytes = preview.getWidth() * preview.getHeight() * bytesPerPixel;
        }
    }

    private static boolean isHashSizeRequest(int targetWidth, int targetHeight) {
        return targetWidth == AppConstants.HASHED_WIDTH
            && targetHeight == AppConstants.HASHED_HEIGHT;
    }

    public static int getExifOrientation(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifIFD0Directory exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifDirectory != null && exifDirectory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return exifDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch (Exception e) {
            // No EXIF data or error reading, use default orientation
        }
        return 1; // Default: no rotation needed
    }

    public static BufferedImage rotateImageByExif(BufferedImage image, int orientation) {
        if (orientation == 1 || image == null) {
            return image; // No rotation needed
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // For rotations that swap dimensions (90 or 270 degrees)
        int newWidth = (orientation >= 5 && orientation <= 8) ? height : width;
        int newHeight = (orientation >= 5 && orientation <= 8) ? width : height;

        // Use TYPE_INT_RGB for most images, TYPE_INT_ARGB if transparency is needed
        int imageType = BufferedImage.TYPE_INT_RGB;
        if (image.getTransparency() != BufferedImage.OPAQUE) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, imageType);
        Graphics2D g2d = rotatedImage.createGraphics();

        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform transform = new AffineTransform();

        switch (orientation) {
            case 2: // Flip horizontally
                transform.scale(-1.0, 1.0);
                transform.translate(-width, 0);
                break;
            case 3: // Rotate 180 degrees
                transform.translate(width, height);
                transform.rotate(Math.PI);
                break;
            case 4: // Flip vertically
                transform.scale(1.0, -1.0);
                transform.translate(0, -height);
                break;
            case 5: // Flip horizontally and rotate 90 CW
                transform.rotate(Math.PI / 2);
                transform.scale(-1.0, 1.0);
                break;
            case 6: // Rotate 90 CW
                transform.translate(height, 0);
                transform.rotate(Math.PI / 2);
                break;
            case 7: // Flip horizontally and rotate 90 CCW
                transform.scale(-1.0, 1.0);
                transform.translate(-height, 0);
                transform.translate(0, width);
                transform.rotate(3 * Math.PI / 2);
                break;
            case 8: // Rotate 90 CCW
                transform.translate(0, width);
                transform.rotate(3 * Math.PI / 2);
                break;
            default:
                g2d.dispose();
                return image;
        }

        g2d.drawImage(image, transform, null);
        g2d.dispose();

        return rotatedImage;
    }

    private static BufferedImage loadImage(File file) throws Exception {
        if (isRawFile(file)) {
            BufferedImage fullImage = extractRawPreview(file);
            if (fullImage == null) {
                logToConsoleOnly("Skipping RAW file without embedded preview: " + file.getName());
            }
            return fullImage;
        } else {
            return ImageIO.read(file);
        }
    }

    public static BufferedImage readLowResImage(File file, int targetWidth, int targetHeight, ExecutionMode mode) throws Exception {
        String path = file.getAbsolutePath();
        long lastModified = file.lastModified();

        boolean isHashRequest = isHashSizeRequest(targetWidth, targetHeight);

        // Check cache - but skip for hash requests to avoid double-scaling
        ImagePreviewEntry entry = imagePreviewCache.get(path);
        if (entry != null && entry.lastModified == lastModified && !isHashRequest) {
            cacheHits.incrementAndGet();
            logToConsoleOnly("Retrieved cached preview: " + file.getName());
            return scalePreviewIfNeeded(entry.preview, targetWidth, targetHeight);
        }

        // CLI mode: Skip all caching
        if (!mode.shouldCacheThumbnails()) {
            BufferedImage fullImage = loadImage(file);
            if (fullImage == null) return null;
            // Apply EXIF rotation
            int orientation = getExifOrientation(file);
            fullImage = rotateImageByExif(fullImage, orientation);
            return scalePreviewIfNeeded(fullImage, targetWidth, targetHeight);
        }

        // GUI mode: Load original image
        BufferedImage fullImage = loadImage(file);
        if (fullImage == null) return null;
        // Apply EXIF rotation
        int orientation = getExifOrientation(file);
        fullImage = rotateImageByExif(fullImage, orientation);

        // Hash request: Scale directly from original, but still cache preview for GUI
        if (isHashRequest) {
            if (entry == null) {
                int cacheWidth = AppConstants.PREVIEW_WIDTH;
                int cacheHeight = AppConstants.PREVIEW_HEIGHT;
                BufferedImage cachedPreview = scalePreviewIfNeeded(fullImage, cacheWidth, cacheHeight);

                ImagePreviewEntry newEntry = new ImagePreviewEntry(lastModified, cachedPreview);
                if (currentCacheSizeBytes + newEntry.sizeBytes <= MAX_CACHE_SIZE_BYTES) {
                    imagePreviewCache.put(path, newEntry);
                    currentCacheSizeBytes += newEntry.sizeBytes;
                    logToConsoleOnly("Cached preview for: " + file.getName());
                }
            }

            cacheMisses.incrementAndGet();
            return scalePreviewIfNeeded(fullImage, targetWidth, targetHeight);
        }

        // Thumbnail request: Standard cache path
        int cacheWidth = AppConstants.PREVIEW_WIDTH;
        int cacheHeight = AppConstants.PREVIEW_HEIGHT;
        BufferedImage cachedPreview = scalePreviewIfNeeded(fullImage, cacheWidth, cacheHeight);

        ImagePreviewEntry newEntry = new ImagePreviewEntry(lastModified, cachedPreview);
        if (currentCacheSizeBytes + newEntry.sizeBytes <= MAX_CACHE_SIZE_BYTES) {
            imagePreviewCache.put(path, newEntry);
            currentCacheSizeBytes += newEntry.sizeBytes;
            logToConsoleOnly("Cached preview for: " + file.getName());
        }

        cacheMisses.incrementAndGet();
        return scalePreviewIfNeeded(cachedPreview, targetWidth, targetHeight);
    }

    public static boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".png") || name.endsWith(".bmp")
                || name.endsWith(".gif") || name.endsWith(".webp")
                // RAW formats
                || name.endsWith(".arw") || name.endsWith(".sr2")  // Sony
                || name.endsWith(".cr2") || name.endsWith(".cr3") || name.endsWith(".crw")  // Canon
                || name.endsWith(".nef") || name.endsWith(".nrw")  // Nikon
                || name.endsWith(".dng")  // Adobe/Universal
                || name.endsWith(".orf") || name.endsWith(".ori")  // Olympus
                || name.endsWith(".raf")  // Fujifilm
                || name.endsWith(".pef") || name.endsWith(".ptx")  // Pentax
                || name.endsWith(".rw2")  // Panasonic
                || name.endsWith(".3fr")  // Hasselblad
                || name.endsWith(".raw");  // Generic
    }

    public static boolean isRawFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".arw") || name.endsWith(".sr2")
                || name.endsWith(".cr2") || name.endsWith(".cr3") || name.endsWith(".crw")
                || name.endsWith(".nef") || name.endsWith(".nrw")
                || name.endsWith(".dng")
                || name.endsWith(".orf") || name.endsWith(".ori")
                || name.endsWith(".raf")
                || name.endsWith(".pef") || name.endsWith(".ptx")
                || name.endsWith(".rw2")
                || name.endsWith(".3fr")
                || name.endsWith(".raw");
    }

    /**
     * Extracts embedded JPEG preview from a RAW file.
     *
     * Uses metadata-extractor to locate the thumbnail, then reads the bytes directly.
     * Caching is handled by readLowResImage(), not here.
     *
     * @param file The RAW file to extract preview from
     * @return BufferedImage of the embedded JPEG preview, or null if no preview found
     */
    private static BufferedImage extractRawPreview(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifThumbnailDirectory thumbnailDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);

            if (thumbnailDir == null) {
                return null;
            }

            // Get thumbnail length from metadata-extractor
            // The library handles all RAW format parsing for us
            Integer length = thumbnailDir.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);

            if (length == null) {
                return null;
            }

            // Use getAdjustedThumbnailOffset() for the correct absolute offset
            // This method properly calculates the offset relative to the file start
            int adjustedOffset = thumbnailDir.getAdjustedThumbnailOffset();

            // Read the thumbnail bytes from the file at the adjusted offset
            byte[] thumbnailBytes = new byte[length];
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(adjustedOffset);
                raf.readFully(thumbnailBytes);
            }

            // Decode the JPEG thumbnail
            ByteArrayInputStream bais = new ByteArrayInputStream(thumbnailBytes);
            BufferedImage preview = ImageIO.read(bais);

            // Apply EXIF rotation to RAW preview
            if (preview != null) {
                int orientation = getExifOrientation(file);
                preview = rotateImageByExif(preview, orientation);
            }

            return preview;

        } catch (Exception e) {
            logToConsoleOnly("Failed to extract preview from RAW file: " + file.getName() + " - " + e.getMessage());
            return null;
        }
    }

    private static BufferedImage scalePreviewIfNeeded(BufferedImage preview, int targetWidth, int targetHeight) {
        int previewWidth = preview.getWidth();
        int previewHeight = preview.getHeight();

        // If preview is significantly larger than target, scale it down
        if (previewWidth > targetWidth * 2 || previewHeight > targetHeight * 2) {
            int xStep = Math.max(1, previewWidth / targetWidth);
            int yStep = Math.max(1, previewHeight / targetHeight);

            BufferedImage scaledPreview = new BufferedImage(
                previewWidth / xStep,
                previewHeight / yStep,
                BufferedImage.TYPE_INT_RGB
            );

            java.awt.Graphics2D g = scaledPreview.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                              java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(preview, 0, 0, scaledPreview.getWidth(), scaledPreview.getHeight(), null);
            g.dispose();

            return scaledPreview;
        }

        return preview;
    }

    public static long extractTimestamp(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            if (directory != null) {
                Date date = directory.getDateOriginal();
                if (date != null) {
                    return date.getTime(); // convert to unix time
                }
            }
        } catch (Exception e) {
            logMessage("couldn't find exif for: " + file.getName() + ", defaulting to last modified");
        }

        return file.lastModified(); // fallback
    }

    public static ImageIcon getScaledIcon(BufferedImage img, int width, int height) {
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_FAST);
        return new ImageIcon(scaled);
    }
    
    public static float[] rgbToLab(int red, int green, int blue) {
        // convoluted algorithm to convert rgb color space to lab
        
        float rNorm = red / 255f;
        float gNorm = green / 255f;
        float bNorm = blue / 255f;

        rNorm = (rNorm > 0.04045f) ? (float) Math.pow((rNorm + 0.055f) / 1.055f, 2.4f) : rNorm / 12.92f;
        gNorm = (gNorm > 0.04045f) ? (float) Math.pow((gNorm + 0.055f) / 1.055f, 2.4f) : gNorm / 12.92f;
        bNorm = (bNorm > 0.04045f) ? (float) Math.pow((bNorm + 0.055f) / 1.055f, 2.4f) : bNorm / 12.92f;

        float x = (rNorm * 0.4124f + gNorm * 0.3576f + bNorm * 0.1805f) / 0.95047f;
        float y = (rNorm * 0.2126f + gNorm * 0.7152f + bNorm * 0.0722f) / 1.00000f;
        float z = (rNorm * 0.0193f + gNorm * 0.1192f + bNorm * 0.9505f) / 1.08883f;

        x = (x > 0.008856f) ? (float) Math.pow(x, 1.0 / 3.0) : (7.787f * x + 16f / 116f);
        y = (y > 0.008856f) ? (float) Math.pow(y, 1.0 / 3.0) : (7.787f * y + 16f / 116f);
        z = (z > 0.008856f) ? (float) Math.pow(z, 1.0 / 3.0) : (7.787f * z + 16f / 116f);

        float luminance = 116f * y - 16f;
        float a = 500f * (x - y);
        float b = 200f * (y - z);

        return new float[]{luminance, a, b};
    }
    
    public static float[] rgbToHsv(int r, int g, int b) {
        float[] hsv = new float[3];

        float rNormalized = r / 255f;
        float gNormalized = g / 255f;
        float bNormalized = b / 255f;

        float cMax = Math.max(rNormalized, Math.max(gNormalized, bNormalized));
        float cMin = Math.min(rNormalized, Math.min(gNormalized, bNormalized));
        float delta = cMax - cMin;

        float h = 0;
        if (delta != 0) {
          if (cMax == rNormalized) {
              h = 60 * (((gNormalized - bNormalized) / delta) % 6);
          } else if (cMax == gNormalized) {
              h = 60 * (((bNormalized - rNormalized) / delta) + 2);
          } else if (cMax == bNormalized) {
              h = 60 * (((rNormalized - gNormalized) / delta) + 4);
          }
      }

        if(h < 0)
          h += 360;


        float s = (cMax == 0) ? 0 : delta / cMax;
        float v = cMax;

        hsv[0] = h;
        hsv[1] = s;
        hsv[2] = v;
        return hsv;
    }

    /**
     * Returns the number of image previews currently cached in memory.
     * Useful for debugging and monitoring cache effectiveness.
     */
    public static int getImagePreviewCacheSize() {
        return imagePreviewCache.size();
    }

    /**
     * Returns the current cache size in bytes.
     */
    public static long getCurrentCacheSizeBytes() {
        return currentCacheSizeBytes;
    }

    /**
     * Returns the maximum image preview cache size in bytes.
     */
    public static long getImagePreviewCacheMaxSizeBytes() {
        return MAX_CACHE_SIZE_BYTES;
    }

    /**
     * Clears all cached image previews.
     * Useful for testing or freeing memory.
     */
    public static void clearImagePreviewCache() {
        int size = imagePreviewCache.size();
        imagePreviewCache.clear();
        currentCacheSizeBytes = 0;
        logMessage("Cleared image preview cache (" + size + " entries)");
    }

}
