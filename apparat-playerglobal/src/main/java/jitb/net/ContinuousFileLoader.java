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
				final File file = new File(continuousFileLoader._file);
				long lastModified = 0L;

				try {
					while(continuousFileLoader._running && !Thread.interrupted()) {
						if(file.lastModified() > lastModified) {
							lastModified = file.lastModified();

							FileInputStream fis = null;
							FileChannel fc = null;

							try {
								fis = new FileInputStream(file);
								fc = fis.getChannel( );
								ByteBuffer bb = ByteBuffer.allocateDirect((int)file.length());
								int numBytesRead;
								do { numBytesRead = fc.read(bb); } while(numBytesRead > 0);
								continuousFileLoader._data = ByteArray.JITB$fromBuffer(bb);
								EventSystem.delayedDispatch(continuousFileLoader, new Event(Event.COMPLETE));
							} catch(FileNotFoundException e) {
								/* ignored */
							} catch(IOException e) {
								/* ignored */
							} finally {
								if(null != fc) {
									try { fc.close();
										System.out.println("close fc"); } catch(Throwable t) { /*nada*/ }
								}
								if(null != fis) {
									try { fis.close();
										System.out.println("close fis");} catch(Throwable t) { /*nada*/ }
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
