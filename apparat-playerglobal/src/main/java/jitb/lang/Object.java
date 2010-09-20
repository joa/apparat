package jitb.lang;

import jitb.errors.DynamicCodeException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * @author Joa Ebert
 */
public class Object {
	public void setPropertyIsEnumerable(final java.lang.String name) {
		setPropertyIsEnumerable(name, true);
	}

	public void setPropertyIsEnumerable(final java.lang.String name, final boolean isEnum) {}

	@Override
	public java.lang.String toString() {
		return "[object "+getClass().getName()+"]";
	}

	public Object valueOf() {
		return this;
	}

	public java.lang.Object JITB$getIndex(final int index) { throw new DynamicCodeException(); }

	public void JITB$setIndex(final int index, final java.lang.Object value) { throw new DynamicCodeException(); }

	public java.lang.Object JITB$getProperty(final java.lang.String property) { throw new DynamicCodeException(); }

	public void JITB$setProperty(final java.lang.String property, final java.lang.Object value) { throw new DynamicCodeException(); }
}
