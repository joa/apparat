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
