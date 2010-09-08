package flash.display;

import jitb.lang.Array;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

import java.util.HashMap;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public final class ShaderParameter extends jitb.lang.Object {
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

	ShaderParameter(
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
			//todo
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
		if(property.equals("description")) {
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
		if(property.equals("description")) {
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
}
