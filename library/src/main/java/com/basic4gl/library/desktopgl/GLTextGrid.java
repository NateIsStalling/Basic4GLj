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
    private int texture;

    /**
     * Character set image. Because OpenGL context keeps getting re-created,
     * we need to keep loading it into our texture!
     */
    private Image image; //

    private int textureRows, textureColumns; // Rows and columns in texture
    private double charXSize, charYSize; // Character size in texture coordinates
    private int rows, columns; // Rows and columns in grid
    private char[] chars; // Character grid
    // unsigned long *
    private ByteBuffer colours; // Colour grid
    // GLuint *
    private int[] textures; // Texture handle grid
    private int cursorX, cursorY; // Cursor position
    private boolean showCursor;
    // unsigned
    private long currentColour; // Current colour
    // GLuint
    private int currentTexture; // Current texture font
    private boolean scroll;

    public static long makeColour(short r, short g, short b) {
        return makeColour(r, g, b, (short) 255);
    }

    public static long makeColour(short r, short g, short b, short alpha) {
        return r | (long) g << 8 | (long) b << 16 | (long) alpha << 24;
    }

    GLTextGrid(String texFile, FileOpener files, int rows, int cols, int texRows, int texCols) {
        this.rows = rows;
        columns = cols;
        textureRows = texRows;
        textureColumns = texCols;
        assertTrue(this.rows > 0);
        assertTrue(columns > 0);
        assertTrue(textureRows > 0);
        assertTrue(textureColumns > 0);

        // Initialise defaults
        showCursor = false;
        cursorX = 0;
        cursorY = 0;
        texture = 0;
        chars = null;
        colours = null;
        textures = null;
        currentColour = makeColour((short) 220, (short) 220, (short) 255);
        currentTexture = 0;
        scroll = true;

        // Load charset texture
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glEnable(GL_TEXTURE_2D);
        LoadImage.init(files);
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
        image = LoadImage.loadImage(texFile);
        if (image == null || image.getPixels() == null) {
            setError("Error loading bitmap: " + texFile);
            return;
        }
        uploadCharsetTexture();

        // Calculate character size (in texture coordinates)
        charXSize = 1.0 / textureColumns;
        charYSize = 1.0 / textureRows;

        // Allocate character map
        chars = new char[this.rows * columns];
        colours = BufferUtils.createByteBuffer(this.rows * columns * (Long.SIZE / Byte.SIZE));
        textures = new int[this.rows * columns];
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
        double xStep = 2.0 / columns, // Character size on screen in screen coordinates
                yStep = 2.0 / rows;
        int lastTex = 0;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {

                // Get current and previous character
                char c = chars[index];
                colours.position(index * (Long.SIZE / Byte.SIZE));
                int tex = textures[index];

                // Redraw character if changed
                // Find character 2D position in texture
                if (c != ' ') {
                    int charx = c % textureColumns;
                    int chary = c / textureColumns;

                    // Convert to texture coordinates
                    double tx = charx * charXSize + OFFSET, ty = 1.0 - (chary + 1) * charYSize + OFFSET;

                    // Find screen coordinates
                    double screenX = x * xStep - 1.0, screenY = 1.0 - (y + 1) * yStep;

                    // Bind appropriate texture
                    if (tex != lastTex) {
                        glBindTexture(GL_TEXTURE_2D, tex);
                        lastTex = tex;
                    }

                    // Write character
                    glColor4ubv(colours);
                    glBegin(GL_QUADS);
                    glTexCoord2d(tx, ty);
                    glVertex2d(screenX, screenY);
                    glTexCoord2d(tx + charXSize, ty);
                    glVertex2d(screenX + xStep, screenY);
                    glTexCoord2d(tx + charXSize, ty + charYSize);
                    glVertex2d(screenX + xStep, screenY + yStep);
                    glTexCoord2d(tx, ty + charYSize);
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
        tempTexture.put(texture);
        tempTexture.rewind();
        // Upload the character set texture into OpenGL
        glGenTextures(tempTexture);
        texture = tempTexture.get(0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);

        /*
        //GLU deprecated

        gluBuild2DMipmaps ( GL_TEXTURE_2D,
        		(this.image.getFormat() & 0xffff) == corona::PF_R8G8B8 ? 3 : 4,
        		this.image.getWidth(),
        		this.image.getHeight(),
        		ImageFormat (this.image),
        		GL_UNSIGNED_BYTE,
        		this.image.getPixels ());*/
        // Create a new texture object in memory and bind it
        // Upload the texture data and generate mip maps (for scaling)
        // TODO get image format properly; currently hard coded as GL_RGBA
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL_RGBA,
                image.getWidth(),
                image.getHeight(),
                0,
                GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                image.getPixels());
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        //        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);
        currentTexture = texture;
    }

    public void resize(int rows, int cols) {
        assertTrue(rows > 0);
        assertTrue(cols > 0);

        this.rows = rows;
        columns = cols;

        // Reallocate text grid
        chars = null;
        chars = new char[this.rows * columns];

        colours = null;
        colours = BufferUtils.createByteBuffer(this.rows * columns * (Long.SIZE / Byte.SIZE));

        textures = null;
        textures = new int[this.rows * columns];

        clear();
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
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
        for (y = 1; y < rows; y++) {
            for (x = 0; x < columns; x++) {
                chars[((y - 1) * columns) + x] = chars[(y * columns) + x];
            }
        }
        // Clear bottom line
        Arrays.fill(chars, (rows - 1) * columns, ((rows - 1) * columns) + columns, ' ');

        // Do the same to the colour map
        colours.rewind();
        LongBuffer col = colours.asLongBuffer();
        for (y = 1; y < rows; y++) {
            for (x = 0; x < columns; x++) {
                col.put(((y - 1) * columns) + x, col.get((y * columns) + x));
            }
        }

        // And the texture line
        for (y = 1; y < rows; y++) {
            for (x = 0; x < columns; x++) {
                textures[((y - 1) * columns) + x] = textures[(y * columns) + y];
            }
        }

        // Move cursor up
        cursorUp();
    }

    public boolean getScroll() {
        return scroll;
    }

    public void setScroll(boolean scroll) {
        this.scroll = scroll;
    }

    // Cursor commands
    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public void showCursor() {
        showCursor = true;
    }

    public void hideCursor() {
        showCursor = false;
    }

    public void setCursorPosition(int x, int y) {
        if (x < 0) {
            x = 0;
        }
        if (x >= columns) {
            x = columns - 1;
        }
        if (y < 0) {
            y = 0;
        }
        if (y >= rows) {
            y = rows - 1;
        }
        cursorX = x;
        cursorY = y;
    }

    public void cursorUp() {
        if (--cursorY < 0) {
            cursorY = 0;
        }
    }

    public void cursorDown() {
        if (++cursorY >= rows) {
            if (scroll) {
                scrollUp();
            } else {
                cursorUp();
            }
        }
    }

    public void cursorLeft() {
        if (--cursorX < 0) {
            cursorX = 0;
        }
    }

    public void newLine() {
        cursorX = 0;
        cursorDown();
    }

    public void cursorRight() {
        cursorRight(1);
    }

    public void cursorRight(int dist) {
        cursorX += dist;
        while (cursorX >= columns) {
            cursorX -= columns;
            cursorDown();
        }
    }

    // Insert/delete space
    public void delete() {
        colours.rewind();
        LongBuffer col = colours.asLongBuffer();

        // Shift text to the left
        int lineOffset = cursorY * columns;
        for (int x = cursorX; x < columns - 1; x++) {
            chars[lineOffset + x] = chars[lineOffset + x + 1];
            col.put(lineOffset + x, col.get(lineOffset + x + 1));
        }

        // Insert space on the right
        chars[lineOffset + columns - 1] = ' ';
    }

    public boolean insert() {
        colours.rewind();
        LongBuffer col = colours.asLongBuffer();

        // Room to insert a space?
        int lineOffset = cursorY * columns;
        if (chars[lineOffset + columns - 1] <= ' ') {

            // Shift text to the right
            for (int x = columns - 1; x > cursorX; x--) {
                chars[lineOffset + x] = chars[lineOffset + x - 1];
                col.put(lineOffset + x, col.get(lineOffset + x - 1));
            }

            // Insert space
            chars[lineOffset + cursorX] = ' ';

            return true;
        } else {
            return false;
        }
    }

    public boolean backspace() {
        if (cursorX > 0) {
            --cursorX;
            delete();
            return true;
        } else {
            return false;
        }
    }

    // Writing commands
    public void clear() {

        // Clear character map
        Arrays.fill(chars, ' ');

        // Move cursor home
        setCursorPosition(0, 0);
    }

    public void write(String s) {

        // Write out string. Split it around rows.
        int len = s.length(), index = 0;
        while (len > 0) {

            // Determine length of next substring
            int subLen = columns - cursorX;
            if (subLen > len) {
                subLen = len;
            }

            // Write out bit
            int offset = cursorY * columns + cursorX;
            if (subLen > 0) {
                // TODO properly set characters
                for (int i = 0; i < subLen; i++) {
                    chars[offset + i] = s.charAt(index + i);
                }
                // Arrays.fill(this.chars, offset, subLen ,s.charAt(index));
                index += subLen;
                len -= subLen;
            }

            // Set colours and textures
            int x;
            colours.rewind();
            LongBuffer col = colours.asLongBuffer();
            for (x = 0; x < subLen; x++) {
                col.put(offset + x, currentColour);
            }
            for (x = 0; x < subLen; x++) {
                textures[offset + x] = currentTexture;
            }

            // Advance cursor
            cursorRight(subLen);
        }
    }

    public void clearLine() {

        // Clear the current line
        Arrays.fill(chars, cursorY * columns, (cursorY * columns) + columns, ' ');
    }

    public void clearRegion(int x1, int y1, int x2, int y2) {

        // Validate region
        if (x1 < 0) {
            x1 = 0;
        }
        if (y1 < 0) {
            y1 = 0;
        }
        if (x2 >= columns) {
            x2 = columns - 1;
        }
        if (y2 >= rows) {
            y2 = rows - 1;
        }
        if (x2 >= x1 && y2 >= y1)

        // Clear it
        {
            for (int y = y1; y <= y2; y++) {
                Arrays.fill(chars, y * columns + x1, (y * columns + x1) + x2 - x1 + 1, ' ');
            }
        }
    }

    // Reading commands
    public char getTextAt(int x, int y) {
        if (x < 0 || x >= columns || y < 0 || y >= rows) {
            return 0;
        } else {
            return chars[y * columns + x];
        }
    }

    // unsigned
    public long getColourAt(int x, int y) {
        colours.rewind();
        LongBuffer col = colours.asLongBuffer();
        if (x < 0 || x >= columns || y < 0 || y >= rows) {
            return currentColour;
        } else {
            return col.get(y * columns + x);
        }
    }

    // GLuint
    public int getTextureAt(int x, int y) {
        if (x < 0 || x >= columns || y < 0 || y >= rows) {
            return currentTexture;
        } else {
            return textures[y * columns + x];
        }
    }

    // Colour commands
    // unsigned
    public long getColour() {
        return currentColour;
    }

    // SetColour (unsigned long col)
    public void setColour(long col) {
        currentColour = col;
    }

    // Texture commands
    // GLuint
    public int getTexture() {
        return currentTexture;
    }

    // SetTexture (GLuint tex)
    public void setTexture(int tex) {
        currentTexture = tex;
    }

    // GLuint
    public int getDefaultTexture() {
        return texture;
    }

    // Input commands

    /**
     * Allows user to type in a string. Returns string upon exit.
     * @param window
     * @return
     */
    public String getString(GLWindow window) {

        assertTrue(window != null);
        colours.rewind();
        LongBuffer col = colours.asLongBuffer();

        // Record leftmost cursor position
        // Cursor can not be moved further left than that point
        int left = cursorX;
        boolean saveCursor = showCursor;
        showCursor = true;

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
                    if (cursorX > left) {
                        cursorX--;
                    }
                    break;
                case GLFW_KEY_RIGHT:
                    if (cursorX < columns - 1) {
                        cursorX++;
                    }
                    break;
                case GLFW_KEY_DELETE:
                    delete();
                    break;
                case GLFW_KEY_BACKSPACE:
                    if (cursorX > left) {
                        backspace();
                    }
                    break;
            }
            if (sc == GLFW_KEY_ENTER) {
                done = true;
            }
            if (c >= ' ') {
                if (cursorX < columns - 1 && insert()) {
                    chars[cursorY * columns + cursorX] = (char) c;
                    col.put(cursorY * columns + cursorX, currentColour);
                    textures[cursorY * columns + cursorX] = currentTexture;
                    cursorX++;
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
        int lineOffset = cursorY * columns;
        int right = columns;
        while (right > left && chars[lineOffset + right - 1] <= ' ') // Trim spaces from right
        {
            right--;
        }
        while (left < right && chars[lineOffset + left] <= ' ') // Trim spaces from left
        {
            left++;
        }
        String result = "";
        for (int i = left; i < right; i++) {
            result += chars[lineOffset + i];
        }

        // Restore cursor, perform newline and update screen
        newLine();
        showCursor = saveCursor;
        glClear(GL_COLOR_BUFFER_BIT);
        draw((byte) 0xff);
        window.swapBuffers();

        return result;
    }

    /**
     * release image from memory
     */
    void destroy() {
        /*if (this.image != null)
        	stbi_image_free(this.image.getPixels());
        this.image = null;*/
    }
}
