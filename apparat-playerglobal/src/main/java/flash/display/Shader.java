package flash.display;

import apparat.pbj.Pbj;
import flash.utils.ByteArray;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GLContext;

import java.nio.ByteBuffer;

/**
 * @author Joa Ebert
 */
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
	}

	public ShaderData data() {
		if(null == _data) {
			_data = new ShaderData(pbj());
		}

		return _data;
	}
	
	public void data(final ShaderData value) { _data = value; }

	public void JITB$bind() {
		if(GLContext.getCapabilities().GL_ARB_shader_objects) {
			if(null != data()) {
				data().JITB$applyParameters(programId());
			}
			ARBShaderObjects.glUseProgramObjectARB(programId());
		} else {
			throw new RuntimeException("Sorry, no shaders supported on your system.");
		}
	}

	public void JITB$unbind() {
		ARBShaderObjects.glUseProgramObjectARB(0);
	}
	
	private Pbj pbj() {
		if(null == _pbj && null != _byteCode) {
			//ignore IDE error, code compiles and is correct
			_pbj = Pbj.fromByteArray(_byteCode.JITB$toByteArray());
		}

		return _pbj;
	}

	private int programId() {
		if(-1 == _programId) {
			compileShader();
		}

		return _programId;
	}

	private void compileShader() {
		System.out.println("Compiling shader ...");
		final String vertexShader = pbj().toVertexShader();
		final String fragmentShader = pbj().toFragmentShader();
		final ByteBuffer vertexBuffer = BufferUtils.createByteBuffer(vertexShader.length()).put(vertexShader.getBytes());
		final ByteBuffer fragmentBuffer = BufferUtils.createByteBuffer(fragmentShader.length()).put(fragmentShader.getBytes());

		System.out.println("Vertex shader:");
		System.out.println(vertexShader);
		System.out.println("Fragment shader:");
		System.out.println(fragmentShader);
		
		vertexBuffer.flip();
		fragmentBuffer.flip();

		_vertexShaderId = ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
		ARBShaderObjects.glShaderSourceARB(_vertexShaderId, vertexBuffer);
		ARBShaderObjects.glCompileShaderARB(_vertexShaderId);

		_fragmentShaderId = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
		ARBShaderObjects.glShaderSourceARB(_fragmentShaderId, fragmentBuffer);
		ARBShaderObjects.glCompileShaderARB(_fragmentShaderId);

		_programId = ARBShaderObjects.glCreateProgramObjectARB();
		ARBShaderObjects.glAttachObjectARB(_programId, _vertexShaderId);
		ARBShaderObjects.glAttachObjectARB(_programId, _fragmentShaderId);
		ARBShaderObjects.glLinkProgramARB(_programId);
	}
}
