/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
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
