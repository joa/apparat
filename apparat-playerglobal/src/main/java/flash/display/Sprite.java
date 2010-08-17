package flash.display;

/**
 * @author Joa Ebert
 */
public class Sprite extends DisplayObjectContainer {
	@Override
	public final void render() {
		renderChildren();
	}
}
