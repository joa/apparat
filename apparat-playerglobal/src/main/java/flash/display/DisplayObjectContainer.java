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
package flash.display;

import java.util.AbstractSequentialList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Joa Ebert
 */
public abstract class DisplayObjectContainer extends InteractiveObject {
	private final List<DisplayObject> _children = new LinkedList<DisplayObject>();

	public int numChildren() {
		return _children.size();
	}

	public DisplayObject addChildAt(final int index, final DisplayObject child) {
		_children.add(index, child);
		return child;
	}

	public DisplayObject addChild(final DisplayObject child) {
		return addChildAt(_children.size(), child);
	}

	protected final void JITB$renderChildren() {
		for(final DisplayObject child : _children) {
			child.JITB$renderDisplayObject();
		}
	}
}
