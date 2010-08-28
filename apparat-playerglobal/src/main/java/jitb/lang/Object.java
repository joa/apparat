package jitb.lang;

import jitb.errors.DynamicCodeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Joa Ebert
 */
public class Object {
	public void setPropertyIsEnumerable(final String name) {
		setPropertyIsEnumerable(name, true);
	}

	public void setPropertyIsEnumerable(final String name, final boolean isEnum) {}

	@Override
	public String toString() {
		return "[object "+getClass().getName()+"]";
	}

	public Object valueOf() {
		return this;
	}
}
