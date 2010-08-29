package flash.display;

import flash.events.EventDispatcher;
import flash.geom.ColorTransform;
import flash.geom.Matrix;
import flash.geom.Transform;
import jitb.display.DisplayList;
import jitb.display.IDisplayObject;
import jitb.errors.MissingImplementationException;
import jitb.lang.trace;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;

/**
 * @author Joa Ebert
 */
public abstract class DisplayObject extends EventDispatcher implements IBitmapDrawable, IDisplayObject {
	private final DoubleBuffer modelViewMatrix = BufferUtils.createDoubleBuffer(16);

	private double _x = 0.0;
	private double _y = 0.0;
	private double _scaleX = 1.0;
	private double _scaleY = 1.0;
	private double _rotation = 0.0;
	private String _blendMode = BlendMode.NORMAL;
	private final Transform _transform = new Transform().$JITBinit(this);

	public DisplayObject() {
		DisplayList.register(this);
	}

	public Stage stage() { return null; }

	public String blendMode() { return _blendMode; }
	public void blendMode(final String value) { _blendMode = value; }

	public double x() { return _x; }
	public void x(final double value) { _x = value; }

	public double y() { return _y; }
	public void y(final double value) { _y = value; }

	public double rotation() { return _rotation; }
	public void rotation(final double value) { _rotation = value; }

	public double scaleX() { return _scaleX; }
	public void scaleX(final double value) { _scaleX = value; }

	public double scaleY() { return _scaleY; }
	public void scaleY(final double value) { _scaleY = value; }

	public Transform transform() { return _transform; }

	protected abstract void JITB$render();

	public final void JITB$loadMatrix(final Matrix matrix) {
		//TODO FIXME this should be fixed
		_x = matrix.tx;
		_y = matrix.ty;
		_scaleX = matrix.a;
		_scaleY = matrix.d;
		_rotation = Math.asin(matrix.b) / Math.PI * 180.0;
	}

	public final void JITB$renderDisplayObject() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		//transform().matrix().JITB$toDoubleBuffer(modelViewMatrix);
		//GL11.glLoadMatrix(modelViewMatrix);

		GL11.glTranslated(x(), y(), 0.0);
		GL11.glRotatef((float)(_rotation / 180.0 * Math.PI), 0.0f, 0.0f, 1.0f);
		GL11.glScaled(_scaleX, _scaleY, 1.0);
		GL11.glColor4d(_transform.colorTransform().redMultiplier, _transform.colorTransform().greenMultiplier,
					   _transform.colorTransform().blueMultiplier, _transform.colorTransform().alphaMultiplier);
		final String blendMode = blendMode();
		boolean disableDepthTest = false;

		if(blendMode.equals(BlendMode.ADD)) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
			disableDepthTest = true;
		}

		try {
			JITB$render();
		} finally {
			if(disableDepthTest) {
				GL11.glDisable(GL11.GL_BLEND);
			}
			GL11.glPopMatrix();
		}
	}
}
