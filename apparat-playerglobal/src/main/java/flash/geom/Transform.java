package flash.geom;

import flash.display.DisplayObject;

/**
 * @author Joa Ebert
 */
public class Transform {
	private ColorTransform _colorTransform = new ColorTransform();

	public ColorTransform colorTransform() { return _colorTransform.clone(); }
	public void colorTransform(final ColorTransform value) { _colorTransform = value; }

	public Transform $JITBinit(final DisplayObject displayObject) {
		return this;
	}
}
