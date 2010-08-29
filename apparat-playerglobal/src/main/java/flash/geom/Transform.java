package flash.geom;

import flash.display.DisplayObject;
import jitb.errors.Require;

/**
 * @author Joa Ebert
 */
public class Transform extends jitb.lang.Object {
	private ColorTransform _colorTransform = new ColorTransform();
	private Matrix _matrix = new Matrix();
	private DisplayObject _displayObject = null;

	public ColorTransform colorTransform() {
		return new ColorTransform(_colorTransform.redMultiplier, _colorTransform.greenMultiplier,
								  _colorTransform.blueMultiplier, _colorTransform.alphaMultiplier,
								  _colorTransform.redOffset, _colorTransform.greenOffset,
								  _colorTransform.blueOffset, _colorTransform.alphaOffset);
	}
	
	public void colorTransform(final ColorTransform value) {
		Require.nonNull("colorTransform", value);
		_colorTransform = value;
	}

	public Matrix matrix() {
		return _matrix;
	}

	public void matrix(final Matrix value) {
		Require.nonNull("matrix", value);
		_matrix = value;
		
		if(null != _displayObject) {
			_displayObject.JITB$loadMatrix(_matrix);
		}
	}

	public Matrix JITB$matrix() {
		return _matrix;
	}
	
	public Transform JITB$init(final DisplayObject displayObject) {
		_displayObject = displayObject;
		return this;
	}
}
