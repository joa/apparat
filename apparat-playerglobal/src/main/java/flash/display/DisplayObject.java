package flash.display;

import flash.events.EventDispatcher;
import jitb.display.DisplayList;

/**
 * @author Joa Ebert
 */
public class DisplayObject extends EventDispatcher implements IBitmapDrawable {
	public DisplayObject() {
		DisplayList.register(this);
	}
	
	public Stage stage() { return null; }
}
