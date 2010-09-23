package jitb.display;

import flash.display.Shader;
import flash.display.ShaderData;
import flash.display.ShaderInput;
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
				new ShaderInput[0]
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
