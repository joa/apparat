package jitb.lang.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Element annotation represents an ActionScript Element annotaiton.
 *
 * <p>Since all annotations in ActionScript 3 are completely untyped the Element
 * annotation mimics this behaviour.</p>
 *
 * <p>An ActionScript 3 annotation is usually defined like the following:
 * 	<code>[Name(key0=value0, key1=value1, ..., keyN=valueN)]</code></p>
 *
 * <p>This is behaviour is replicated with the Element annotation by specifying
 *  <code>@Element(name="Name", keys={"key0", "key1", ..., "keyN"}, values={"value0", "value1", ..., "valueN"})</code>
 * It is also noteworthy that this is the metadata format in ABC files.
 * </p>
 *
 * @see Metadata
 * @author Joa Ebert
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Element {
	String name();
	String[] keys() default {};
	String[] values() default {};
}
