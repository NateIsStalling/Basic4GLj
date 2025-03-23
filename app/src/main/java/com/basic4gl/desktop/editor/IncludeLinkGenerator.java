package com.basic4gl.desktop.editor;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import javax.swing.event.HyperlinkEvent;
import org.fife.ui.rsyntaxtextarea.LinkGenerator;
import org.fife.ui.rsyntaxtextarea.LinkGeneratorResult;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class IncludeLinkGenerator implements LinkGenerator {
	private static final String INCLUDE = "include ";

	/**
	 * Separators used to determine words in text.
	 */
	private final java.util.List<String> textSeparators = Arrays.asList("\n");

	// Arrays.asList(",", ";", "\n", "|", "{", "}", "[", "]", "=", "\"", "'", "*", "%", "&", "?");

	private final ITabProvider tabProvider;

	public IncludeLinkGenerator(ITabProvider tabProvider) {
		this.tabProvider = tabProvider;
	}

	@Override
	public LinkGeneratorResult isLinkAtOffset(RSyntaxTextArea source, final int pos) {
		final String code = source.getText();
		final int wordStart = getWordStart(code, pos);
		final int wordEnd = getWordEnd(code, pos);
		final String word = code.substring(wordStart, wordEnd);
		final String link;
		final Dimension key;

		final LinkGeneratorResult value;

		if (word.startsWith(INCLUDE)) {
			link = code.substring(wordStart + INCLUDE.length(), wordEnd).trim();
			key = new Dimension(wordStart + INCLUDE.length(), wordEnd);
		} else {
			return null;
		}

		if (word != null) {
			value = new LinkGeneratorResult() {
				@Override
				public HyperlinkEvent execute() {
					String filename = separatorsToSystem(link);

					int index;

					index = tabProvider.getTabIndex(filename);

					if (index != -1) {
						tabProvider.setSelectedTabIndex(index);
					} else {
						tabProvider.openTab(filename);
					}

					return new HyperlinkEvent(this, HyperlinkEvent.EventType.EXITED, null);
				}

				@Override
				public int getSourceOffset() {
					return wordStart;
				}
			};
		} else {
			value = null;
		}
		return value;
	}

	/**
	 * Returns a word start index at the specified location.
	 *
	 * @param text     text to retrieve the word start index from
	 * @param location word location
	 * @return word start index
	 */
	public int getWordStart(final String text, final int location) {
		int wordStart = location;
		while (wordStart > 0 && !textSeparators.contains(text.substring(wordStart - 1, wordStart))) {
			wordStart--;
		}
		return wordStart;
	}

	/**
	 * Returns a word end index at the specified location.
	 *
	 * @param text     text to retrieve the word end index from
	 * @param location word location
	 * @return word end index
	 */
	public int getWordEnd(final String text, final int location) {
		int wordEnd = location;
		while (wordEnd < text.length() && !textSeparators.contains(text.substring(wordEnd, wordEnd + 1))) {
			wordEnd++;
		}
		return wordEnd;
	}

	String separatorsToSystem(String res) {
		if (res == null) {
			return null;
		}
		if (File.separatorChar == '\\') {
			// From Windows to Linux/Mac
			return res.replace('/', File.separatorChar);
		} else {
			// From Linux/Mac to Windows
			return res.replace('\\', File.separatorChar);
		}
	}
}
