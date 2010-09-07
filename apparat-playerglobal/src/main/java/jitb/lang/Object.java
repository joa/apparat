package jitb.lang;

import jitb.errors.DynamicCodeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * @author Joa Ebert
 */
public class Object extends java.lang.Object {
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

	public java.lang.Object JITB$getIndex(final int index) { throw new DynamicCodeException(); }

	public void JITB$setIndex(final int index, final java.lang.Object value) { throw new DynamicCodeException(); }

	public java.lang.Object JITB$getProperty(final String property) { throw new DynamicCodeException(); }

	public void JITB$setProperty(final String property, final java.lang.Object value) { throw new DynamicCodeException(); }
}
