package flash.geom;

/**
 * @author Joa Ebert
 */
public class Point {
	public static double distance(final Point pt1, final Point pt2) {
		final double dx = pt1.x - pt2.x;
		final double dy = pt1.y - pt2.y;

		return Math.sqrt(dx * dx + dy * dy);
	}

	public double x;
	public double y;

	public Point() { this(0.0, 0.0); }
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
}
