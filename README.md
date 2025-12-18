# Cullergrader

> **Note:** This is a fork of [PenguinPush/cullergrader](https://github.com/PenguinPush/cullergrader) with improvements and default settings optimized for my workflow. Key additions include:
> - **Expression-based selection strategies** - Flexible photo selection using custom boolean expressions with variables like `index`, `length`, `deltaTime`, and `similarity`
> - **Optimized defaults** - Tighter grouping thresholds (0.3s, 30% similarity) and higher hash resolution (12×12) for burst photography
> - **Similarity-adaptive selection** - Automatically selects more photos from dynamic scenes and fewer from static ones

**Cullergrader** is a simple Java GUI made for photographers that groups and exports images based on <a href="https://en.wikipedia.org/wiki/Perceptual_hashing" target="_blank">**perceptual similarity**</a> (and timestamps), allowing users to select the best shots from each set of similar, consecutively taken photos.

Like many photographers, I have the habit of taking the same shot multiple times and selecting the best one to keep. However, when going through thousands of photos, this process of culling images is time-consuming, and tools such as <a href="https://github.com/qarmin/czkawka" target="_blank">**Czkawka**</a> (a large inspiration for this project) can detect a few _very similar_ images, but isn't sensitive enough to group _somewhat similar_ bursts.

Cullergrader is named for being a tool that culls and grades\* photos. Please note that it doesn't actually _colour grade_ photos.

<sub>\* grading photos has yet to be implemented...</sub>

<div style="display: flex; justify-content: center; align-items: center; gap: 20px;">
    <img src="images/exampleA.JPG" alt="DSC07442.JPG" width="300"/>
    <img src="images/exampleB.JPG" alt="DSC07443.JPG" width="300"/>
</div>
<p align="left">For example: Cullergrader would mark these two images as "similar" (at similarity threshold >= 37%)</p>

## Table of Contents

1. [Features](#features)
2. [Installation](#installation)
    1. [Prebuilt Executable](#prebuilt-executable)
    2. [Compiling from Source](#compiling-from-source)
3. [How to Use](#how-to-use)
    1. [Open a Folder of Images](#1-open-a-folder-of-images)
    2. [Calibrate Grouping Settings](#2-calibrate-grouping-settings)
    3. [View Photos and Select Takes](#3-view-photos-and-select-takes)
    4. [Export Selected Takes](#4-export-selected-takes)
4. [Config](#config)
    1. [Default Config](#default-config)
    2. [Config Settings Explained](#config-settings-explained)
5. [Performance Tuning](#performance-tuning)
6. [Contributing](#contributing)
7. [License](#license)

## Features

- 100% free and open-source!
- Experimental RAW image format support - works with most camera RAW files that contain an embedded JPEG preview
- Configurable options for calibrating your perceptual hash
    - Hash similarity
    - Timestamp difference
- Exports the best takes to any folder on your computer
- Runs on Windows, Mac, Linux, and anything else that supports Java GUIs
- Blazingly-fast thanks to configurable multithreading support
- Caches images -- future scans should be incredibly fast!
- Extra information about images available on hover
- Runs completely offline, and never connects to the internet
- Logs information to .txt files
- Light/Dark themes from FlatLaf
- Configurable:
    - Multithreading
    - Hashing settings
    - Cache options
    - Grouping settings
    - Dark theme

![images/group_viewer.png](images/group_viewer.png)

## Installation

### Prebuilt Executable

1. Cullergrader requires a Java 8 JRE or newer to run
2. A prebuilt executable `.jar` with all libraries bundled is available for download at [GitHub Releases](https://github.com/PenguinPush/cullergrader/releases)
3. Extract the `.zip` file to any folder and run `cullergrader-<version>.jar` file to begin using Cullergrader

**Note**:

- If you want to view logs in console, please use `run.bat` or `run.sh` depending on your operating system
- **Using console is highly recommended for large batches of images (or on slow drives), as there is no other way to monitor slow hashing progress!**

### Compiling from Source

1. Ensure you have the following installed:
    - Java Development Kit (JDK) 8 or newer
    - Apache Maven
2. Clone the repository with:
    ```bash
    git clone https://github.com/PenguinPush/cullergrader.git
    cd cullergrader
    ```
3. Build the project with the following command:
    ```
    mvn clean install
    ```
4. Extract the generated `cullergrader-version.zip` and run as described above

## How to Use

### 1. Open a Folder of Images

Folders can be opened from `File > Open Folder` or with `Ctrl + O`. The first time images are computed, it may take a few minutes (but often less) to hash the images, depending on image count and disk speed. Hashes are cached for future use in `cache.json`, so as long as this file stays intact, future computations of the same images will be nearly instant.

![images/open_folder.png](images/open_folder.png)

### 2. Calibrate Grouping Settings

Although the default settings are designed to work fine out of the box, depending on many factors in your photo, and your style of photography, manual calibration is often recommended. By adjusting the timestamp and similarity thresholds and hitting `Reload Groups`, you can adjust how your images are grouped until the grouping behaves as expected.

- `Timestamp Threshold` is the amount of seconds between two photos before it counts it as no longer a part of the same photo group, and creates a new group
- `Similarity Threshold` is the percentage of similarity between the hash of two photos. A higher threshold means more tolerance for less similar photos to be in the same group.

![images/grouping_settings.png](images/grouping_settings.png)

### 3. View Photos and Select Takes

By clicking on a photo, users can access the `Photo Viewer`, bringing up all individual photos in a group, with selected takes marked by a star. By navigating using either mouse or `arrow keys` (left and right to move between photos, up and down to move between groups), users can press `spacebar` or use `Controls > Set as Selected Take` to toggle photo selection. Multiple photos can be selected per group, and groups with 0 selections will not be exported.

![images/photo_viewer.png](images/photo_viewer.png)

**Tip:** by hovering on a photo in the photo viewer, you can view its name, seconds between the last photo, and similarity % to the last photo. Use this information to help you calibrate the grouper.

![images/photo_info.png](images/photo_info.png)

### 4. Export Selected Takes

Selected takes can be exported to a folder using `File > Export Selected Takes` or with `Ctrl + S`. After choosing an export folder, all selected takes from each group will be copied to that folder. Groups with no selections will be skipped.

![images/export_to.png](images/export_to.png)

## CLI Usage

Cullergrader can be run in command-line mode for automated workflows and scripting.

### Basic Usage

```bash
# Launch GUI (no arguments)
java -jar cullergrader.jar

# Run CLI mode
java -jar cullergrader.jar --input /path/to/photos --output /path/to/export
```

### CLI Options

| Option         | Short | Description                                            | Required |
| -------------- | ----- | ------------------------------------------------------ | -------- |
| `--input`      | `-i`  | Input folder containing photos                         | Yes      |
| `--output`     | `-o`  | Output folder for best takes (preview mode if omitted) | No       |
| `--json`       | `-j`  | Export group information to JSON file                  | No       |
| `--time`       | `-t`  | Time threshold in seconds (default: 15)                | No       |
| `--similarity` | `-s`  | Similarity threshold 0-100 (default: 45)               | No       |
| `--help`       | `-h`  | Show help message                                      | No       |

### Examples

**Preview mode (no export)**:

```bash
java -jar cullergrader.jar --input ~/photos/vacation
```

**Export to folder**:

```bash
java -jar cullergrader.jar --input ~/photos/vacation --output ~/photos/best
```

**Custom thresholds**:

```bash
java -jar cullergrader.jar -i ~/photos/vacation -o ~/photos/best -t 10 -s 40
```

**Export JSON metadata only**:

```bash
java -jar cullergrader.jar --input ~/photos/vacation --json groups.json
```

**Export both files and JSON**:

```bash
java -jar cullergrader.jar -i ~/photos/vacation -o ~/photos/best --json ~/photos/best/groups.json
```

## Config

Configuration is **optional**. Cullergrader includes sensible defaults for all settings. To customize, create a `config.json` file in the same directory as the executable.

### Default Config

```json
{
    "DARK_THEME": true,
    "CACHE_FILE": "hashes.json",
    "LOG_DIRECTORY": "logs",
    "DEFAULT_FOLDER_PATH": "",
    "HASHING_ENABLED": true,
    "EXECUTOR_TIMEOUT_MINUTES": 60,
    "MAX_THREADS_RATIO": 4,
    "HASHED_WIDTH": 12,
    "HASHED_HEIGHT": 12,
    "TIME_THRESHOLD_SECONDS": 0.3,
    "SELECTION_STRATEGY": "index == 0 || minDistanceToSelected > 15",
    "SIMILARITY_THRESHOLD_PERCENT": 30,
    "IMAGE_PREVIEW_CACHE_SIZE_MB": 2048
}
```

#### Why These Defaults?

**Grouping Thresholds (0.3s / 30%):**
- `TIME_THRESHOLD_SECONDS: 0.3` is optimized for burst mode photography. My cameras shoot at 11 fps (0.09s between frames) and 4 fps (0.25s between frames), so 0.3s captures both burst modes while separating deliberate pauses between shots.
- `SIMILARITY_THRESHOLD_PERCENT: 30` works well for my shooting style, grouping similar burst shots while keeping distinct compositions separate.

**Selection Strategy:**
The default expression uses **greedy diversity selection** to avoid redundant similar photos:
- Expression: `"index == 0 || minDistanceToSelected > 15"`
- Always selects the first photo (index == 0)
- Subsequent photos are only selected if they're >15% different from **all** previously-selected photos
- This implements the Furthest-First Traversal algorithm for optimal diversity

**Why this works:**
- **Dynamic groups** (lots of variation): Many photos exceed the 15% threshold → more selections
- **Static groups** (little variation): Few photos exceed 15% → fewer selections (but always at least one)
- **Prevents redundancy:** Unlike pairwise similarity, this checks against the entire selection set, so you won't select visually similar photos just because they had different intermediate frames

With 12×12 hash resolution, 15% distance ≈ 77 bits different out of 432 total. This provides good diversity while avoiding over-selection. For typical burst photography, this results in 2-5 selections per group, adapting automatically to scene dynamics.

**Higher Hash Resolution (12×12):**
Increased from 8×8 for better similarity detection, especially useful with the tighter 30% threshold.

### Config Settings Explained

| Setting                        | Description                                                                                                                                                                                                                                | Variable Type |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------- |
| `DARK_THEME`                   | Toggles the dark and light FlatLaf themes. On by default (obviously)                                                                                                                                                                       | `boolean`     |
| `CACHE_FILE`                   | The name of the _file_ used to store hash caches. Passed as a pathname in a File constructor                                                                                                                                               | `String`      |
| `LOG_DIRECTORY`                | The name of the _folder_ used to store logs. Passed as a pathname in a File constructor                                                                                                                                                    | `String`      |
| `DEFAULT_FOLDER_PATH`          | The default folder opened when importing images. Passed as a pathname in a File constructor. When empty, uses the systems `user.home` property                                                                                             | `String`      |
| `HASHING_ENABLED`              | Whether or not hashing is used in grouping photos                                                                                                                                                                                          | `boolean`     |
| `EXECUTOR_TIMEOUT_MINUTES`     | The amount of time, in minutes, before the hash manager times out and stops hashing photos                                                                                                                                                 | `int`         |
| `MAX_THREADS_RATIO`            | The fraction of CPU threads the hasher is allowed to multithread. 2 means half your threads, 3 means a third, etc.                                                                                                                         | `int`         |
| `HASHED_WIDTH`                 | The width that images are computed at before hashing, higher values mean more accurate similarity checks at the cost of performance                                                                                                        | `int`         |
| `HASHED_HEIGHT`                | The height that images are computed at before hashing, higher values mean more accurate similarity checks at the cost of performance                                                                                                       | `int`         |
| `TIME_THRESHOLD_SECONDS`       | The default amount of seconds between photos (from the timestamp) before they're counted as a new group. Editable in-app, but will not change the default stored here                                                                      | `float`       |
| `SIMILARITY_THRESHOLD_PERCENT` | The default similarity between two photo hashes before they're counted as a new group. Higher values means more lenience in image similarity (larger groups, less in number). Editable in-app, but will not change the default stored here | `float`       |
| `SELECTION_STRATEGY`   | The automatic selection strategy when creating groups. Can be a predefined alias or a custom boolean expression (see Expression Syntax below)                                                                                              | `String`      |
| `IMAGE_PREVIEW_CACHE_SIZE_MB`  | Maximum memory (in megabytes) to use for caching image previews. Default 1024 MB (1 GB). Increase for large photo shoots (see Performance Tuning section)                                                                                  | `int`         |

### Expression Syntax for `SELECTION_STRATEGY`

The selection strategy can be either a **predefined alias** or a **custom boolean expression** that determines which photos to select from each group.

#### Predefined Aliases

For convenience, these aliases expand to common expressions:

| Alias              | Equivalent Expression                 | Description                |
| ------------------ | ------------------------------------- | -------------------------- |
| `"first"`          | `index == 0`                          | Select first photo only    |
| `"last"`           | `index == length - 1`                 | Select last photo only     |
| `"first_and_last"` | `index == 0 \|\| index == length - 1` | Select both first and last |
| `"all"`            | `true`                                | Select all photos          |
| `"none"`           | `false`                               | No automatic selection     |

#### Custom Expressions

You can write custom boolean expressions using the following components:

**Variables** (available for each photo in a group):

- `index` - 0-based position in group (0 = first photo, 1 = second, etc.)
- `length` - Total number of photos in the group
- `deltaTime` - Seconds since the previous photo (0 for first photo)
- `similarity` - Difference % to previous photo (0 = identical, 100 = completely different)
- `maxGroupSimilarity` - Maximum difference % found in the group (0-30 within groups)
- `minDistanceToSelected` - Minimum distance % to any previously-selected photo (100 for first photo, enables greedy diversity selection)

**Keywords:**

- `true` - Boolean true
- `false` - Boolean false

**Operators:**

- Arithmetic: `+`, `-`, `*`, `/`, `%` (modulo)
- Comparison: `<`, `>`, `<=`, `>=`, `==` (equals), `!=` (not equals)
- Logical: `&&` (and), `||` (or), `!` (not)
- Grouping: `(`, `)`
- Ternary conditional: `condition ? trueValue : falseValue`

#### Expression Examples

**Basic Selection:**

```json
{
    "SELECTION_STRATEGY": "index < 3"
}
```

Selects the first 3 photos from each group (indices 0, 1, 2).

```json
{
    "SELECTION_STRATEGY": "index % 2 == 0"
}
```

Selects every other photo (even indices: 0, 2, 4, 6...).

```json
{
    "SELECTION_STRATEGY": "index % 2 == 1"
}
```

Selects every other photo (odd indices: 1, 3, 5, 7...).

**Using Metadata:**

```json
{
    "SELECTION_STRATEGY": "deltaTime > 5"
}
```

Selects photos taken more than 5 seconds after the previous photo. Useful for capturing action sequences while skipping bursts.

```json
{
    "SELECTION_STRATEGY": "similarity < 30"
}
```

Selects photos less than 30% similar to the previous photo. Great for finding the most different shots in a sequence.

```json
{
    "SELECTION_STRATEGY": "index == 0 || similarity < 20"
}
```

Selects the first photo OR any photo with low similarity (< 20%). Ensures you keep at least the first shot plus any significantly different ones.

**Conditional Logic:**

```json
{
    "SELECTION_STRATEGY": "length < 5 ? true : index < 2"
}
```

Smart selection: if the group has fewer than 5 photos, select all; otherwise select only the first 2.

```json
{
    "SELECTION_STRATEGY": "length > 10 ? index % 3 == 0 : index < 3"
}
```

For large groups (> 10 photos), select every 3rd photo; for smaller groups, select first 3.

**Complex Combinations:**

```json
{
    "SELECTION_STRATEGY": "(index < 3 || index > length - 3) && similarity < 50"
}
```

Selects photos from the first 3 or last 3 positions, but only if they're less than 50% similar to the previous photo.

```json
{
    "SELECTION_STRATEGY": "index == 0 || (deltaTime > 3 && similarity < 40)"
}
```

Always selects the first photo, plus any photo taken more than 3 seconds later AND less than 40% similar.

**Greedy Diversity Selection:**

```json
{
    "SELECTION_STRATEGY": "index == 0 || minDistanceToSelected > 15"
}
```

Default strategy. Selects first photo, then only photos >15% different from all already-selected photos. Prevents selecting similar-looking photos.

```json
{
    "SELECTION_STRATEGY": "index == 0 || minDistanceToSelected > 20"
}
```

More selective version with 20% threshold. Results in fewer selections, only keeping very diverse photos.

```json
{
    "SELECTION_STRATEGY": "index == 0 || (minDistanceToSelected > 10 && deltaTime > 0.5)"
}
```

Combines diversity with time spacing. Photo must be both >10% different from selected photos AND taken >0.5 seconds after previous photo.

```json
{
    "SELECTION_STRATEGY": "minDistanceToSelected > maxGroupSimilarity"
}
```

Adaptive threshold based on group characteristics. Automatically adjusts selection rate based on how diverse the group is.

**Note:** `minDistanceToSelected` is a **stateful variable** - each photo's value depends on which photos were selected before it (in index order). This enables sophisticated diversity-based selection that's impossible with purely pairwise variables like `similarity`.

#### Tips for Writing Expressions

1. **Test incrementally**: Start with simple expressions like `index < 5` and build complexity.
2. **Use parentheses**: They make complex expressions clearer: `(index < 3) || (index > length - 3)`.
3. **Consider group size**: Use `length` in conditionals to adapt behavior: `length > 20 ? index % 5 == 0 : true`.
4. **Combine criteria**: Mix position, time, and similarity for precise control: `index < 10 && deltaTime > 2`.
5. **Error handling**: If an expression has syntax errors, the system falls back to selecting the first photo and logs the error.

Note: More config options are technically functional, such as `PLACEHOLDER_THUMBNAIL_PATH`, `KEYBIND_TOGGLE_SELECTION`, or `GRIDMEDIA_LABEL_TEXT_COLOR`, but are not documented here and aren't editable by default due to their configurability not significantly impacting program function. Users are free to explore the source code and add these into `config.json` themselves, and they should work as intended.

## Performance Tuning

For large photo shoots, you can increase the image preview cache size in `config.json`:

```json
{
    "IMAGE_PREVIEW_CACHE_SIZE_MB": 2048
}
```

**Default:** 2048 MB (2 GB)

The preview cache stores scaled thumbnails (240×160) for all image files in memory to avoid re-reading from disk. The cache fills until reaching the configured limit, then stops caching new entries (no eviction).

**Configuration examples:**

- 512 MB = Smaller cache for limited memory systems
- 1024 MB = Moderate cache for typical photo shoots
- 2048 MB = Default (recommended for most users)
- 4096 MB = Very large photo shoots (4000+ files)

The cache applies to all image formats (JPEG, PNG, RAW, etc.) and significantly improves performance when:

- Changing grouping thresholds
- Reloading groups
- Scrolling through large photo sets

The cache clears automatically when loading a new directory.

## Contributing

Contributions to Cullergrader are **greatly appreciated**, as a tool made from one photographer to another, the best way Cullergrader can improve is through continued feedback and contributions.

Any and all kinds of contributions to Cullergrader are welcome, please fork the repo and make a pull request! For suggestions without code, feel free to open an issue with an "enhancement" tag. Don't forget to give the project a star! 🙏🙏

1. Fork the Project to your own GitHub account
2. Create your Feature Branch with `git checkout -b feature/<your-awesome-feature>`
3. Commit your changes with `git commit -m "Add <a description of your change>"`
4. Push your changes with `git push origin feature/<your-awesome-feature>`
5. Open a Pull Request at the original repository here, don't forget to describe what you added!

## License

Distributed under the MIT License, see [LICENSE](https://github.com/PenguinPush/cullergrader/blob/main/LICENSE) for more info.
