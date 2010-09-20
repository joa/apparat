package flash.display;

import flash.geom.Matrix;
import jitb.lang.Array;
import org.lwjgl.opengl.ARBShaderObjects;

import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Joa Ebert
 */
public final class Graphics extends jitb.lang.Object {
	private interface ICommand {
		void run();
	}

	private interface IFill extends ICommand {
		void init(double x, double y, double width, double height);
	}

	private final class DrawRectCommand implements ICommand {
		private final double _x, _y, _width, _height;

		public DrawRectCommand(final double x, final double y, final double width, final double height) {
			_x = x;
			_y = y;
			_width = width;
			_height = height;
		}

		@Override
		public void run() {
			initFill(_x, _y, _width, _height);

			glPolygonMode(GL_FRONT, GL_FILL);
			glBegin(GL_QUADS); {
				glTexCoord2d(0.0, 0.0);
				glVertex2d(_x, _y);

				glTexCoord2d(_width, 0.0);
				glVertex2d(_x + _width, _y);

				glTexCoord2d(_width, _height);
				glVertex2d(_x + _width, _y + _height);

				glTexCoord2d(0.0, _height);
				glVertex2d(_x, _y + _height);
			} glEnd();
		}
	}

	private final class BeginShaderFillCommand implements IFill {
		private final Shader _shader;
		private final Matrix _matrix;

		public BeginShaderFillCommand(final Shader shader, final Matrix matrix) {
			_shader = shader;
			_matrix = matrix;
		}

		@Override
		public void run() {
			lastFill(this);
		}

		@Override
		public void init(final double x, final double y,
									 final double width, final double height) {
			_shader.JITB$bind(x, y, width, height);
		}
	}

	private final class EndFillCommand implements ICommand {
		public EndFillCommand() {}
		
		@Override
		public void run() {
			lastFill(null);
			ARBShaderObjects.glUseProgramObjectARB(0);
		}
	}

	private LinkedList<ICommand> _commands = new LinkedList<ICommand>();
	private IFill _lastFill = null;
	
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
		_commands.addLast(new BeginShaderFillCommand(shader, matrix));
	}

	public void clear() {
		_commands.clear();
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
		_commands.addLast(new DrawRectCommand(x, y, width, height));
	}

	public void drawRoundRect(final double x, final double y, final double width, final double height,
							  final double ellipseWidth, final double ellipseHeight) {

	}

	//drawTriangles

	public void endFill() {
		_commands.addLast(new EndFillCommand());
	}

	public void JITB$render() {
		for(final ICommand command : _commands) {
			command.run();
		}
	}

	private void lastFill(final IFill fill) {
		_lastFill = fill;
	}

	private void initFill(double x, double y, double width, double height) {
		if(null != _lastFill) {
			_lastFill.init(x, y, width, height);
		}
	}
}
