package flash.display;

import flash.geom.Matrix;
import jitb.lang.Array;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Joa Ebert
 */
public final class Graphics extends jitb.lang.Object {
	private Shader _shader;

	public void beginBitmapFill(final BitmapData bitmap, final Matrix matrix, final boolean repeat, final boolean smooth) {

	}

	public void beginFill(final long color, final double alpha) {

	}

	public void beginGradientFill(final String type, final Array colors, final Array alphas, final Array ratios, final Matrix matrix,
								  final String spreadMethod, final String interpolationMethod, final double focalPointRatio) {}

	public void beginShaderFill(final Shader shader) {
		beginShaderFill(shader, null);
	}

	public void beginShaderFill(final Shader shader, final Matrix matrix) {
		_shader = shader;
		_shader.JITB$bind();
	}

	public void clear() {
		
	}

	public void copyFrom(final Graphics sourceGraphics) {
		
	}

	public void drawCircle(final double x, final double y, final double radius) {

	}

	public void drawEllipse(final double x, final double y, final double width, final double height) {

	}

	//public void drawGraphicsData(Vector.<IGraphicsData>)
	//public void drawPath

	public void drawRect(final double x, final double y, final double width, final double height) {
		glBegin(GL_QUADS); {
			glTexCoord2f(0.0f, 0.0f);
			glVertex2d(x, y);

			glTexCoord2f(1.0f, 0.0f);
			glVertex2d(x + width, y);

			glTexCoord2f(1.0f, 1.0f);
			glVertex2d(x + width, y + height);

			glTexCoord2f(0.0f, 1.0f);
			glVertex2d(x, y + height);
		} glEnd();
	}

	public void drawRoundRect(final double x, final double y, final double width, final double height,
							  final double ellipseWidth, final double ellipseHeight) {

	}

	//drawTriangles

	public void endFill() {
		if(null != _shader) {
			_shader.JITB$unbind();
			_shader = null;
		}
	}

	
}
