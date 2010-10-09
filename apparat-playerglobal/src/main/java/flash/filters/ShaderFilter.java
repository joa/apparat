package flash.filters;

import flash.display.Shader;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public class ShaderFilter extends BitmapFilter {
	private Shader _shader;

	private int _topExtension;
	private int _rightExtension;
	private int _bottomExtension;
	private int _leftExtension;

	public ShaderFilter() {
		this(null);
	}

	public ShaderFilter(final Shader shader) {
		_shader = shader;
	}

	public Shader shader() { return _shader; }
	public void shader(final Shader value) { _shader = value; }

	public int topExtension() { return _topExtension; }
	public void topExtension(final int value) { _topExtension = value; }

	public int rightExtension() { return _rightExtension; }
	public void rightExtension(final int value) { _rightExtension = value; }

	public int bottomExtension() { return _bottomExtension; }
	public void bottomExtension(final int value) { _bottomExtension = value; }

	public int leftExtension() { return _leftExtension; }
	public void leftExtension(final int value) { _leftExtension = value; }
}
