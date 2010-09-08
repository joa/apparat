package jitb.lang.annotations;

/**
 * The Metadata annotation is a container for multiple Element annotations.
 *
 * @see Element
 * @author Joa Ebert
 */
public @interface Metadata {
	Element[] value();
}
