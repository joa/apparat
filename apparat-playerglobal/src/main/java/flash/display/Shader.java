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

import apparat.pbj.Pbj;
import flash.utils.ByteArray;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;
import jitb.util.ShaderUtil;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public class Shader extends jitb.lang.Object {
	private ByteArray _byteCode;
	private Pbj _pbj;

	private ShaderData _data;

	private int _vertexShaderId = -1;
	private int _fragmentShaderId = -1;
	private int _programId = -1;

	public Shader() {}

	public Shader(final ByteArray byteCode) {
		byteCode(byteCode);
	}

	public ByteArray byteCode() { return _byteCode; }
	public void byteCode(final ByteArray value) {
		_byteCode = value;

		if(null != _pbj) {
			_pbj = null;
			_data = null;
		}

		if(-1 != _programId) {
			//cleanup ...
		}

		pbj();
	}

	public ShaderData data() { return _data; }

	public void data(final ShaderData value) { _data = value; }

	public final void JITB$bind(double x, double y, double width, double height) {
		JITB$bind(x, y, width, height, true);
	}

	public void JITB$bind(double x, double y, double width, double height, boolean flipY) {
		if(ShaderUtil.shaderSupport()) {
			ARBShaderObjects.glUseProgramObjectARB(programId());

			final int location = ARBShaderObjects.glGetUniformLocationARB(programId(), "PB_OFFSET");

			if(flipY) {
				ARBShaderObjects.glUniform4fARB(location, (float)x, (float)y, (float)height, -1.0f);
			} else {
				ARBShaderObjects.glUniform4fARB(location, (float)x, (float)y, 0.0f, 1.0f);
			}

			if(null != data()) {
				data().JITB$applyParameters(programId());
			}
		} else {
			throw new RuntimeException("Sorry, no shaders supported on your system.");
		}
	}

	public void JITB$unbind() {
		ARBShaderObjects.glUseProgramObjectARB(0);
		if(null != data()) {
			data().JITB$unapplyParameters();
		}
	}

	private Pbj pbj() {
		if(null == _pbj && null != _byteCode) {
			_pbj = ShaderUtil.getPbj(_byteCode);
			_data = new ShaderData(_pbj);
		}

		return _pbj;
	}

	protected int programId() {
		if(-1 == _programId) {
			compileShader();
		}

		return _programId;
	}

	protected String vertexShader() { return pbj().toVertexShader(); }

	protected String fragmentShader() { return pbj().toFragmentShader(); }

	protected void compileShader() {
		System.out.println("Compiling shader ...");
		final String vertexShader = vertexShader();
		final String fragmentShader = fragmentShader();

		System.out.println("Vertex shader:");
		System.out.println(vertexShader);
		System.out.println("Fragment shader:");
		System.out.println(fragmentShader);

		_vertexShaderId = ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
		ARBShaderObjects.glShaderSourceARB(_vertexShaderId, vertexShader);
		ARBShaderObjects.glCompileShaderARB(_vertexShaderId);
		System.out.println("VertexShader Info: "+ARBShaderObjects.glGetInfoLogARB(_vertexShaderId, 8192));

		_fragmentShaderId = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
		ARBShaderObjects.glShaderSourceARB(_fragmentShaderId, fragmentShader);
		ARBShaderObjects.glCompileShaderARB(_fragmentShaderId);
		System.out.println("FragmentShader Info: "+ARBShaderObjects.glGetInfoLogARB(_fragmentShaderId, 8192));

		_programId = ARBShaderObjects.glCreateProgramObjectARB();
		ARBShaderObjects.glAttachObjectARB(_programId, _vertexShaderId);
		ARBShaderObjects.glAttachObjectARB(_programId, _fragmentShaderId);
		ARBShaderObjects.glLinkProgramARB(_programId);
		System.out.println("Program Info: "+ARBShaderObjects.glGetInfoLogARB(_programId, 8192));
	}
}
