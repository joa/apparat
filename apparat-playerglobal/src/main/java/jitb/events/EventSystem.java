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
package jitb.events;

import flash.events.Event;
import flash.events.IEventDispatcher;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The EventSystem class is a manager for concurrent tasks.
 *
 * <p>The importance of the EventSystem is best explained with an
 * asynchronous task like the URLLoader. When <code>URLLoader.load</code> is
 * called a new asynchronous task is started.
 * This task can be scheduled with <code>EventSystem.execute</code> to run
 * the task in a thread pool. However we have to pass a message back to ActionScript
 * once the task has finished executing. The Flash Player dispatches such events outside
 * of the <code>[ENTER_FRAME, EXIT_FRAME]</code> interval.</p>
 *
 * <p>When a task wants to dispatch an event like <code>Event.COMPLETE</code> via its original
 * dispatcher the <code>EventSystem.delayedDispatch</code> method should be used. This
 * guarantees that the event will occur in the right position and in the main thread
 * of the ActionScript code execution.</p>
 *
 * @author Joa Ebert
 */
public final class EventSystem {
	private static final class Pair {
		public final IEventDispatcher target;
		public final Event event;
		public final CountDownLatch latch;
		public final Runnable callback;

		public Pair(final IEventDispatcher target, final Event event,
								final CountDownLatch latch, final Runnable callback) {
			this.target = target;
			this.event = event;
			this.latch = latch;
			this.callback = callback;
		}
	}

	private static final ExecutorService _executor = Executors.newCachedThreadPool();
	private static final ReentrantLock _lock = new ReentrantLock();
	private static final Queue<Pair> _queue = new LinkedList<Pair>();

	public static void shutdown() {
		_executor.shutdown();
	}

	public static void execute(final Runnable command) {
		_executor.execute(command);
	}

	public static void delayedDispatch(final IEventDispatcher target, final Event event) {
		delayedDispatch(target, event, null);
	}

	public static void delayedDispatch(final IEventDispatcher target, final Event event,
																		 final CountDownLatch latch) {
		delayedDispatch(target, event, latch, null);
	}

	public static void delayedDispatch(final IEventDispatcher target, final Event event,
																		 final CountDownLatch latch, final Runnable callback) {
		final Pair pair = new Pair(target, event, latch, callback);

		_lock.lock();

		try {
			_queue.add(pair);
		} finally {
			_lock.unlock();
		}
	}

	public static void futureDispatch(final IEventDispatcher target, final Event event) {
		final CountDownLatch latch = new CountDownLatch(1);

		delayedDispatch(target, event, latch);

		try {
			latch.await();
		} catch(final InterruptedException interrupt) {
			interrupt.printStackTrace();
		}
	}

	public static void callbackDispatch(final IEventDispatcher target, final Event event,
																			final Runnable callback) {
		delayedDispatch(target, event, null, callback);
	}

	public static void dispatchEvents() {
		final Pair[] pairs;

		_lock.lock();

		try {
			pairs = new Pair[_queue.size()];
			_queue.toArray(pairs);
			_queue.clear();
		} finally {
			_lock.unlock();
		}

		for(final Pair p : pairs) {
			p.target.dispatchEvent(p.event);
			if(null != p.latch) {
				p.latch.countDown();
			}

			if(null != p.callback) {
				p.callback.run();
			}
		}
	}

	private EventSystem() {}
}
