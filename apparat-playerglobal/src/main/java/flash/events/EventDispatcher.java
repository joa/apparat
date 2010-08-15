package flash.events;

import jitb.Function;
import jitb.Function1;

/**
 * @author Joa Ebert
 */
public class EventDispatcher implements IEventDispatcher {
	@Override
	public boolean dispatchEvent(Event event) {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean hasEventListener(String type) {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean willTrigger(String type) {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
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
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void removeEventListener(String type, Function<Object> listener) {
		removeEventListener(type, listener, false);
	}

	@Override
	public void removeEventListener(String type, Function<Object> listener, boolean useCapture) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
