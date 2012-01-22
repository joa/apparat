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
package flash.filters;

import flash.display.Shader;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public class ShaderFilter extends BitmapFilter {
	private Shader _shader;

	private int _topExtension;
	private int _rightExtension;
	private int _bottomExtension;
	private int _leftExtension;

	public ShaderFilter() {
		this(null);
	}

	public ShaderFilter(final Shader shader) {
		_shader = shader;
	}

	public Shader shader() { return _shader; }
	public void shader(final Shader value) { _shader = value; }

	public int topExtension() { return _topExtension; }
	public void topExtension(final int value) { _topExtension = value; }

	public int rightExtension() { return _rightExtension; }
	public void rightExtension(final int value) { _rightExtension = value; }

	public int bottomExtension() { return _bottomExtension; }
	public void bottomExtension(final int value) { _bottomExtension = value; }

	public int leftExtension() { return _leftExtension; }
	public void leftExtension(final int value) { _leftExtension = value; }
}
