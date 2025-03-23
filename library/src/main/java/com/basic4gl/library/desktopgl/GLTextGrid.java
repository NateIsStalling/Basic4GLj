package com.basic4gl.library.desktopgl;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import com.basic4gl.lib.util.FileOpener;
import com.basic4gl.runtime.HasErrorState;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Created by Nate on 8/25/2015.
 */
public class GLTextGrid extends HasErrorState {
	public static final byte DRAW_TEXT = 1;
	private static final double OFFSET = 0.000;
	// GLuint
	private int m_texture;

	/**
	 * Character set image. Because OpenGL context keeps getting re-created,
	 * we need to keep loading it into our texture!
	 */
	private Image m_image; //

	private int m_texRows, m_texCols; // Rows and columns in texture
	private double m_charXSize, m_charYSize; // Character size in texture coordinates
	private int m_rows, m_cols; // Rows and columns in grid
	private char[] m_chars; // Character grid
	// unsigned long *
	private ByteBuffer m_colours; // Colour grid
	// GLuint *
	private int[] m_textures; // Texture handle grid
	private int m_cx, m_cy; // Cursor position
	private boolean m_showCursor;
	// unsigned
	private long m_currentColour; // Current colour
	// GLuint
	private int m_currentTexture; // Current texture font
	private boolean m_scroll;

	public static long makeColour(short r, short g, short b) {
		return makeColour(r, g, b, (short) 255);
	}

	public static long makeColour(short r, short g, short b, short alpha) {
		return r | (long) g << 8 | (long) b << 16 | (long) alpha << 24;
	}

	GLTextGrid(String texFile, FileOpener files, int rows, int cols, int texRows, int texCols) {
		m_rows = rows;
		m_cols = cols;
		m_texRows = texRows;
		m_texCols = texCols;
		assertTrue(m_rows > 0);
		assertTrue(m_cols > 0);
		assertTrue(m_texRows > 0);
		assertTrue(m_texCols > 0);

		// Initialise defaults
		m_showCursor = false;
		m_cx = 0;
		m_cy = 0;
		m_texture = 0;
		m_chars = null;
		m_colours = null;
		m_textures = null;
		m_currentColour = makeColour((short) 220, (short) 220, (short) 255);
		m_currentTexture = 0;
		m_scroll = true;

		// Load charset texture
		glPushAttrib(GL_ALL_ATTRIB_BITS);
		glEnable(GL_TEXTURE_2D);
		LoadImage.init(files);
		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);
		m_image = LoadImage.loadImage(texFile);
		if (m_image == null || m_image.getPixels() == null) {
			setError("Error loading bitmap: " + texFile);
			return;
		}
		uploadCharsetTexture();

		// Calculate character size (in texture coordinates)
		m_charXSize = 1.0 / m_texCols;
		m_charYSize = 1.0 / m_texRows;

		// Allocate character map
		m_chars = new char[m_rows * m_cols];
		m_colours = BufferUtils.createByteBuffer(m_rows * m_cols * (Long.SIZE / Byte.SIZE));
		m_textures = new int[m_rows * m_cols];
		clear();

