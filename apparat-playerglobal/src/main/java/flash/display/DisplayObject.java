package flash.display;

import flash.events.EventDispatcher;

/**
 * @author Joa Ebert
 */
public class DisplayObject extends EventDispatcher implements IBitmapDrawable {
	public Stage stage() {
		return new Stage();
	}
}
