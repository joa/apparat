package flash.events;

import flash.utils.ByteArray;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public class SampleDataEvent extends Event {
	public static final String SAMPLE_DATA = "sampleData";

	private double _position;
	private ByteArray _data;

	public SampleDataEvent(final String type) {
		this(type, false);
	}

	public SampleDataEvent(final String type, final boolean bubbles) {
		this(type, bubbles, false);
	}

	public SampleDataEvent(final String type, final boolean bubbles, final boolean cancelable) {
		this(type, bubbles, cancelable, 0.0);
	}

	public SampleDataEvent(final String type, final boolean bubbles, final boolean cancelable, final double theposition) {
		this(type, bubbles, cancelable, theposition, null);
	}

	public SampleDataEvent(final String type, final boolean bubbles, final boolean cancelable, final double theposition, final ByteArray thedata) {
		super(type, bubbles, cancelable);

		_position = theposition;
	}

	public double position() { return _position; }
	public void position(final double value) { _position = value; }

	public ByteArray data() { return _data; }
	public void data(final ByteArray value) { _data = value; }

	@Override
	public String toString() {
		return "[SampleDataEvent]";
	}

	@Override
	public Event clone() {
		return new SampleDataEvent(type(), bubbles(), cancelable(), position(), data());
	}
}
