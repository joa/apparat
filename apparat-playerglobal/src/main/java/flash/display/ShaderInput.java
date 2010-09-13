package flash.display;

import flash.utils.ByteArray;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL13;

import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public final class ShaderInput extends jitb.lang.Object {
	public static ShaderInput JITB$create(final String name, final int channels, final int index) {
		return new ShaderInput(name, channels, index);
	}
	
	private enum InputType { NONE, BITMAPDATA, BYTEARRAY, VECTOR }
	private final HashMap<String, Object> _dynamic = new HashMap<String, Object>();

	private String _name;
	private int _channels;
	private int _height;
	private int _index;
	private jitb.lang.Object _input;
	private int _width;
	private InputType _inputType = InputType.NONE;

	private float _widthR = .0f;
	private float _heightR = .0f;

	public ShaderInput() {}

	private ShaderInput(final String name, final int channels, final int index) {
		_name = name;
		_channels = channels;
		_index = index;
	}

	String name() {
		return _name;
	}

	public int channels() { return _channels; }

	public int height() { return _height; }
	public void height(final int value) {
		_height = value;

		if(_height != 0.0f) {
			_heightR = 1.0f / _height;
		} else {
			_heightR = 0.0f;
		}
	}

	public int index() { return _index; }

	public void input(final jitb.lang.Object value) {
		_input = value;

		if(_input == null) {
			_inputType = InputType.NONE;
		} else {
			if(_input instanceof BitmapData) {
				final BitmapData bitmapData = (BitmapData)_input;
				width(bitmapData.width());
				height(bitmapData.height());
				_inputType = InputType.BITMAPDATA;
			} else if(_input instanceof ByteArray) {
				final ByteArray byteArray = (ByteArray)_input;
				if((byteArray.length() % channels()) != 0) {
					throw new Error("ByteArray length is not a divisible by "+channels()+".");
				}
				_inputType = InputType.BYTEARRAY;
			/*} else if(_input instanceof Vector) {
				_inputType = InputType.VECTOR;
			*/
			} else {
				throw new Error("Illegal input type.");//todo replace with correct argument error.
			}
		}
	}
	public jitb.lang.Object input() { return _input; }

	public int width() { return _width; }
	public void width(final int value) {
		_width = value;

		if(_width != 0.0f) {
			_widthR = 1.0f / _width;
		} else {
			_widthR = 0.0f;
		}
	}

	@Override
	public Object JITB$getProperty(final String property) {
		if(property.equals("channels")) {
			return channels();
		} else if(property.equals("height")) {
			return height();
		} else if(property.equals("index")) {
			return index();
		} else if(property.equals("input")) {
			return input();
		} else if(property.equals("width")) {
			return width();
		} else {
			return _dynamic.get(property);
		}
	}

	@Override
	public void JITB$setProperty(final String property, final Object value) {
		if(property.equals("channels")) {
			throw new IllegalAccessError();//this is read-only
		} else if(property.equals("height")) {
			height((Integer)value);
		} else if(property.equals("index")) {
			throw new IllegalAccessError();//this is read-only
		} else if(property.equals("input")) {
			input((jitb.lang.Object)value);
		} else if(property.equals("width")) {
			height((Integer)value);
		} else {
			_dynamic.put(property, value);
		}
	}

	public void JITB$applyInput(final int programId) {
		if(null != input() && InputType.NONE.equals(_inputType)) {
			return;
		}
		
		final int sizeLocation = ARBShaderObjects.glGetUniformLocationARB(programId, "texs"+index());
		ARBShaderObjects.glUniform2fARB(sizeLocation, _widthR, _heightR);

		final int textureLocation = ARBShaderObjects.glGetUniformLocationARB(programId, "tex"+index());
		ARBShaderObjects.glUniform1iARB(textureLocation, index());

		switch(_inputType) {
			case BITMAPDATA:
				GL13.glActiveTexture(GL13.GL_TEXTURE0 + index());
				glBindTexture(GL_TEXTURE_2D, ((BitmapData)input()).JITB$textureId());
				break;
			default:
				//
				// System.out.println("Warning: Unhandled input type "+_inputType+".");
				//
		}
	}

	public void JITB$unapplyInput() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + index());
		glBindTexture(GL_TEXTURE_2D, 0);
	}
}
