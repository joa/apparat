package flash.display;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Joa Ebert
 */
public abstract class DisplayObjectContainer extends InteractiveObject {
	private final List<DisplayObject> _children = new LinkedList<DisplayObject>();

	public DisplayObject addChild(final DisplayObject child) {
		_children.add(child);
		return child;
	}

	protected final void renderChildren() {
		for(final DisplayObject child : _children) {
			child.render();
		}
	}
}
