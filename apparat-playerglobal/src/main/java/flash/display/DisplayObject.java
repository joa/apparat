package flash.display;

import flash.events.EventDispatcher;
import jitb.display.DisplayList;
import jitb.display.IDisplayObject;

/**
 * @author Joa Ebert
 */
public abstract class DisplayObject extends EventDispatcher implements IBitmapDrawable, IDisplayObject {
	public DisplayObject() {
		DisplayList.register(this);
	}
	
	public Stage stage() { return null; }

	@Override
	public abstract void render();
}
