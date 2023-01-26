package com.basic4gl.library.desktopgl;

import com.basic4gl.library.standard.TrigBasicLib;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Vector;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.opengl.GL11.*;
/**
 * Created by Nate on 11/2/2015.
 */
////////////////////////////////////////////////////////////////////////////////
// GLTileMap
//
// A 2D grid of texture tiles for use in 2D sprite drawing.
// Can be manipulated as a glBasicSprite.
//
// Textures are loaded with SetTexture(s) or AddTexture(s). The first texture
// loaded is index 0. The tile map is specified as a 2D grid of integers, each
// corresponding to an index in the textures array.

public class GLTileMap extends GLBasicSprite {

    private int m_xTiles, m_yTiles;
    private Vector<Integer> m_tiles;

    private void SetDefaults() {
        m_xTiles = 0;
        m_yTiles = 0;
        m_xCentre = 0;
        m_yCentre = 0;
        m_xRepeat = true;
        m_yRepeat = true;
        m_solid = true;
    }


    protected void InternalCopy(GLBasicSprite s) {
        super.InternalCopy(s);
        GLTileMap t = (GLTileMap) s;
        SetTiles(t.m_xTiles, t.m_yTiles, t.m_tiles);
    }


    public boolean m_xRepeat, m_yRepeat;

    // Construction/destruction
    public GLTileMap() {
        super();
        SetDefaults ();
    }

    public GLTileMap(int tex) {
        super(tex);
        SetDefaults();
    }

    public GLTileMap(Vector<Integer> tex) {
        super(tex);
        SetDefaults();
    }

    // Class type identification
    public GLSpriteEngine.GLSpriteType Type() {
        return GLSpriteEngine.GLSpriteType.SPR_TILEMAP;
    }

    // Getters/Setters
    public int XTiles() {
        return m_xTiles;
    }

    public int YTiles() {
        return m_yTiles;
    }

    public Vector<Integer> Tiles() {
        return m_tiles;
    }

    public void SetTiles(int xTiles, int yTiles, Vector<Integer> tiles) {
        assertTrue(xTiles >= 0);
        assertTrue(yTiles >= 0);
        assertTrue(tiles.size() >= xTiles * yTiles);
        m_xTiles = xTiles;
        m_yTiles = yTiles;
        if (xTiles > 0 || yTiles > 0) {
            m_tiles = tiles;
        } else {
            m_tiles.clear();
        }
    }

    // Rendering
    public void Render(float[] camInv) {

        // Render tile map using OpenGL commands

        // Assumes that the appropriate projection/translation matrices have been setup,
        // and other OpenGL state (such as texturing & transparency) has been setup
        // accordingly.

        // Sprite must be visible
        if (!m_visible || m_xTiles == 0 || m_yTiles == 0) {
            return;
        }

        ByteBuffer byteBuf = BufferUtils.createByteBuffer(m_colour.length * 4); //4 bytes per float
        FloatBuffer buffer = byteBuf.asFloatBuffer();
        buffer.put(m_colour);
        buffer.position(0);
        glColor4fv(buffer);
        buffer.rewind();
        buffer.get(m_colour);

        // Setup translation, rotation and scaling.
        // Note:    We will setup 2 matrices.
        //          First the OpenGL matrix to perform the operations to drawn elements.
        //          Second is an INVERSE of all the operations. This will be used to
        //          determine where the screen corners map to the tile map, so we
        //          can calculate what range of tiles needs to be drawn.
        float m1[] = new float[16], m2[] = new float[16];
        glPushMatrix();

        // Translate to object position
        glTranslatef(m_x, m_y, 0);
        TrigBasicLib.Translate(-m_x, -m_y, 0);
        TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), camInv, m1);

        // Rotate by angle
        if (m_angle != 0) {
            glRotatef(m_angle, 0, 0, 1);
            TrigBasicLib.RotateZ(-m_angle);
            TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m1, m2);
        } else {
            TrigBasicLib.CopyMatrix(m2, m1);
        }

        // Scale to tile size
        glScalef(m_xSize * m_scale,
                m_ySize * m_scale,
                1);
        TrigBasicLib.Scale(1.0f / (m_xSize * m_scale),
                1.0f / (m_ySize * m_scale),
                1);
        TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m2, m1);

        // Centre offset
        if (m_xCentre != 0 || m_yCentre != 0) {
            glTranslatef(-m_xCentre, -m_yCentre, 0);
            TrigBasicLib.Translate(m_xCentre, m_yCentre, 0);
            TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m1, m2);
        } else {
            TrigBasicLib.CopyMatrix(m2, m1);
        }

        // Note: Flip not implemented yet!

        // Now we can use our m1 matrix to translate from camera space into tile space
        // Find range of tiles spanned.
        float camCorner[][] = new float[][]{{0, 0, 0, 1}, {1, 0, 0, 1}, {0, 1, 0, 1}, {1, 1, 0, 1}};
//    vmReal camCorner[4][4] = {{.1,.1, 0, 1}, {.9,.1, 0, 1}, {.1,.9, 0, 1}, {.9,.9, 0, 1} };       // DEBUGGING!!!
        int maxX = -1000000, minX = 1000000, maxY = -1000000, minY = 1000000;
        for (int i = 0; i < 4; i++) {
            float tileSpaceCorner[] = new float[4];
            TrigBasicLib.MatrixTimesVec(m2, camCorner[i], tileSpaceCorner);
            int x = (int) tileSpaceCorner[0], y = (int) tileSpaceCorner[1];
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }

        minX--;
        minY--;
        if (!m_xRepeat) {

            // Clamp x
            if (minX < 0) {
                minX = 0;
            }
            if (maxX >= m_xTiles) {
                maxX = m_xTiles - 1;
            }
        }
        if (!m_yRepeat) {

            // Clamp y
            if (minY < 0) {
                minY = 0;
            }
            if (maxY >= m_yTiles) {
                maxY = m_yTiles - 1;
            }
        }
        int startTileX = minX % m_xTiles, startTileY = minY % m_yTiles;
        if (startTileX < 0) {
            startTileX += m_xTiles;
        }
        if (startTileY < 0) {
            startTileY += m_yTiles;
        }

        // Draw tile map
        if (minX <= maxX && minY <= maxY) {
            int tileX = startTileX;
            for (int x = minX; x <= maxX; x++) {
                int offset = tileX * m_yTiles;
                int tileY = startTileY;
                for (int y = minY; y <= maxY; y++) {

                    assertTrue(tileY >= 0);
                    assertTrue(tileX >= 0);
                    assertTrue(tileY < m_yTiles);
                    assertTrue(tileX < m_xTiles);

                    // Find tile index. Only draw if valid
                    int tile = m_tiles.get(offset + tileY);
                    if (tile >= 0 && tile < m_textures.size()) {

                        // Bind texture
                        glBindTexture(GL_TEXTURE_2D, m_textures.get(tile));

                        // Draw tile
                        glBegin(GL_QUADS);
                        glTexCoord2f(0, 1);
                        glVertex2f(x, y);
                        glTexCoord2f(1, 1);
                        glVertex2f(x + 1, y);
                        glTexCoord2f(1, 0);
                        glVertex2f(x + 1, y + 1);
                        glTexCoord2f(0, 0);
                        glVertex2f(x, y + 1);
                        glEnd();
                    }
                    tileY = (tileY + 1) % m_yTiles;
                }
                tileX = (tileX + 1) % m_xTiles;
            }
        }
        glPopMatrix();
    }
}
