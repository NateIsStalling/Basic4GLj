package com.basic4gl.desktop.util;

import com.basic4gl.desktop.spi.DialogService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class FileUtil {
    public static String fromUserHome(String absolutePath) {
        String userHome = System.getProperty("user.home");

        if (absolutePath.startsWith(userHome)) {
            return absolutePath.replaceFirst(userHome, "~");
        }
        return absolutePath; // Return as-is if not in home directory
    }

    public static String getMediaTypeLabel(File file) {
        if (file == null) {
            return "Other";
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".gif")
                || name.endsWith(".bmp")
                || name.endsWith(".webp")
                || name.endsWith(".ico")) {
            return "Images";
        }
        if (name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".flac")) {
            return "Audio";
        }
        if (name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".webm")) {
            return "Video";
        }
        if (name.endsWith(".txt")
                || name.endsWith(".md")
                || name.endsWith(".json")
                || name.endsWith(".xml")
                || name.endsWith(".csv")
                || name.endsWith(".ini")
                || name.endsWith(".cfg")
                || name.endsWith(".properties")) {
            return "Text";
        }
        if (name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".docx") || name.endsWith(".rtf")) {
            return "Documents";
        }
        return "Other";
    }

    public static String formatRelativePath(File file, File baseDir) {
        if (file == null) {
            return "";
        }
        if (baseDir != null) {
            try {
                java.nio.file.Path relative = baseDir.getAbsoluteFile()
                        .toPath()
                        .normalize()
                        .relativize(file.getAbsoluteFile().toPath().normalize());
                String text = relative.toString().replace('\\', '/');
                if (!text.startsWith("..")) {
                    return text;
                }
            } catch (Exception ignored) {
                // Fall back to file name below.
            }
        }
        return file.getName();
    }

    public static void revealInFinder(File file, DialogService dialogService) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browseFileDirectory(file);
            }
        } catch (Exception ex) {
            // Fallback when browseFileDirectory is unavailable.
            openWithSystemDefault(file.getParentFile(), dialogService);
        }
    }

    public static void openWithSystemDefault(File file, DialogService dialogService) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException ex) {
            dialogService.showDialog("Unable to open file: " + ex.getMessage());
        }
    }
}
