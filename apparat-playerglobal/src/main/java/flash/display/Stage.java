package flash.display;

import org.lwjgl.opengl.GL11;

/**
 * @author Joa Ebert
 */
public final class Stage extends DisplayObjectContainer {
	private double _rate = 32.0;
	private String _quality = "high";
	private String _scaleMode = "none";

	public void frameRate(double value) {
		_rate = value;
	}

	public double frameRate() {
		return _rate;
	}

	public void quality(String value) {
		_quality = value;
	}

	public String quality() {
		return _quality;
	}

	public void scaleMode(String value) {
		_scaleMode = value;
	}

	public String scaleMode() {
		return _scaleMode;
	}

	@Override
	protected final void JITB$render() {
		//
		// Clear the screen when the stage is re-rendered.
		//

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		//
		// Translate corresponding to stage align.
		//

		GL11.glTranslatef(0.0f, 0.0f, 0.0f);

		//
		// Now render all children of the stage.
		//

		JITB$renderChildren();
	}
}
