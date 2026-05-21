package com.basic4gl.lib.util;

import java.util.List;

/**
 * Optional contract for builders that support embedding export assets.
 */
public interface IAssetExportBuilder {

    void setExportAssets(List<String> assets);

    void setExportAssetBaseDirectory(String baseDirectory);

    List<String> getExportAssets();
}

