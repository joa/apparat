package flash.geom;

import java.util.Timer;

/**
 * @author Joa Ebert
 */
public class Rectangle {
	private double _x;
	private double _y;
	private double _width;
	private double _height;

	public Rectangle(final double x, final double y, final double width, final double height) {
		_x = x;
		_y = y;
		_width = width;
		_height = height;
	}

	public double x() { return _x; }
	public void x(final double value) { _x = value; }

	public double y() { return _y; }
	public void y(final double value) { _y = value; }

	public double width() { return _width; }
	public void width(final double value) { _width = value; }

	public double height() { return _height; }
	public void height(final double value) { _height = value; }

	public String toString() {
		return "[Rectangle x: "+x()+", y: "+y()+", width: "+width()+", height: "+height()+"]";
	}
}
