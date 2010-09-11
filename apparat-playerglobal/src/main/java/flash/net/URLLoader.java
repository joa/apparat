package flash.net;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.ByteArray;
import jitb.errors.Require;
import jitb.events.EventSystem;
import jitb.util.PathUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Joa Ebert
 */
public class URLLoader extends EventDispatcher {
	public long bytesLoaded = 0L;
	public long bytesTotal = 0L;
	public jitb.lang.Object data = null;
	public String dataFormat = URLLoaderDataFormat.TEXT;

	private URLRequest _request;

	private Runnable _runnable;

	public URLLoader() { this(null); }
	
	public URLLoader(final URLRequest request) {
		_request = request;
	}

	public void close() {

	}

	public void load() {
		Require.nonNull("request", _request);
		load(_request);
	}

	public void load(final URLRequest request) {
		_request = request;

		final String path = PathUtil.createPath(_request.url());

		if(path.startsWith("/") || path.indexOf(':') == 1) {
			loadFile(path);
		}
	}

	private void loadFile(final String pathname) {
		final URLLoader urlLoader = this;
		EventSystem.execute(new Runnable() {
			@Override
			public void run() {
				final File file = new File(pathname);
				FileInputStream fis = null;

				try {
					fis = new FileInputStream(file);
					final FileChannel fc = fis.getChannel();
					final int fs = (int)fc.size();
					final MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fs);
					final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fs);
					bb.load();
					byteBuffer.put(bb);
					byteBuffer.flip();
					fc.close();
					data = ByteArray.JITB$fromBuffer(byteBuffer);
					EventSystem.delayedDispatch(urlLoader, new Event(Event.COMPLETE));
				} catch(FileNotFoundException e) {
					//dispatch IOErrorEvent
					e.printStackTrace();
				} catch(IOException e) {
					//dispatch IOErrorEvent
					e.printStackTrace();
				} finally {
					if(null != fis) {
						try { fis.close(); } catch(Throwable t) { /*nada*/ }
					}
				}
			}
		});
	}
}
