package flash.geom;

/**
 * @author Joa Ebert
 */
public class Point extends jitb.lang.Object {
	public static double distance(final Point pt1, final Point pt2) {
		final double dx = pt1.x - pt2.x;
		final double dy = pt1.y - pt2.y;

		return Math.sqrt(dx * dx + dy * dy);
	}

	public static Point interpolate(final Point pt1, final Point pt2, final double f) {
		final double d = pt1.x - pt2.x;
		final double x = pt2.x + f * d;
		final double y = pt2.y + (x - pt2.x) * ((pt1.y - pt2.y) / d);
		
		return new Point(x, y);
	}

	public static Point polar(final double len, final double ang) {
		return new Point(len * Math.sin(ang), len * Math.cos(ang));
	}

	public double x;
	public double y;

	public Point() { this(0.0); }
	public Point(final double x) { this(x, 0.0); }
	public Point(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	public Point add(final Point value) {
		return new Point(x + value.x, y + value.y);
	}

	public Point clone() {
		return new Point(x, y);
	}

	public boolean equals(final Point toCompare) {
		return x == toCompare.x && y == toCompare.y;
	}

	public void normalize(final double thickness) {
		final double length = length();

		if(0.0 != length) {
			x = x / length * thickness;
			y = y / length * thickness;
		}
	}

	public void offset(final double dx, final double dy) {
		x += dx;
		y += dy;
	}

	public Point subtract(final Point value) {
		return new Point(x - value.x, y - value.y);
	}

	@Override
	public String toString() {
		return "(x="+x+", y="+y+")";
	}
}
