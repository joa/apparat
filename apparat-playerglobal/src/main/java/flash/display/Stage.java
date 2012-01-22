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

import org.lwjgl.opengl.GL11;

/**
 * @author Joa Ebert
 */
public final class Stage extends DisplayObjectContainer {
	private double _rate = 32.0;
	private String _quality = "high";
	private String _scaleMode = "none";

	public void frameRate(final double value) {
		_rate = value;
	}

	public double frameRate() {
		return _rate;
	}

	public void quality(final String value) {
		_quality = value;
	}

	public String quality() {
		return _quality;
	}

	public void scaleMode(final String value) {
		_scaleMode = value;
	}

	public String scaleMode() {
		return _scaleMode;
	}

	@Override
	protected final void JITB$render() {
		//
		// Clear the screen when the stage is re-rendered.
		//

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		//
		// Translate corresponding to stage align.
		//

		GL11.glTranslatef(0.0f, 0.0f, 0.0f);

		//
		// Now render all children of the stage.
		//

		JITB$renderChildren();
	}
}
