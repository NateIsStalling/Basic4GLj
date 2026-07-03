package com.basic4gl.compiler;

/*  Created 2-Jun-2007: Thomas Mulgrew (tmulgrew@slingshot.co.nz)

	Basic4GL compiler pre-processor.
*/

import static com.basic4gl.language.core.internal.Assert.assertTrue;

import com.basic4gl.compiler.util.ISourceFile;
import com.basic4gl.compiler.util.ISourceFileServer;
import com.basic4gl.language.core.runtime.HasErrorState;
import com.basic4gl.language.spi.PluginManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Preprocessor
 *
 * Basic4GL compiler preprocessor.
 * Note: Basic4GL doesn't do a lot of preprocessing. But we do implement an
 * #include file mechanism. The preprocessor has the task of transparently
 * expanding #includes into a single large source file.
 *
 * Basic4GLj adds the #plugin directive to the preprocessor for compatibility
 * with older programs that loaded DLLs declared in their file header.
 */
public class Preprocessor extends HasErrorState {
    private static final String PLUGIN_DIRECTIVE = "#plugin";

    private final PluginManager pluginManager;

    // Registered source file servers
    private List<ISourceFileServer> fileServers = new ArrayList<>();

    // Stack of currently opened files.
    // openFiles.back() is the current file being parsed
    private final Vector<ISourceFile> openFiles = new Vector<>();

    // Filenames of visited source files. (To prevent circular includes)
    private final List<String> visitedFiles = new ArrayList<>();

    // Source file <=> Processed file mapping
    private final LineNumberMapping lineNumberMap = new LineNumberMapping();

    void closeAll() {

        // Close all open files
        for (int i = 0; i < openFiles.size(); i++) {
            openFiles.get(i).release();
        }
        openFiles.clear();
    }

    ISourceFile openFile(String filename) {
        System.out.println("Preprocessing include file: \n" + filename);
        // Query file servers in order until one returns an open file.
        for (ISourceFileServer server : fileServers) {
            ISourceFile file = server.openSourceFile(filename);
            if (file != null) {
                return file;
            }
        }

        // Unable to open file
        return null;
    }

    /**
     * Construct the preprocessor. Pass in 0 or more file servers to initialise.
     */
    public Preprocessor(PluginManager pluginManager, int serverCount, ISourceFileServer... server) {
        this.pluginManager = pluginManager;
        // Register source file servers
        for (int i = 0; i < serverCount; i++) {
            fileServers.add(server[i]);
        }
    }

    protected void finalize() // virtual ~Preprocessor();
            {

        // Ensure no source files are still open
        closeAll();

        // Delete source file servers
        fileServers.clear();
        fileServers = null;
    }

    /**
     * Process source file into one large file.
     * Parser is initialised with the expanded file.
     */
    public boolean preprocess(ISourceFile mainFile, Parser parser) {
        assertTrue(mainFile != null);

        // Reset
        closeAll();
        visitedFiles.clear();
        lineNumberMap.clear();
        clearError();

        // Clear the parser
        parser.getSourceCode().clear();

        // Load the main file
        openFiles.add(mainFile);

        // Process files
        while (!openFiles.isEmpty() && !hasError()) {
            // Check for Eof
            if (openFiles.lastElement().isEof()) {

                // Close innermost file
                openFiles.lastElement().release();
                openFiles.remove(openFiles.size() - 1);
            } else {

                // Read a line from the source file
                int lineNo = openFiles.lastElement().getLineNumber();
                String line = openFiles.lastElement().getNextLine();

                // Check for #plugin declaration. Keep source line mapping aligned while
                // removing preprocessor-only directive text from parser input.
                String pluginDeclaration = extractPluginDirectiveValue(line);
                if (pluginDeclaration != null) {
                    lineNumberMap.addLine(openFiles.lastElement().getFilename(), lineNo);
                    parser.getSourceCode().add("");
                    if (!ensurePluginLoaded(pluginDeclaration)) {
                        if (getError() == null || getError().isBlank()) {
                            setError("Could not find plugin: " + pluginDeclaration);
                        }
                        break;
                    }
                    continue;
                }

                // Check for #include
                boolean include = (line.length() >= 8
                        && line.substring(0, 8).toLowerCase().equals("include "));
                if (include) {

                    // Get filename
                    String includeName =
                            separatorsToSystem(line.substring(8, line.length()).trim());
                    String parent = new File(mainFile.getFilename()).getParent(); // Parent directory
                    String filename = new File(parent, includeName).getAbsolutePath();

                    // Check this file hasn't been included already
                    if (!visitedFiles.contains(filename)) {

                        // Open next file
                        ISourceFile file = openFile(filename);
                        if (file == null) {
                            setError("Unable to open file: " + includeName);
                        } else {
                            // This becomes the new innermost file
                            openFiles.add(file);

                            // Add to visited files list
                            visitedFiles.add(0, filename);
                        }
                    } else {
                        setError("File already included: " + includeName);
                    }
                } else {
                    // Not an #include line
                    // Add to parser, and line number map
                    lineNumberMap.addLine(openFiles.lastElement().getFilename(), lineNo);
                    parser.getSourceCode().add(line);
                }
            }
        }

        // Return true if no error encountered
        return !hasError();
    }

