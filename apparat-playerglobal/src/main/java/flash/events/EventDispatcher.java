package flash.events;

import jitb.Function;

import java.util.*;

/**
 * @author Joa Ebert
 */
public class EventDispatcher implements IEventDispatcher {
	private final class EventListener implements Comparable<EventListener> {
		public final Function<Object> callback;
		public final boolean useCapture;
		public final int priority;

		public EventListener(final Function<Object> callback, final boolean useCapture, final int priority) {
			this.callback = callback;
			this.useCapture = useCapture;
			this.priority = priority;
		}

		@Override
		public int compareTo(final EventListener that) {
			if(null == that || this.priority > that.priority) {
				return 1;
			} else if(this.priority < that.priority) {
				return -1;
			} else {
				return 0;
			}
		}
	}


	private Map<String, List<EventListener>> map = new HashMap<String, List<EventListener>>();

	@Override
	public boolean dispatchEvent(final Event event) {
		final List<EventListener> listOfListenersForType = map.get(event.type());

		if(null != listOfListenersForType && listOfListenersForType.size() > 0) {
			for(final EventListener listener : listOfListenersForType) {
				listener.callback.apply(event);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean hasEventListener(String type) {
		final List<EventListener> listOfListenersForType = map.get(type);
		return null != listOfListenersForType && listOfListenersForType.size() > 0;
	}

	@Override
	public boolean willTrigger(String type) {
		return false;
	}

	@Override
	public void addEventListener(String type, Function<Object> listener) {
		addEventListener(type, listener, false);
	}

	@Override
	public void addEventListener(String type, Function<Object> listener, boolean useCapture) {
		addEventListener(type, listener, useCapture, 0);
	}

	@Override
	public void addEventListener(String type, Function<Object> listener, boolean useCapture, int priority) {
		addEventListener(type, listener, useCapture, priority, false);
	}

	@Override
	public void addEventListener(String type, Function<Object> listener, boolean useCapture, int priority, boolean useWeakReference) {
		List<EventListener> listOfListenersForType = map.get(type);

		if(null == listOfListenersForType) {
			listOfListenersForType = new LinkedList<EventListener>();
			map.put(type, listOfListenersForType);
		}

		//TODO add weak reference here.
		listOfListenersForType.add(new EventListener(listener, useCapture, priority));
	}

	@Override
	public void removeEventListener(String type, Function<Object> listener) {
		removeEventListener(type, listener, false);
	}

	@Override
	public void removeEventListener(String type, Function<Object> listener, boolean useCapture) {
		List<EventListener> listOfListenersForType = map.get(type);

		if(null == listOfListenersForType) {
			return;
		}

		final Iterator<EventListener> iter = listOfListenersForType.listIterator();

		while(iter.hasNext()) {
			final EventListener internalListener = iter.next();

			if(internalListener.useCapture == useCapture &&
					internalListener.callback == listener) {
				iter.remove();
				return;
			}
		}
	}
}
