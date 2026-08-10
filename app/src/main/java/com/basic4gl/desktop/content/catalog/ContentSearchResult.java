package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.IndexedContent;

public record ContentSearchResult(String globalId, IndexedContent content, boolean template, int score) {}
