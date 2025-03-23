package com.basic4gl.library.desktopgl;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.opengl.GL11.*;

import com.basic4gl.library.standard.TrigBasicLib;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Vector;
import org.lwjgl.BufferUtils;

/**
 * A 2D grid of texture tiles for use in 2D sprite drawing.
 * Can be manipulated as a glBasicSprite.
 *
 * Textures are loaded with SetTexture(s) or AddTexture(s). The first texture
 * loaded is index 0. The tile map is specified as a 2D grid of integers, each
 * corresponding to an index in the textures array.
 */
public class GLTileMap extends GLBasicSprite {

	private int xTiles, yTiles;
	private Vector<Integer> tiles;

	public boolean repeatX, repeatY;

	// Construction/destruction
	public GLTileMap() {
		super();
		setDefaults();
	}

	public GLTileMap(int tex) {
		super(tex);
		setDefaults();
	}

	public GLTileMap(Vector<Integer> tex) {
		super(tex);
		setDefaults();
	}

	private void setDefaults() {
		xTiles = 0;
		yTiles = 0;
		centerX = 0;
		centerY = 0;
		repeatX = true;
		repeatY = true;
		solid = true;
	}

	protected void internalCopy(GLBasicSprite s) {
		super.internalCopy(s);
		GLTileMap t = (GLTileMap) s;
		setTiles(t.xTiles, t.yTiles, t.tiles);
	}

	// Class type identification
	public GLSpriteEngine.GLSpriteType getGLSpriteType() {
		return GLSpriteEngine.GLSpriteType.SPR_TILEMAP;
	}

	// Getters/Setters
	public int getTilesX() {
		return xTiles;
	}

	public int getTilesY() {
		return yTiles;
	}

	public Vector<Integer> getTiles() {
		return tiles;
	}

	public void setTiles(int xTiles, int yTiles, Vector<Integer> tiles) {
		assertTrue(xTiles >= 0);
		assertTrue(yTiles >= 0);
		assertTrue(tiles.size() >= xTiles * yTiles);
		this.xTiles = xTiles;
		this.yTiles = yTiles;
		if (xTiles > 0 || yTiles > 0) {
			this.tiles = tiles;
		} else {
			this.tiles.clear();
		}
	}

	// Rendering
	public void render(float[] camInv) {

		// Render tile map using OpenGL commands

		// Assumes that the appropriate projection/translation matrices have been setup,
		// and other OpenGL state (such as texturing & transparency) has been setup
		// accordingly.

		// Sprite must be visible
		if (!visible || xTiles == 0 || yTiles == 0) {
			return;
		}

		ByteBuffer byteBuf = BufferUtils.createByteBuffer(color.length * 4); // 4 bytes per float
		FloatBuffer buffer = byteBuf.asFloatBuffer();
		buffer.put(color);
		buffer.position(0);
		glColor4fv(buffer);
		buffer.rewind();
		buffer.get(color);

		// Setup translation, rotation and scaling.
		// Note:    We will setup 2 matrices.
		//          First the OpenGL matrix to perform the operations to drawn elements.
		//          Second is an INVERSE of all the operations. This will be used to
		//          determine where the screen corners map to the tile map, so we
		//          can calculate what range of tiles needs to be drawn.
		float[] m1 = new float[16], m2 = new float[16];
		glPushMatrix();

		// Translate to object position
		glTranslatef(positionX, positionY, 0);
		TrigBasicLib.translate(-positionX, -positionY, 0);
		TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), camInv, m1);

		// Rotate by angle
		if (angle != 0) {
			glRotatef(angle, 0, 0, 1);
			TrigBasicLib.rotateZ(-angle);
			TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m1, m2);
		} else {
			TrigBasicLib.copyMatrix(m2, m1);
		}

		// Scale to tile size
		glScalef(sizeX * scale, sizeY * scale, 1);
		TrigBasicLib.scale(1.0f / (sizeX * scale), 1.0f / (sizeY * scale), 1);
		TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m2, m1);

		// Centre offset
		if (centerX != 0 || centerY != 0) {
			glTranslatef(-centerX, -centerY, 0);
			TrigBasicLib.translate(centerX, centerY, 0);
			TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m1, m2);
		} else {
			TrigBasicLib.copyMatrix(m2, m1);
		}

		// Note: Flip not implemented yet!

		// Now we can use our m1 matrix to translate from camera space into tile space
		// Find range of tiles spanned.
		float[][] camCorner = new float[][] {{0, 0, 0, 1}, {1, 0, 0, 1}, {0, 1, 0, 1}, {1, 1, 0, 1}};
		//    vmReal camCorner[4][4] = {{.1,.1, 0, 1}, {.9,.1, 0, 1}, {.1,.9, 0, 1}, {.9,.9, 0, 1} };
		//    // DEBUGGING!!!
		int maxX = -1000000, minX = 1000000, maxY = -1000000, minY = 1000000;
		for (int i = 0; i < 4; i++) {
			float[] tileSpaceCorner = new float[4];
			TrigBasicLib.matrixTimesVec(m2, camCorner[i], tileSpaceCorner);
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
		if (!repeatX) {

			// Clamp x
			if (minX < 0) {
				minX = 0;
			}
			if (maxX >= xTiles) {
				maxX = xTiles - 1;
			}
		}
		if (!repeatY) {

			// Clamp y
			if (minY < 0) {
				minY = 0;
			}
			if (maxY >= yTiles) {
				maxY = yTiles - 1;
			}
		}
		int startTileX = minX % xTiles, startTileY = minY % yTiles;
		if (startTileX < 0) {
			startTileX += xTiles;
		}
		if (startTileY < 0) {
			startTileY += yTiles;
		}

		// Draw tile map
		if (minX <= maxX && minY <= maxY) {
			int tileX = startTileX;
			for (int x = minX; x <= maxX; x++) {
				int offset = tileX * yTiles;
				int tileY = startTileY;
				for (int y = minY; y <= maxY; y++) {

					assertTrue(tileY >= 0);
					assertTrue(tileX >= 0);
					assertTrue(tileY < yTiles);
					assertTrue(tileX < xTiles);

					// Find tile index. Only draw if valid
					int tile = tiles.get(offset + tileY);
					if (tile >= 0 && tile < textures.size()) {

						// Bind texture
						glBindTexture(GL_TEXTURE_2D, textures.get(tile));

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
					tileY = (tileY + 1) % yTiles;
				}
				tileX = (tileX + 1) % xTiles;
			}
		}
		glPopMatrix();
	}
}
