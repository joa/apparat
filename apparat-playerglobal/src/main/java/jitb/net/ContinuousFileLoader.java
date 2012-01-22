/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
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
