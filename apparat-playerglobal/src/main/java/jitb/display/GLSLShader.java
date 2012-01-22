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
package jitb.display;

import flash.display.Shader;
import flash.display.ShaderData;
import flash.utils.ByteArray;
import jitb.util.ShaderUtil;
import org.lwjgl.opengl.ARBShaderObjects;

/**
 * @author Joa Ebert
 */
public final class GLSLShader extends Shader {
	private final String _vertexShader;
	private final String _fragmentShader;

	public GLSLShader(final String fragmentShader) {
		this(fragmentShader, "void main(){gl_Position=ftransform();}");
	}

	public GLSLShader(final String fragmentShader, final String vertexShader) {
		_fragmentShader = fragmentShader;
		_vertexShader = vertexShader;

		data(
			ShaderData.JITB$fromParameters(
				ShaderUtil.getGLSLParameters(_fragmentShader),
				ShaderUtil.getGLSLTextures(_fragmentShader)
			)
		);
	}

	@Override
	public ByteArray byteCode() { return null; }

	@Override
	public void byteCode(ByteArray value) {}

	@Override
	protected String vertexShader() { return _vertexShader; }

	@Override
	protected String fragmentShader() { return _fragmentShader; }

	@Override
	public void JITB$bind(double x, double y, double width, double height, boolean flipY) {
		if(ShaderUtil.shaderSupport()) {
			ARBShaderObjects.glUseProgramObjectARB(programId());

			if(null != data()) {
				data().JITB$applyParameters(programId());
			}
		} else {
			throw new RuntimeException("Sorry, no shaders supported on your system.");
		}
	}

	public void dispose() {
		//todo only if it has been used before!
		ARBShaderObjects.glDeleteObjectARB(programId());
	}
}
