package com.basic4gl.language.adapter;

import static org.junit.jupiter.api.Assertions.*;

import com.basic4gl.desktop.spi.content.ClasspathContentSource;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class Basic4GLContentResourcesTest {

    @Test
    void embedsDocsAndSamplesWithIndexes() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        ClasspathContentSource docs = new ClasspathContentSource(
                classLoader, "basic4gl-content/docs/basic4gl", "basic4gl-content/docs/basic4gl.index");
        ClasspathContentSource samples = new ClasspathContentSource(
                classLoader, "basic4gl-content/samples/Programs", "basic4gl-content/samples/Programs.index");

        assertTrue(
                docs.resources().stream().anyMatch(resource -> resource.path().equals("index.md")));
        assertTrue(samples.resources().stream()
                .anyMatch(resource -> resource.path().equals("AsteroidDemo.gb")));
        assertTrue(new String(docs.open("index.md").readAllBytes(), StandardCharsets.UTF_8).contains("Basic4GL"));
    }
}
