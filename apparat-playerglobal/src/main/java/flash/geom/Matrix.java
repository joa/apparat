package flash.geom;

import jitb.errors.MissingImplementationException;
import org.lwjgl.BufferUtils;

import java.nio.DoubleBuffer;

/**
 * @author Joa Ebert
 */
public class Matrix extends jitb.lang.Object {
	public double a;
	public double b;
	public double c;
	public double d;
	public double tx;
	public double ty;

	public Matrix() {
		this(1.0);
	}
	
	public Matrix(final double a) {
		this(a, 0.0);
	}

	public Matrix(final double a, final double b) {
		this(a, b, 0.0);
	}

	public Matrix(final double a, final double b, final double c) {
		this(a, b, c, 1.0);
	}

	public Matrix(final double a, final double b, final double c, final double d) {
		this(a, b, c, d, 0.0);
	}

	public Matrix(final double a, final double b, final double c, final double d,
				  final double tx) {
		this(a, b, c, d, tx, 0.0);
	}

	public Matrix(final double a, final double b, final double c, final double d,
				  final double tx, final double ty) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.tx = tx;
		this.ty = ty;
	}

	public Matrix clone() {
		return new Matrix(a, b, c, d, tx, ty);
	}

	public void concat(final Matrix m) {
		// a c tx     a c tx
		// b d ty  x  b d ty
		// 0 0 1      0 0 1
		
		final double ta = a;
		final double tb = b;
		final double tc = c;
		final double td = d;
		final double ttx = tx;
		final double tty = ty;

		a = ta * m.a + tc * m.b;
		b = tb * m.a + td * m.b;
		c = ta * m.c + tc * m.d;
		d = tb * m.c + td * m.d;
		tx = ta * m.tx + tc * m.ty + ttx;
		ty = tb * m.tx + td * m.ty + tty;
	}

	public void createBox(final double scaleX, final double scaleY) {
		createBox(scaleX, scaleY, 0.0);
	}

	public void createBox(final double scaleX, final double scaleY, final double rotation) {
		createBox(scaleX, scaleY, rotation, 0.0);
	}

	public void createBox(final double scaleX, final double scaleY, final double rotation,
						  final double tx) {
		createBox(scaleX, scaleY, rotation, tx, 0.0);
	}

	public void createBox(final double scaleX, final double scaleY, final double rotation,
						  final double tx, final double ty) {
		identity();
		rotate(rotation);
		scale(scaleX, scaleY);
		translate(tx, ty);
	}

	public void createGradientBox(final double width, final double height) {
		createGradientBox(width, height, 0.0);
	}

	public void createGradientBox(final double width, final double height, final double rotation) {
		createGradientBox(width, height, rotation, 0.0);
	}

	public void createGradientBox(final double width, final double height,
								  final double rotation, final double tx) {
		createGradientBox(width, height, rotation, tx, 0.0);
	}

	public void createGradientBox(final double width, final double height,
								  final double rotation, final double tx, final double ty) {
		throw new MissingImplementationException("Matrix.createGradientBox");
	}

	public Point deltaTransformPoint(final Point point) {
		// a c tx     x
		// b d ty  x  y
		// 0 0 1      1
		
		final double x = point.x;
		final double y = point.y;

		return new Point(x + a * x + c * y + tx, y + b * x + d * y + ty);
	}

	public void identity() {
		a = d = 1.0;
		b = c = tx = ty = 0.0;
	}

	private double determinant() {
		return a * d - b * c;
	}

	public void invert() {
		final double det = determinant();
		final double dd = (0.0 == det) ? 1.0 : det;

		final double ta = a;
		final double tb = b;
		final double tc = c;
		final double td = d;
		final double ttx = tx;
		final double tty = ty;

		a = td / dd;
		b = -tb / dd;
		c = -(tc - ttx) / dd;
		d = ta / dd;
		tx = -(tc - ttx * td) / dd;
		ty = -(ta * tty - ttx * tb) / dd;
	}

	public void rotate(final double angle) {
		final double sin = Math.sin(angle);
		final double cos = Math.cos(angle);
		concat(new Matrix(cos, sin, -sin, cos, 0.0, 0.0));
	}

	public void scale(final double sx, final double sy) {
		concat(new Matrix(sx, 0.0, 1.0, sy, 0.0, 0.0));
	}

	@Override
	public String toString() {
		return "(a="+a+", b="+b+", c="+c+", d="+d+", tx="+tx+", ty="+ty+")";
	}

	public void translate(final double dx, final double dy) {
		concat(new Matrix(1.0, 0.0, 1.0, 0.0, dx, dy));
	}

	public void JITB$toDoubleBuffer(final DoubleBuffer value) {
		//
		// a c tx    a c 0 tx
		// b d ty -> b d 0 ty
		// 0 0 1     0 0 1 0
		//           0 0 0 1

		value.clear();
		value.put(a).put(b).put(0.0).put(0.0).
				put(c).put(d).put(0.0).put(0.0).
				put(0.0).put(0.0).put(1.0).put(0.0).
				put(tx).put(ty).put(0.0).put(1.0);
		value.flip();
	}

	public DoubleBuffer JITB$toDoubleBuffer() {
		final DoubleBuffer result = BufferUtils.createDoubleBuffer(16);
		JITB$toDoubleBuffer(result);
		return result;
	}
}
