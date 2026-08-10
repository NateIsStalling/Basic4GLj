package com.basic4gl.desktop.content.catalog;

import java.util.List;

public record ContentSelectionSummary(
        String title,
        String kindLabel,
        String description,
        String category,
        List<String> relatedIds,
        String primaryAction) {}