		// TODO 12/16/2020 - glDisable may be needed; GLTextGrid seems to be causing issues with
		// GL_QUADS, SaveGLState might not be working as expected
		glDisable(GL_TEXTURE_2D);
	}

	protected void saveGLState() {

		// Preserve OpenGL state
		glPushAttrib(GL_ALL_ATTRIB_BITS);
		glMatrixMode(GL_TEXTURE);
		glPushMatrix();
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();

		// Setup state for 2D rendering
		glDisable(GL_FOG);
		glDisable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);
		glDisable(GL_SCISSOR_TEST);
		glDisable(GL_STENCIL_TEST);

		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	protected void restoreGLState() {

		// Restore preserved OpenGL state
		glMatrixMode(GL_TEXTURE);
		glPopMatrix();
		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
		glPopAttrib();
	}

	// virtual
	void internalDraw(byte flags) {

		// Draw text flag must be present
		// TODO test possible values of flags
		if (((flags & DRAW_TEXT) == 0)) {
			return;
		}

		// Create perspective transform
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		// PORTING NOTES: Basic4GL 2.5 source uses gluOrtho2D(0, 0, 1, 1) here when creating the
		// perspective transform.
		// gluOrtho2D would normally be replaced by glOrtho(0, 0, 1, 1, -1, 1) here for LWJGL 3
		// based on the C specifications below,
		// but this appears to cause text display issues on Windows 10/11 with a GL_INVALID_VALUE error.
		// Additionally, running the original Basic4GL 2.6 exe on Windows 10/11 appears to replicate
		// this error.
		// For this port, the error appears to be corrected by excluding glOrtho here,
		// but this change may require additional testing for any unexpected behavior -
		// no issues are anticipated since glLoadIdentity() and saveGLState()
		// are called beforehand here and in the original source.
		// C specifications:
		// - GL_INVALID_VALUE is generated if left = right, or bottom = top, or near = far.
		// - void gluOrtho2D(GLdouble left,
		// 	GLdouble right,
		// 	GLdouble bottom,
		// 	GLdouble top);
		// - void glOrtho(GLdouble left,
		// 	GLdouble right,
		// 	GLdouble bottom,
		// 	GLdouble top,
		// 	GLdouble nearVal,
		// 	GLdouble farVal);

		// Clear model view matrix (to identity)
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		// Iterate through character map
		int index = 0;
		double xStep = 2.0 / m_cols, // Character size on screen in screen coordinates
				yStep = 2.0 / m_rows;
		int lastTex = 0;

		for (int y = 0; y < m_rows; y++) {
			for (int x = 0; x < m_cols; x++) {

				// Get current and previous character
				char c = m_chars[index];
				m_colours.position(index * (Long.SIZE / Byte.SIZE));
				int tex = m_textures[index];

				// Redraw character if changed
				// Find character 2D position in texture
				if (c != ' ') {
					int charx = c % m_texCols;
					int chary = c / m_texCols;

					// Convert to texture coordinates
					double tx = charx * m_charXSize + OFFSET, ty = 1.0 - (chary + 1) * m_charYSize + OFFSET;

					// Find screen coordinates
					double screenX = x * xStep - 1.0, screenY = 1.0 - (y + 1) * yStep;

					// Bind appropriate texture
					if (tex != lastTex) {
						glBindTexture(GL_TEXTURE_2D, tex);
						lastTex = tex;
					}

					// Write character
					glColor4ubv(m_colours);
					glBegin(GL_QUADS);
					glTexCoord2d(tx, ty);
					glVertex2d(screenX, screenY);
					glTexCoord2d(tx + m_charXSize, ty);
					glVertex2d(screenX + xStep, screenY);
					glTexCoord2d(tx + m_charXSize, ty + m_charYSize);
					glVertex2d(screenX + xStep, screenY + yStep);
					glTexCoord2d(tx, ty + m_charYSize);
					glVertex2d(screenX, screenY + yStep);
					glEnd();
				}
				index++;
			}
		}
	}

	/**
	 * Upload charset texture into OpenGL
	 */
	public void uploadCharsetTexture() {
		IntBuffer tempTexture =
				BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE).asIntBuffer();
		tempTexture.put(m_texture);
		tempTexture.rewind();
		// Upload the character set texture into OpenGL
		glGenTextures(tempTexture);
		m_texture = tempTexture.get(0);
		glBindTexture(GL_TEXTURE_2D, m_texture);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);

		/*
		//GLU deprecated

		gluBuild2DMipmaps ( GL_TEXTURE_2D,
				(m_image.getFormat () & 0xffff) == corona::PF_R8G8B8 ? 3 : 4,
				m_image.getWidth (),
				m_image.getHeight (),
				ImageFormat (m_image),
				GL_UNSIGNED_BYTE,
				m_image.getPixels ());*/
		// Create a new texture object in memory and bind it
		// Upload the texture data and generate mip maps (for scaling)
		// TODO get image format properly; currently hard coded as GL_RGBA
		GL11.glTexImage2D(
				GL11.GL_TEXTURE_2D,
				0,
				GL_RGBA,
				m_image.getWidth(),
				m_image.getHeight(),
				0,
				GL_RGBA,
				GL11.GL_UNSIGNED_BYTE,
				m_image.getPixels());
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		//        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);
		m_currentTexture = m_texture;
	}

	public void resize(int rows, int cols) {
		assertTrue(rows > 0);
		assertTrue(cols > 0);

		m_rows = rows;
		m_cols = cols;

		// Reallocate text grid
		m_chars = null;
		m_chars = new char[m_rows * m_cols];

		m_colours = null;
		m_colours = BufferUtils.createByteBuffer(m_rows * m_cols * (Long.SIZE / Byte.SIZE));

		m_textures = null;
		m_textures = new int[m_rows * m_cols];

		clear();
	}

	public int getRows() {
		return m_rows;
	}

	public int getColumns() {
		return m_cols;
	}

	// Drawing
	public void draw(byte flags) {
		saveGLState();
		internalDraw(flags);
		restoreGLState();
	}

	// Scrolling
	public void scrollUp() {

		// Move rows up
		int x, y;
		for (y = 1; y < m_rows; y++) {
			for (x = 0; x < m_cols; x++) {
				m_chars[((y - 1) * m_cols) + x] = m_chars[(y * m_cols) + x];
			}
		}
		// Clear bottom line
		Arrays.fill(m_chars, (m_rows - 1) * m_cols, ((m_rows - 1) * m_cols) + m_cols, ' ');

		// Do the same to the colour map
		m_colours.rewind();
		LongBuffer col = m_colours.asLongBuffer();
		for (y = 1; y < m_rows; y++) {
			for (x = 0; x < m_cols; x++) {
				col.put(((y - 1) * m_cols) + x, col.get((y * m_cols) + x));
			}
		}

		// And the texture line
		for (y = 1; y < m_rows; y++) {
			for (x = 0; x < m_cols; x++) {
				m_textures[((y - 1) * m_cols) + x] = m_textures[(y * m_cols) + y];
			}
		}

		// Move cursor up
		cursorUp();
	}

	public boolean getScroll() {
		return m_scroll;
	}

	public void setScroll(boolean scroll) {
		m_scroll = scroll;
	}

	// Cursor commands
	public int getCursorX() {
		return m_cx;
	}

	public int getCursorY() {
		return m_cy;
	}

	public void showCursor() {
		m_showCursor = true;
	}

	public void hideCursor() {
		m_showCursor = false;
	}

	public void setCursorPosition(int x, int y) {
		if (x < 0) {
			x = 0;
		}
		if (x >= m_cols) {
			x = m_cols - 1;
		}
		if (y < 0) {
			y = 0;
		}
		if (y >= m_rows) {
			y = m_rows - 1;
		}
		m_cx = x;
		m_cy = y;
	}

	public void cursorUp() {
		if (--m_cy < 0) {
			m_cy = 0;
		}
	}

	public void cursorDown() {
		if (++m_cy >= m_rows) {
			if (m_scroll) {
				scrollUp();
			} else {
				cursorUp();
			}
		}
	}

	public void cursorLeft() {
		if (--m_cx < 0) {
			m_cx = 0;
		}
	}

	public void newLine() {
		m_cx = 0;
		cursorDown();
	}

	public void cursorRight() {
		cursorRight(1);
	}

	public void cursorRight(int dist) {
		m_cx += dist;
		while (m_cx >= m_cols) {
			m_cx -= m_cols;
			cursorDown();
		}
	}

	// Insert/delete space
	public void delete() {
		m_colours.rewind();
		LongBuffer col = m_colours.asLongBuffer();

		// Shift text to the left
		int lineOffset = m_cy * m_cols;
		for (int x = m_cx; x < m_cols - 1; x++) {
			m_chars[lineOffset + x] = m_chars[lineOffset + x + 1];
			col.put(lineOffset + x, col.get(lineOffset + x + 1));
		}

		// Insert space on the right
		m_chars[lineOffset + m_cols - 1] = ' ';
	}

	public boolean insert() {
		m_colours.rewind();
		LongBuffer col = m_colours.asLongBuffer();

		// Room to insert a space?
		int lineOffset = m_cy * m_cols;
		if (m_chars[lineOffset + m_cols - 1] <= ' ') {

			// Shift text to the right
			for (int x = m_cols - 1; x > m_cx; x--) {
				m_chars[lineOffset + x] = m_chars[lineOffset + x - 1];
				col.put(lineOffset + x, col.get(lineOffset + x - 1));
			}

			// Insert space
			m_chars[lineOffset + m_cx] = ' ';

			return true;
		} else {
			return false;
		}
	}

	public boolean backspace() {
		if (m_cx > 0) {
			--m_cx;
			delete();
			return true;
		} else {
			return false;
		}
	}

	// Writing commands
	public void clear() {

		// Clear character map
		Arrays.fill(m_chars, ' ');

		// Move cursor home
		setCursorPosition(0, 0);
	}

	public void write(String s) {

		// Write out string. Split it around rows.
		int len = s.length(), index = 0;
		while (len > 0) {

			// Determine length of next substring
			int subLen = m_cols - m_cx;
			if (subLen > len) {
				subLen = len;
			}

			// Write out bit
			int offset = m_cy * m_cols + m_cx;
			if (subLen > 0) {
				// TODO properly set characters
				for (int i = 0; i < subLen; i++) {
					m_chars[offset + i] = s.charAt(index + i);
				}
				// Arrays.fill(m_chars, offset, subLen ,s.charAt(index));
				index += subLen;
				len -= subLen;
			}

			// Set colours and textures
			int x;
			m_colours.rewind();
			LongBuffer col = m_colours.asLongBuffer();
			for (x = 0; x < subLen; x++) {
				col.put(offset + x, m_currentColour);
			}
			for (x = 0; x < subLen; x++) {
				m_textures[offset + x] = m_currentTexture;
			}

			// Advance cursor
			cursorRight(subLen);
		}
	}

	public void clearLine() {

		// Clear the current line
		Arrays.fill(m_chars, m_cy * m_cols, (m_cy * m_cols) + m_cols, ' ');
	}

	public void clearRegion(int x1, int y1, int x2, int y2) {

		// Validate region
		if (x1 < 0) {
			x1 = 0;
		}
		if (y1 < 0) {
			y1 = 0;
		}
		if (x2 >= m_cols) {
			x2 = m_cols - 1;
		}
		if (y2 >= m_rows) {
			y2 = m_rows - 1;
		}
		if (x2 >= x1 && y2 >= y1)

		// Clear it
		{
			for (int y = y1; y <= y2; y++) {
				Arrays.fill(m_chars, y * m_cols + x1, (y * m_cols + x1) + x2 - x1 + 1, ' ');
			}
		}
	}

	// Reading commands
	public char getTextAt(int x, int y) {
		if (x < 0 || x >= m_cols || y < 0 || y >= m_rows) {
			return 0;
		} else {
			return m_chars[y * m_cols + x];
		}
	}

	// unsigned
	public long getColourAt(int x, int y) {
		m_colours.rewind();
		LongBuffer col = m_colours.asLongBuffer();
		if (x < 0 || x >= m_cols || y < 0 || y >= m_rows) {
			return m_currentColour;
		} else {
			return col.get(y * m_cols + x);
		}
	}

	// GLuint
	public int getTextureAt(int x, int y) {
		if (x < 0 || x >= m_cols || y < 0 || y >= m_rows) {
			return m_currentTexture;
		} else {
			return m_textures[y * m_cols + x];
		}
	}

	// Colour commands
	// unsigned
	public long getColour() {
		return m_currentColour;
	}

	// SetColour (unsigned long col)
	public void setColour(long col) {
		m_currentColour = col;
	}

	// Texture commands
	// GLuint
	public int getTexture() {
		return m_currentTexture;
	}

	// SetTexture (GLuint tex)
	public void setTexture(int tex) {
		m_currentTexture = tex;
	}

	// GLuint
	public int getDefaultTexture() {
		return m_texture;
	}

	// Input commands

	/**
	 * Allows user to type in a string. Returns string upon exit.
	 * @param window
	 * @return
	 */
	public String getString(GLWindow window) {

		assertTrue(window != null);
		m_colours.rewind();
		LongBuffer col = m_colours.asLongBuffer();

		// Record leftmost cursor position
		// Cursor can not be moved further left than that point
		int left = m_cx;
		boolean saveCursor = m_showCursor;
		m_showCursor = true;

		// Perform UI
		boolean done = false;
		while (!(window.isClosing() || done)) {

			glClear(GL_COLOR_BUFFER_BIT);
			draw((byte) 0xff);
			window.swapBuffers();

			// Keyboard input
			int c = 0, sc = 0;
			while (c == 0 && sc == 0 && !window.isClosing()) {
				sc = window.getScanKey();
				if (sc == 0) {
					c = window.getKey();
				}
				// Go easy on the processor
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// Keep window responsive
				glfwPollEvents();
			}
			if (window.isClosing()) {
				return "";
			}
			switch (sc) {
				case GLFW_KEY_LEFT:
					if (m_cx > left) {
						m_cx--;
					}
					break;
				case GLFW_KEY_RIGHT:
					if (m_cx < m_cols - 1) {
						m_cx++;
					}
					break;
				case GLFW_KEY_DELETE:
					delete();
					break;
				case GLFW_KEY_BACKSPACE:
					if (m_cx > left) {
						backspace();
					}
					break;
			}
			if (sc == GLFW_KEY_ENTER) {
				done = true;
			}
			if (c >= ' ') {
				if (m_cx < m_cols - 1 && insert()) {
					m_chars[m_cy * m_cols + m_cx] = (char) c;
					col.put(m_cy * m_cols + m_cx, m_currentColour);
					m_textures[m_cy * m_cols + m_cx] = m_currentTexture;
					m_cx++;
				}
			}
			// Go easy on the processor
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Keep window responsive
			glfwPollEvents();
		}
		if (window.isClosing()) {
			return "";
		}
		// Extract string
		int lineOffset = m_cy * m_cols;
		int right = m_cols;
		while (right > left && m_chars[lineOffset + right - 1] <= ' ') // Trim spaces from right
		{
			right--;
		}
		while (left < right && m_chars[lineOffset + left] <= ' ') // Trim spaces from left
		{
			left++;
		}
		String result = "";
		for (int i = left; i < right; i++) {
			result += m_chars[lineOffset + i];
		}

		// Restore cursor, perform newline and update screen
		newLine();
		m_showCursor = saveCursor;
		glClear(GL_COLOR_BUFFER_BIT);
		draw((byte) 0xff);
		window.swapBuffers();

		return result;
	}

	/**
	 * release image from memory
	 */
	void destroy() {
		/*if (m_image != null)
			stbi_image_free(m_image.getPixels());
		m_image = null;*/
	}
}
