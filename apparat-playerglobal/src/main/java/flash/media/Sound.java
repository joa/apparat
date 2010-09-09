package flash.media;

import flash.events.EventDispatcher;
import flash.net.URLRequest;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;

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
		/*
		 [Version("10")]
  native public function extract(target:flash.utils.ByteArray,length:Number,startPosition:Number = -1):Number;
  native public function load(stream:flash.net.URLRequest,context:flash.media.SoundLoaderContext = null):void;
  native public function close():void;

  native public function get url():String;
  native public function get bytesLoaded():uint;
  native public function play(startTime:Number = 0,loops:int = 0,sndTransform:flash.media.SoundTransform = null):flash.media.SoundChannel;
  native public function get length():Number;

  native public function get id3():flash.media.ID3Info;
  native public function get bytesTotal():int;
  native public function get isBuffering():Boolean;
		 */
}