    public LineNumberMapping getLineNumberMap() {
        return lineNumberMap;
    }

    public static String extractPluginDirectiveValue(String line) {
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (trimmed.length() <= PLUGIN_DIRECTIVE.length()
                || !trimmed.regionMatches(true, 0, PLUGIN_DIRECTIVE, 0, PLUGIN_DIRECTIVE.length())) {
            return null;
        }
        if (!Character.isWhitespace(trimmed.charAt(PLUGIN_DIRECTIVE.length()))) {
            return null;
        }
        String value = trimmed.substring(PLUGIN_DIRECTIVE.length()).trim();
        return value.isEmpty() ? null : value;
    }

    private boolean ensurePluginLoaded(String pluginDeclaration) {
        if (pluginManager == null) {
            setError("Plugin manager is unavailable for #plugin declaration: " + pluginDeclaration);
            return false;
        }

        String normalizedDeclaration = normalizePluginDeclaration(pluginDeclaration);
        if (normalizedDeclaration == null) {
            setError("Invalid #plugin declaration: " + pluginDeclaration);
            return false;
        }

        LinkedHashSet<String> candidates = buildPluginCandidates(normalizedDeclaration);
        if (isPluginAlreadyLoaded(normalizedDeclaration, candidates)) {
            return true;
        }

        String lastPluginError = null;

        // Prefer direct metadata-name matches (e.g. "fontplugin.dll" -> metadata name "fontplugin").
        String metadataResolvedFilename = resolveFilenameByMetadataName(normalizedDeclaration);
        if (metadataResolvedFilename != null && !metadataResolvedFilename.isBlank()) {
            candidates.add(metadataResolvedFilename);
        }

        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            if (pluginManager.loadPlugin(candidate)) {
                return true;
            }

            String pluginError = pluginManager.getError();
            if (pluginError != null && pluginError.toLowerCase(Locale.ROOT).contains("already loaded")) {
                return true;
            }
            if (pluginError != null && !pluginError.isBlank()) {
                lastPluginError = pluginError;
            }
        }

