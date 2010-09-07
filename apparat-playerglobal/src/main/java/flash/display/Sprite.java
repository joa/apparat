package flash.display;

/**
 * @author Joa Ebert
 */
public class Sprite extends DisplayObjectContainer {
	private final Graphics _graphics = new Graphics();

	public Graphics graphics() {
		return _graphics;
	}

	@Override
	protected final void JITB$render() {
		JITB$renderChildren();
		_graphics.JITB$render();
	}
}
