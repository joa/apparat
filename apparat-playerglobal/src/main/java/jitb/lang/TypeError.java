package jitb.lang;

import jitb.errors.ErrorUtil;

/**
 * @author Joa Ebert
 */
public class TypeError extends Error {
	public TypeError(final String message) {
		super(message);
		name = "TypeError";
	}
}