        if (lastPluginError == null || lastPluginError.isBlank()) {
            setError("Could not find plugin: " + normalizedDeclaration);
        } else {
            setError("Could not find plugin '" + normalizedDeclaration + "': " + lastPluginError);
        }
        return false;
    }

    private String resolveFilenameByMetadataName(String declaration) {
        if (pluginManager == null) {
            return null;
        }
        String declarationToken = normalizePluginToken(declaration);
        if (declarationToken == null) {
            return null;
        }

        try {
            Method getJarFiles = pluginManager.getClass().getMethod("getJARFiles");
            Object result = getJarFiles.invoke(pluginManager);
            if (!(result instanceof Iterable<?> jarFiles)) {
                return null;
            }

            for (Object jarFile : jarFiles) {
                if (jarFile == null) {
                    continue;
                }
                String filename = invokeStringGetter(jarFile, "getFilename");
                if (filename == null || filename.isBlank()) {
                    continue;
                }

                String pluginName = invokeStringGetter(jarFile, "getPluginName");
                if (matchesPluginDeclarationIgnoreCase(pluginName, declaration)) {
                    return filename;
                }
                String pluginNameToken = normalizePluginToken(pluginName);
                if (pluginNameToken != null && declarationToken != null && pluginNameToken.equalsIgnoreCase(declarationToken)) {
                    return filename;
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private boolean matchesPluginDeclarationIgnoreCase(String pluginName, String declaration) {
        String normalizedName = normalizePluginDeclaration(pluginName);
        String normalizedDeclaration = normalizePluginDeclaration(declaration);
        if (normalizedName == null || normalizedDeclaration == null) {
            return false;
        }

        if (normalizedName.equalsIgnoreCase(normalizedDeclaration)) {
            return true;
        }

        String normalizedNameNoExt = stripKnownExtension(normalizedName);
        String normalizedDeclarationNoExt = stripKnownExtension(normalizedDeclaration);
        return normalizedNameNoExt.equalsIgnoreCase(normalizedDeclarationNoExt);
    }

    private LinkedHashSet<String> buildPluginCandidates(String declaration) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(declaration);

        String filenameOnly = declaration;
        int separatorIndex = Math.max(filenameOnly.lastIndexOf('/'), filenameOnly.lastIndexOf('\\'));
        if (separatorIndex >= 0 && separatorIndex < filenameOnly.length() - 1) {
            filenameOnly = filenameOnly.substring(separatorIndex + 1);
        }
        candidates.add(filenameOnly);

        String lower = filenameOnly.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".dll")) {
            candidates.add(filenameOnly.substring(0, filenameOnly.length() - 4) + ".jar");
        } else if (!lower.endsWith(".jar")) {
            candidates.add(filenameOnly + ".jar");
        }

        String baseName = stripKnownExtension(filenameOnly);
        if (!baseName.equals(filenameOnly)) {
            candidates.add(baseName);
            candidates.add(baseName + ".jar");
        }

        // Include plugin-manager-resolved names and discovered plugin files
        // whose filename starts with the declaration token.
        appendResolvedPluginCandidates(candidates, declaration, normalizePluginToken(declaration));

        return candidates;
    }

    private void appendResolvedPluginCandidates(LinkedHashSet<String> candidates, String declaration, String declarationToken) {
        if (pluginManager == null) {
            return;
        }
        try {
            Method getJarFiles = pluginManager.getClass().getMethod("getJARFiles");
            Object result = getJarFiles.invoke(pluginManager);
            if (!(result instanceof Iterable<?> jarFiles)) {
                return;
            }

            for (Object jarFile : jarFiles) {
                if (jarFile == null) {
                    continue;
                }
                String filename = invokeStringGetter(jarFile, "getFilename");
                String pluginName = invokeStringGetter(jarFile, "getPluginName");
                String description = invokeStringGetter(jarFile, "getDescription");

                if (filename == null || filename.isBlank()) {
                    continue;
                }

                String filenameToken = normalizePluginToken(filename);
                String nameToken = normalizePluginToken(pluginName);
                String descriptionToken = normalizePluginToken(description);

                boolean matchesResolvedName = declarationToken != null
                        && ((nameToken != null && nameToken.equals(declarationToken))
                                || (descriptionToken != null && descriptionToken.equals(declarationToken)));

                boolean filenameStartsWithDeclaration = declarationToken != null
                        && filenameToken != null
                        && filenameToken.startsWith(declarationToken);

                if (!matchesResolvedName && !filenameStartsWithDeclaration) {
                    continue;
                }

                candidates.add(filename);
                candidates.add(stripKnownExtension(filename));
                if (!filename.toLowerCase(Locale.ROOT).endsWith(".jar")) {
                    candidates.add(filename + ".jar");
                }

                if (pluginName != null && !pluginName.isBlank()) {
                    candidates.add(pluginName);
                    candidates.add(stripKnownExtension(pluginName));
                    if (!pluginName.toLowerCase(Locale.ROOT).endsWith(".jar")) {
                        candidates.add(pluginName + ".jar");
                    }
                }
            }
        } catch (Exception ignored) {
            // Keep candidate generation best-effort to avoid breaking preprocessing.
        }
    }

    private String invokeStringGetter(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            if (value instanceof String text) {
                return text;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isPluginAlreadyLoaded(String declaration, Set<String> candidates) {
        String declarationToken = normalizePluginToken(declaration);
        HashSet<String> candidateTokens = new HashSet<>();
        for (String candidate : candidates) {
            String token = normalizePluginToken(candidate);
            if (token != null) {
                candidateTokens.add(token);
            }
        }

        for (Object loadedLibrary : pluginManager.getLoadedLibraries()) {
            if (loadedLibrary == null) {
                continue;
            }

            ArrayList<String> loadedNames = new ArrayList<>();
            if (loadedLibrary instanceof com.basic4gl.language.spi.PluginLibrary pluginLibrary) {
                loadedNames.add(pluginLibrary.getDescription());
            }

            try {
                Method method = loadedLibrary.getClass().getMethod("getFilename");
                Object value = method.invoke(loadedLibrary);
                if (value instanceof String filename) {
                    loadedNames.add(filename);
                }
            } catch (Exception ignored) {
            }

            for (String loadedName : loadedNames) {
                String loadedToken = normalizePluginToken(loadedName);
                if (loadedToken == null) {
                    continue;
                }
                if (declarationToken != null && declarationToken.equals(loadedToken)) {
                    return true;
                }
                if (candidateTokens.contains(loadedToken)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String normalizePluginDeclaration(String declaration) {
        if (declaration == null) {
            return null;
        }
        String normalized = declaration.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if ((normalized.startsWith("\"") && normalized.endsWith("\"")
                        || normalized.startsWith("'") && normalized.endsWith("'"))
                && normalized.length() > 1) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizePluginToken(String declaration) {
        String normalized = normalizePluginDeclaration(declaration);
        if (normalized == null) {
            return null;
        }

        int separatorIndex = Math.max(normalized.lastIndexOf('/'), normalized.lastIndexOf('\\'));
        if (separatorIndex >= 0 && separatorIndex < normalized.length() - 1) {
            normalized = normalized.substring(separatorIndex + 1);
        }

        normalized = stripKnownExtension(normalized.toLowerCase(Locale.ROOT));
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') {
                token.append(ch);
            } else {
                break;
            }
        }
        return token.length() == 0 ? null : token.toString();
    }

    private String stripKnownExtension(String value) {
        if (value == null) {
            return "";
        }
        if (value.endsWith(".dll") || value.endsWith(".jar")) {
            return value.substring(0, value.length() - 4);
        }
        return value;
    }

    String separatorsToSystem(String res) {
        if (res == null) {
            return null;
        }
        if (File.separatorChar == '\\') {
            // From Linux/Mac to Windows
            return res.replace('/', File.separatorChar);
        } else {
            // From Windows to Linux/Mac
            return res.replace('\\', File.separatorChar);
        }
    }
}
