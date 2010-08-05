package flash.geom;

/**
 * @author Joa Ebert
 */
public class Point implements Cloneable {
	public static double distance(final Point pt1, final Point pt2) {
		final double dx = pt1.x() - pt2.x();
		final double dy = pt1.y() - pt2.y();

		return Math.sqrt(dx * dx + dy * dy);
	}

	private double _x;
	private double _y;

	public Point() { this(0.0, 0.0); }
	public Point(final double x) { this(x, 0.0); }
	public Point(final double x, final double y) {
		_x = x;
		_y = y;
	}

	public double x() { return _x; }
	public void x(double value) { _x = value; }

	public double y() { return _y; }
	public void y(double value) { _y = value; }

	public double length() {
		final double x = x();
		final double y = y();

		return Math.sqrt(x * x + y * y);
	}

	public Point add(final Point value) {
		return new Point(x() + value.x(), y() + value.y());
	}

	@Override
	public Point clone() throws CloneNotSupportedException {
		return (Point)super.clone();
	}


	@Override
	public boolean equals(final Object that) {
		if(this == that) {
			return true;
		}

		if(that == null || getClass() != that.getClass()) {
			return false;
		}

		final Point point = (Point)that;

		return Double.compare(point.x(), x()) == 0 && Double.compare(point.y(), y()) == 0;
	}

	public boolean equals(final Point that) {
		return this == that || (that != null && Double.compare(that.x(), x()) == 0 &&
				Double.compare(that.y(), y()) == 0);
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = x() != +0.0d ? Double.doubleToLongBits(x()) : 0L;
		result = (int)(temp ^ (temp >>> 32));
		temp = y() != +0.0d ? Double.doubleToLongBits(y()) : 0L;
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		return result;
	}
}
