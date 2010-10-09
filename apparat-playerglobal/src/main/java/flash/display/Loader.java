package flash.display;

import flash.events.Event;
import flash.net.URLLoader;
import flash.net.URLLoaderDataFormat;
import flash.net.URLRequest;
import flash.system.LoaderContext;
import flash.utils.ByteArray;
import jitb.errors.Require;
import jitb.lang.closure.Function1;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Joa Ebert
 */
public class Loader extends DisplayObjectContainer {
	private DisplayObject _content;
	private final LoaderInfo _contentLoaderInfo = new LoaderInfo();
	private LoaderContext _loaderContext;

	private URLLoader _urlLoader;

	public Loader() {}

	public void close() {
		
	}
	
	public DisplayObject content() { return _content; }

	public LoaderInfo contentLoaderInfo() { return _contentLoaderInfo; }

	//load(request:URLRequest, context:LoaderContext = null):void

	public void load(final URLRequest request) {
		load(request, null);
	}

	public void load(final URLRequest request, final LoaderContext context) {
		_urlLoader = new URLLoader();
		_urlLoader.dataFormat = URLLoaderDataFormat.BINARY;
		_urlLoader.addEventListener(Event.COMPLETE,
			new Function1<Event, Object>() {
				@Override
				public Object apply1(final jitb.lang.Object thisArg, final Event value) {
					applyVoid1(thisArg, value);
					return null;
				}

				@Override
				public void applyVoid1(jitb.lang.Object thisArg, Event value) {
					onURLLoaderComplete();
				}
			}, false, 0, true);
		_urlLoader.load(request);
	}

	public void loadBytes(final ByteArray bytes) {
		loadBytes(bytes, null);
	}

	public void loadBytes(final ByteArray bytes, final LoaderContext context) {
		Require.nonNull("bytes", bytes);
		final ByteBuffer buffer = bytes.JITB$buffer();

		try {
			final byte[] buf = new byte[bytes.JITB$buffer().capacity()];
			bytes.JITB$buffer().get(buf);//todo fixme and get rid of buffer
			final BufferedImage image = ImageIO.read(new ByteArrayInputStream(buf));
			final BitmapData bitmapData = BitmapData.JITB$fromImage(image);
			_content = new Bitmap(bitmapData);
		} catch(IOException e) {
			e.printStackTrace();
			throw new Error("IOError");//todo dispatch proper error...
		}

		_contentLoaderInfo.dispatchEvent(new Event(Event.COMPLETE));
	}

	public void unload() {
		_content = null;
	}

	public void unloadAndStop() {
		unloadAndStop(true);
	}

	public void unloadAndStop(final boolean gc) {
		if(gc) {
			// We ignore this flag since it is more specific to the Flash Player
			// and not required in the JVM.
		}

		_content = null;
	}

	private void onURLLoaderComplete() {
		loadBytes((ByteArray)_urlLoader.data, _loaderContext);
	}

	@Override
	protected void JITB$render() {
		if(null != content()) {
			content().JITB$render();
		}
	}
}
