package flash.display;

import jitb.lang.Array;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;
import org.lwjgl.opengl.ARBShaderObjects;

import java.util.HashMap;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public final class ShaderParameter extends jitb.lang.Object {
	public static ShaderParameter JITB$create(final String name,
			final String description,
			final Array minValue,
			final Array maxValue,
			final Array defaultValue,
			final String type, final int index) {
		return new ShaderParameter(name, description, minValue, maxValue, defaultValue, type, index);
	}

	private final HashMap<String, Object> _dynamic = new HashMap<String, Object>();

	private Array _value;

	private String _type;
	private int _index;

	private String _name;
	private String _description;
	private Array _minValue;
	private Array _maxValue;
	private Array _defaultValue;

	public ShaderParameter() {}

	private ShaderParameter(
			final String name,
			final String description,
			final Array minValue,
			final Array maxValue,
			final Array defaultValue,
			final String type, final int index) {
		_name = name;
		_description = description;
		_minValue = minValue;
		_maxValue = maxValue;
		_defaultValue = defaultValue;
		_type = type;
		_index = index;

		if(null == defaultValue) {
			//todo create new...
		} else {
			_value = defaultValue;
		}
	}

	public Array value() { return _value; }
	public void value(final Array value) {
		_value = value;
		//todo clamp
	}

	public String type() { return _type; }
	public int index() { return _index; }
	public String name() { return _name; }
	public String description() { return _description; }
	public Array minValue() { return _minValue; }
	public Array maxValue() { return _maxValue; }
	public Array defaultValue() { return _defaultValue; }

	@Override
	public Object JITB$getProperty(final String property) {
		if(property.equals("value")) {
			return _value;
		} else if(property.equals("type")) {
			return _type;
		} else if(property.equals("index")) {
			return _index;
		} else if(property.equals("name")) {
			return _name;
		} else if(property.equals("description")) {
			return _description;
		} else if(property.equals("minValue")) {
			return _minValue;
		} else if(property.equals("maxValue")) {
			return _maxValue;
		} else if(property.equals("defaultValue")) {
			return _defaultValue;
		} else {
			return _dynamic.get(property);
		}
	}

	@Override
	public void JITB$setProperty(final String property, final Object value) {
		if(property.equals("value")) {
			value((Array)value);
		} else if(property.equals("type")) {
			throw new IllegalAccessError(); 
		} else if(property.equals("index")) {
			throw new IllegalAccessError();//this is read-only
		} else if(property.equals("name")) {
			throw new IllegalAccessError();//this is read-only
		} else if(property.equals("description")) {
			_description = (String)value;
		} else if(property.equals("minValue")) {
			_minValue = (Array)value;
		} else if(property.equals("maxValue")) {
			_maxValue = (Array)value;
		} else if(property.equals("defaultValue")) {
			_defaultValue = (Array)value;
		} else {
			_dynamic.put(property, value);
		}
	}

	public void JITB$applyParameter(final int programId) {
		final int location = ARBShaderObjects.glGetUniformLocationARB(programId, name());

		if(_type.equals(ShaderParameterType.FLOAT)) {
			ARBShaderObjects.glUniform1fARB(location, getFloat(0));
		} else if(_type.equals(ShaderParameterType.FLOAT2)) {
			ARBShaderObjects.glUniform2fARB(location, getFloat(0), getFloat(1));
		} else if(_type.equals(ShaderParameterType.FLOAT3)) {
			ARBShaderObjects.glUniform3fARB(location, getFloat(0), getFloat(1), getFloat(2));
		} else if(_type.equals(ShaderParameterType.FLOAT4)) {
			ARBShaderObjects.glUniform4fARB(location, getFloat(0), getFloat(1), getFloat(2), getFloat(3));
		}
	}

	private float getFloat(final int index) {
		if(null == _value || _value.length() <= index) {
			return 0.0f;
		}

		final Object value = _value.JITB$getIndex(index);
		float floatValue = Float.NaN;

		if(value instanceof Integer) {
			floatValue = ((Integer)value).floatValue();
		} else if(value instanceof Double) {
			floatValue = ((Double)value).floatValue();
		} else if(value instanceof Long) {
			floatValue = ((Long)value).floatValue();
		} else if(value instanceof Float) {
			floatValue = (Float)value;
		} else {
			//use default value?
		}

		return floatValue;
	}
}
