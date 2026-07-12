package com.basic4gl.desktop.content;

import com.basic4gl.desktop.spi.FileUtil;
import com.basic4gl.desktop.spi.LanguageService;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static com.basic4gl.desktop.util.FileUtil.getMediaTypeLabel;

public class AssetService {
    private final FileManager fileManager;

    public AssetService(FileManager fileManager) {
        this.fileManager = fileManager;
    }

}
