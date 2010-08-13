package flash.events;

import jitb.Function1;

/**
 * @author Joa Ebert
 */
public interface IEventDispatcher {
	void addEventListener(Function1<Event,Object> value, boolean useCapture, int priority, boolean useWeakReference);
}
