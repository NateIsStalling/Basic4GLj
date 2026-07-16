package com.basic4gl.desktop.content;

import com.basic4gl.desktop.editor.IFileEditorActionListener;
import com.basic4gl.desktop.editor.IFileViewer;
import com.basic4gl.desktop.editor.IToggleBreakpointListener;
import com.basic4gl.desktop.spi.PluginContext;
import java.io.File;
import java.util.Locale;
import org.fife.ui.rsyntaxtextarea.LinkGenerator;
import org.fife.ui.rtextarea.SearchContext;

/**
 * Factory class that determines which viewer to use for different file types.
 * This is the central place to configure which file types get which viewers.
 */
public class FileViewerFactory {
    private FileViewerFactory() {
        // Utility class
    }

    /**
     * Gets the appropriate viewer type for a given file.
     *
     * @param file the file to determine viewer type for
     * @return the viewer type to use for this file
     */
    public static IFileViewer.ViewerType getViewerType(File file) {
        if (file == null) {
            return IFileViewer.ViewerType.TEXT_EDITOR;
        }

        String name = file.getName().toLowerCase(Locale.ROOT);

        // Image files
        if (isImageFile(name)) {
            return IFileViewer.ViewerType.IMAGE_VIEWER;
        }

        // Audio files
        if (isAudioFile(name)) {
            return IFileViewer.ViewerType.AUDIO_VIEWER;
        }

        // Markdown files
        if (name.endsWith(".md")) {
            return IFileViewer.ViewerType.MARKDOWN_VIEWER;
        }

        // Default to text editor
        return IFileViewer.ViewerType.TEXT_EDITOR;
    }

    /**
     * Creates a file viewer for the given file with the specified viewer type preference.
     * If the preferred viewer type is not available or the file type doesn't match, falls back
     * to a sensible default.
     *
     * @param file the file to create a viewer for (can be null for new unsaved files)
     * @param preferredViewerType the preferred viewer type (can be null to auto-detect)
     * @param actionListener listener for file editor actions
     * @param fileManager file manager instance
     * @param toggleBreakpointListener listener for breakpoint toggles
     * @param linkGenerator link generator for hyperlinks
     * @param searchContext search context for find/replace
     * @param pluginContext plugin context for accessing IDE services
     * @return a new IFileViewer instance
     */
    public static IFileViewer createViewer(
            File file,
            IFileViewer.ViewerType preferredViewerType,
            IFileEditorActionListener actionListener,
            com.basic4gl.desktop.util.IFileManager fileManager,
            IToggleBreakpointListener toggleBreakpointListener,
            LinkGenerator linkGenerator,
            SearchContext searchContext,
            PluginContext pluginContext) {

        IFileViewer.ViewerType viewerType = preferredViewerType;

        // Auto-detect if not preferred
        if (viewerType == null && file != null) {
            viewerType = getViewerType(file);
        }

        // Create the appropriate viewer
        switch (viewerType) {
            case IMAGE_VIEWER:
                if (file != null && isImageFile(file.getName().toLowerCase(Locale.ROOT))) {
                    return new ImageFileViewer(file);
                }
            case HEX_VIEWER:
                return new HexFileViewer(file);
            case AUDIO_VIEWER:
                if (file != null && isAudioFile(file.getName().toLowerCase(Locale.ROOT))) {
                    return new AudioFileViewer(file);
                }
            case MARKDOWN_VIEWER:
                if (file != null && file.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
                    return new MarkdownViewer(pluginContext, file);
                }
            case HTML_VIEWER:
                if (file != null
                        && (file.getName().toLowerCase(Locale.ROOT).endsWith(".html")
                                || file.getName().toLowerCase(Locale.ROOT).endsWith(".htm"))) {
                    return new HtmlViewer(pluginContext, file);
                }
            case TEXT_EDITOR:
            default:
                return new TextFileViewer(
                        file, actionListener, fileManager, toggleBreakpointListener, linkGenerator, searchContext);
        }
    }

    /**
     * Determines if a file is an image file
     */
    public static boolean isImageFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".bmp")
                || lower.endsWith(".webp")
                || lower.endsWith(".ico")
                || lower.endsWith(".svg")
                || lower.endsWith(".tiff")
                || lower.endsWith(".tif");
    }

    /**
     * Determines if a file is an audio file
     */
    public static boolean isAudioFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".wav")
                || lower.endsWith(".ogg")
                || lower.endsWith(".mp3")
                || lower.endsWith(".flac")
                || lower.endsWith(".aac")
                || lower.endsWith(".m4a")
                || lower.endsWith(".wma")
                || lower.endsWith(".aiff");
    }

    /**
     * Determines if a file should be opened with a hex viewer
     */
    public static boolean isBinaryFile(String filename) {
        // Files that are binary but might need viewing
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jar")
                || lower.endsWith(".zip")
                || lower.endsWith(".bin")
                || lower.endsWith(".dat")
                || lower.endsWith(".class");
    }
}
