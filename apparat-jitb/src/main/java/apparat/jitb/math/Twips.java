package apparat.jitb.math;

/**
 * @author Joa Ebert
 */
public final class Twips {
	private Twips() {

	}
	
	public static int toPixel(final int twips) {
		return twips / 20;
	}
}
