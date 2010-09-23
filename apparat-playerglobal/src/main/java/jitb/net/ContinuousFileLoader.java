package jitb.net;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.ByteArray;
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
public final class ContinuousFileLoader extends EventDispatcher {
	private final String _file;
	private boolean _running = false;
	private ByteArray _data;
	
	public ContinuousFileLoader(final String file) {
		_file = PathUtil.createPath(file);
	}

	public ByteArray data() {
		return _data;
	}
	
	public void start() {
		final ContinuousFileLoader continuousFileLoader = this;
		_running = true;
		EventSystem.execute(new Runnable() {
			@Override
			public void run() {
				File file = null;
				long lastModified = 0L;

				try {
					while(continuousFileLoader._running && !Thread.interrupted()) {
						file = new File(continuousFileLoader._file);

						if(file.lastModified() > lastModified) {
							lastModified = file.lastModified();

							FileInputStream fis = null;
							FileChannel fc = null;

							try {
								fis = new FileInputStream(file);
								fc = fis.getChannel();
								final int fs = (int)fc.size();
								final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fs);
								MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fs);
								bb.load();
								byteBuffer.put(bb);
								bb = null;
								byteBuffer.flip();
								fc.close();
								continuousFileLoader._data = ByteArray.JITB$fromBuffer(byteBuffer);
								EventSystem.delayedDispatch(continuousFileLoader, new Event(Event.COMPLETE));
							} catch(FileNotFoundException e) {
								/* ignored */
							} catch(IOException e) {
								/* ignored */
							} finally {
								file = null;
								
								if(null != fc) {
									try { fc.close(); } catch(Throwable t) { /*nada*/ } 
								}
								if(null != fis) {
									try { fis.close(); } catch(Throwable t) { /*nada*/ }
								}
							}
						}

						Thread.sleep(500L);
					}
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void stop() {
		_running = false;
	}
}
