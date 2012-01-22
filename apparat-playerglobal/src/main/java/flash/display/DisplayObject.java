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
package flash.display;

import flash.events.EventDispatcher;
import flash.geom.Matrix;
import flash.geom.Transform;
import jitb.display.DisplaySystem;
import jitb.display.IDisplayObject;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
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
	private boolean _visible = true;
	private float _rotationRad = 0.0f;
	private String _blendMode = BlendMode.NORMAL;
	private final Transform _transform = new Transform().JITB$init(this);

	public DisplayObject() {
		DisplaySystem.register(this);
	}

	public Stage stage() { return null; }

	public String blendMode() { return _blendMode; }
	public void blendMode(final String value) { _blendMode = value; }

	public double x() { return _x; }
	public void x(final double value) {
		_x = value;
		transform().JITB$matrix().tx = value;
	}

	public double y() { return _y; }
	public void y(final double value) {
		_y = value;
		transform().JITB$matrix().ty = value;
	}

	public double rotation() { return _rotation; }
	public void rotation(final double value) {
		_rotation = value;
		_rotationRad = (float)(value / 180.0 * Math.PI);

		updateMatrix();
	}

	public double scaleX() { return _scaleX; }
	public void scaleX(final double value) {
		_scaleX = value;
		updateMatrix();
	}

	public double scaleY() { return _scaleY; }
	public void scaleY(final double value) {
		_scaleY = value;
		updateMatrix();
	}

	public boolean visible() { return _visible; }
	public void visible(final boolean value) { _visible = false; }

	private void updateMatrix() {
		final Matrix matrix = transform().JITB$matrix();
		//TODO integrate rotation
		matrix.a = _scaleX;
		matrix.b = 0.0;
		matrix.c = 0.0;
		matrix.d = _scaleY;
		matrix.tx = _x;
		matrix.ty = _y;
	}

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
		if(!visible()) {
			return;
		}

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		transform().matrix().JITB$toDoubleBuffer(modelViewMatrix);
		GL11.glLoadMatrix(modelViewMatrix);

		//GL11.glTranslated(x(), y(), 0.0);
		//GL11.glRotatef(_rotationRad, 0.0f, 0.0f, 1.0f);
		//GL11.glScaled(_scaleX, _scaleY, 1.0);
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

	public double mouseX() { return Mouse.getX(); }
	public double mouseY() { return Mouse.getX(); }
}
