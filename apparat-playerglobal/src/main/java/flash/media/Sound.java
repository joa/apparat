package flash.media;

import flash.events.EventDispatcher;
import flash.net.URLRequest;
import flash.utils.ByteArray;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;
import jitb.media.AS3SoundSource;
import jitb.media.JLayerSoundSource;
import jitb.util.PathUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @author Joa Ebert
 */
@Metadata({
		@Element(name="Event", keys={"name", "type"}, values={"progress", "flash.events.ProgressEvent"}),
		@Element(name="Event", keys={"name", "type"}, values={"open", "flash.events.Event"}),
		@Element(name="Event", keys={"name", "type"}, values={"ioError", "flash.events.IOErrorEvent"}),
		@Element(name="Event", keys={"name", "type"}, values={"id3", "flash.events.Event"}),
		@Element(name="Event", keys={"name", "type"}, values={"complete", "flash.events.Event"}),
		@Element(name="Event", keys={"name", "type"}, values={"sampleData", "flash.events.SampleDataEvent"})
})
public class Sound extends EventDispatcher {
	private long _bytesLoaded;
	private long _bytesTotal;
	private ID3Info _id3;
	private boolean _isBuffering;
	private double _length;
	private String _url;

	private URLRequest _stream;
	private SoundLoaderContext _context;

	public Sound() {
		this(null);
	}

	public Sound(final URLRequest stream) {
		this(stream, null);
	}

	public Sound(final URLRequest stream, final SoundLoaderContext context) {
		_stream = stream;
		_context = context;
	}

	@Metadata({@Element(name="Version", keys={""}, values={"10"})})
	public double extract(final ByteArray target, final double length) {
		return extract(target, length, -1.0);
	}

	@Metadata({@Element(name="Version", keys={""}, values={"10"})})
	public double extract(final ByteArray target, final double length, final double startPosition) {
		return 0.0;
	}

	public void load(final URLRequest stream) {
		load(stream, null);
	}

	public void load(final URLRequest stream, final SoundLoaderContext context) {

	}

	public void close() {
		
	}

	public String url() {
		if(null == _stream) {
			return null;
		}
		
		return _stream.url();
	}

	public long bytesLoaded() {
		return _bytesLoaded;
	}

	public long bytesTotal() {
		return _bytesTotal;
	}

	public SoundChannel play() {
		return play(0);
	}

	public SoundChannel play(final double startTime) {
		return play(startTime, 0);
	}

	public SoundChannel play(final double startTime, final int loops) {
		return play(startTime, loops, null);
	}

	public SoundChannel play(final double startTime, final int loops, final SoundTransform sndTransform) {
		if(null == _stream) {
			return new SoundChannel(new AS3SoundSource(this));
		} else {
			final String path = PathUtil.createPath(_stream.url());

			try {
				if(path.startsWith("/") || path.indexOf(':') == 1) {
					return new SoundChannel(new JLayerSoundSource(this, new FileInputStream(path)));
				} else {
					return new SoundChannel(new JLayerSoundSource(this, new URL(_stream.url()).openStream()));
				}
			} catch(IOException e) {
				e.printStackTrace();
				//todo throw ioerror
				return null;
			}
		}
	}

	public double length() { return 0.0; }

	public ID3Info id3() { return _id3; }

	public boolean isBuffering() {
		return false;
	}
}
