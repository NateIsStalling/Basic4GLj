package com.basic4gl.library.plugin;

import java.util.Collections;
import java.util.List;

public class PluginJARDetails {
    private final String filename;
    private final String metadataSummary;
    private final String metadataDetails;
    private final boolean compatible;
    private final List<String> functions;
    private final List<String> constants;
    private final String structures;

    public PluginJARDetails(
            String filename,
            String metadataSummary,
            String metadataDetails,
            boolean compatible,
            List<String> functions,
            List<String> constants,
            String structures) {
        this.filename = filename;
        this.metadataSummary = metadataSummary;
        this.metadataDetails = metadataDetails;
        this.compatible = compatible;
        this.functions = functions == null ? Collections.emptyList() : List.copyOf(functions);
        this.constants = constants == null ? Collections.emptyList() : List.copyOf(constants);
        this.structures = structures == null ? "" : structures;
    }

    public String getFilename() {
        return filename;
    }

    public String getMetadataSummary() {
        return metadataSummary;
    }

    public String getMetadataDetails() {
        return metadataDetails;
    }

    public boolean isCompatible() {
        return compatible;
    }

    public List<String> getFunctions() {
        return functions;
    }

    public List<String> getConstants() {
        return constants;
    }

    public String getStructures() {
        return structures;
    }
}
