/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package flash.geom;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Joa Ebert
 */
public class Rectangle extends jitb.lang.Object {
	private double _x;
	private double _y;
	private double _width;
	private double _height;

	public Rectangle() {
		this(0.0);
	}

	public Rectangle(final double x) {
		this(x, 0.0);
	}

	public Rectangle(final double x, final double y) {
		this(x, y, 0.0);
	}

	public Rectangle(final double x, final double y, final double width) {
		this(x, y, width, 0.0);
	}

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

	public double bottom() { return y() + height(); }

	public Point bottomRight() { return new Point(right(), bottom()); }

	public double left() { return x(); }

	public double right() { return x() + width(); }

	public Point size() { return new Point(width(), height()); }

	public double top() { return y(); }

	public Point topLeft() { return new Point(x(), y()); }

	public Rectangle clone() {
		return new Rectangle(x(), y(), width(), height());
	}

	public boolean contains(final double x, final double y) {
		return (x >= left() && x <= right() && y >= top() && y <= bottom());
	}

	public boolean containsPoint(final Point point) {
		return contains(point.x, point.y);
	}

	public boolean containsRect(final Rectangle rect) {
		return containsPoint(rect.topLeft()) && containsPoint(rect.bottomRight());
	}

	public boolean equals(final Rectangle toCompare) {
		return null != toCompare && x() == toCompare.x() && y() == toCompare.y() &&
				width() == toCompare.width() && height() == toCompare.height();
	}

	public void inflate(final double dx, final double dy) {
		x(x() - dx);
		y(y() - dy);
		width(width() + 2.0 * dx);
		height(height() + 2.0 * dy);
	}

	public void inflatePoint(final Point point) {
		inflate(point.x, point.y);
	}

	public Rectangle intersection(final Rectangle toIntersect) {
		final boolean containsTopLeft = containsPoint(toIntersect.topLeft());
		final boolean containsBottomRight = containsPoint(toIntersect.bottomRight());

		if(containsTopLeft && containsBottomRight) {
			return toIntersect.clone();
		} else if(containsTopLeft) {
			final Point p = toIntersect.topLeft();
			return new Rectangle(p.x, p.y, right() - p.x, bottom() - p.y);
		} else if(containsBottomRight) {
			final Point p = toIntersect.bottomRight();
			return new Rectangle(x(), y(), p.x - x(), p.y - y());
		} else {
			return new Rectangle();
		}
	}

	public boolean intersects(final Rectangle toIntersect) {
		return containsPoint(toIntersect.topLeft()) || containsPoint(toIntersect.bottomRight());
	}

	public boolean isEmpty() {
		return width() == 0.0 && height() == 0.0;
	}

	public void offset(final double dx, final double dy) {
		x(x() + dx);
		y(y() + dy);
	}

	public void offsetPoint(final Point point) {
		offset(point.x, point.y);
	}

	public void setEmpty() {
		x(0.0);
		y(0.0);
		width(0.0);
		height(0.0);
	}

	@Override
	public String toString() {
		return "(x="+x()+", y="+y()+", width="+width()+", height="+height()+")";
	}

	public Rectangle union(final Rectangle toUnion) {
		if(isEmpty()) {
			if(toUnion.isEmpty()) {
				return new Rectangle();
			} else {
				return toUnion.clone();
			}
		} else {
			if(toUnion.isEmpty()) {
				return clone();
			} else {
				final double top = Math.min(top(), toUnion.top());
				final double left = Math.min(left(), toUnion.left());
				final double bottom = Math.max(bottom(), toUnion.bottom());
				final double right = Math.max(right(), toUnion.right());

				return new Rectangle(left, top, right - left, bottom - top);
			}
		}
	}

	public void JITB$render() {
		JITB$render(true);
	}

	public void JITB$render(final boolean normalizedTexCoord) {
		final int width = (int)width();
		final int height = (int)height();

		glPolygonMode(GL_FRONT, GL_FILL);
		if(normalizedTexCoord) {
			glBegin(GL_QUADS); {
				glTexCoord2f(0.0f, 0.0f);
				glVertex2i(0, 0);

				glTexCoord2f(1.0f, 0.0f);
				glVertex2i(width, 0);

				glTexCoord2f(1.0f, 1.0f);
				glVertex2i(width, height);

				glTexCoord2f(0.0f, 1.0f);
				glVertex2i(0, height);
			} glEnd();
		} else {
			final float widthf = (float)width;
			final float heightf = (float)height;

			glBegin(GL_QUADS); {
				glTexCoord2f(0.0f, 0.0f);
				glVertex2i(0, 0);

				glTexCoord2f(widthf, 0.0f);
				glVertex2i(width, 0);

				glTexCoord2f(widthf, heightf);
				glVertex2i(width, height);

				glTexCoord2f(0.0f, heightf);
				glVertex2i(0, height);
			} glEnd();
		}
	}
}
