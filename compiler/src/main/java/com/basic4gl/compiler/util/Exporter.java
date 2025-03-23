package com.basic4gl.compiler.util;

import java.io.*;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.List;
import java.util.jar.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class Exporter {

/**
* Copy files from inside the current running Java application to a JarOutputStream
*
* @param files List of files and directories to copy
* @param excludeRegex Exclude files matching this pattern
* @param target Jar to copy files to
* @throws IOException
*/
public static void addSource(List<String> files, String excludeRegex, JarOutputStream target)
	throws IOException {
	String tempPath = null; // Last valid path in the files list
	boolean copy; // Should file be copied
	HashSet<String> entryNames = new HashSet<>(); // Avoid duplicate entry ZipExceptions

	// Get location of application
	CodeSource src = Exporter.class.getProtectionDomain().getCodeSource();

	Pattern excludePattern = Pattern.compile(excludeRegex, Pattern.CASE_INSENSITIVE);

	if (src != null) {
	URL jar = src.getLocation();
	System.out.println("Source jar:");
	System.out.println(jar);
	JarInputStream input = new JarInputStream(jar.openStream());
	while (true) {
		ZipEntry e = input.getNextEntry();
		if (e == null) {
		break;
		}
		String name = e.getName();

		if (name.startsWith("META-INF/")) // Do not copy manifest to destination
		{
		continue;
		}

		// Check if file should be added
		copy = false;
		Matcher excludeFileMatcher = excludePattern.matcher(name);

		if (excludeFileMatcher.matches()) {
		// skip
		continue;
		}

		// Check if entry name matches cached name
		if (tempPath != null && name.startsWith(tempPath)) {
		Matcher excludePathMatcher = excludePattern.matcher(tempPath);

		if (!excludePathMatcher.matches()) {
			copy = true;
		}
		}

		if (!copy) {
		for (String file : files) // Check if entry name is in file list
		{
			if (name.startsWith(file)) {
			tempPath = file; // Cache path
			copy = true;
			break;
			}
		}
		}
		// Copy file to destination
		if (copy && entryNames.add(name)) {
		target.putNextEntry(e);
		for (int c = input.read(); c != -1; c = input.read()) {
			target.write(c);
		}
		System.out.println("Added: " + name);
		target.closeEntry();
		}
	}

	} else {
	System.err.println("Source not recognized");
	}
}
}
