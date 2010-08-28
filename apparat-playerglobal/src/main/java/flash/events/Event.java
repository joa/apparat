package flash.events;

/**
 * @author Joa Ebert
 */
public class Event extends jitb.lang.Object {
	private final String _type;
	private final boolean _bubbles;
	private final boolean _cancelable;
	private boolean _isDefaultPrevented = false;

	public Event(final String type) {
		this(type, false);
	}

	public Event(final String type, final boolean bubbles) {
		this(type, bubbles, false);
	}

	public Event(final String type, final boolean bubbles, final boolean cancelable) {
		_type = type;
		_bubbles = bubbles;
		_cancelable = cancelable;
	}

	public boolean isDefaultPrevented() {
		return _isDefaultPrevented;
	}

	public long eventPhase() {
		return 0;
	}

	public Event clone() {
		return new Event(type(), bubbles(), cancelable());
	}

	public boolean bubbles() {
		return _bubbles;
	}

	public void preventDefault() {
		_isDefaultPrevented = true;
	}

	public void stopPropagation() {
	}

	public Object target() {
		return null;
	}

	public boolean cancelable() {
		return _cancelable;
	}

	public Object currentTarget() {
		return null;
	}

	public String type() {
		return _type;
	}

	public void stopImmediatePropagation() {
	}

	public final static String CANCEL = "cancel";
	public final static String ENTER_FRAME = "enterFrame";
	public final static String SOUND_COMPLETE = "soundComplete";
	public final static String UNLOAD = "unload";
	public final static String INIT = "init";
	public final static String RENDER = "render";
	public final static String TAB_ENABLED_CHANGE = "tabEnabledChange";
	public final static String ADDED_TO_STAGE = "addedToStage";
	public final static String FRAME_CONSTRUCTED = "frameConstructed";
	public final static String TAB_CHILDREN_CHANGE = "tabChildrenChange";
	public final static String CUT = "cut";
	public final static String CLEAR = "clear";
	public final static String CHANGE = "change";
	public final static String RESIZE = "resize";
	public final static String COMPLETE = "complete";
	public final static String FULLSCREEN = "fullScreen";
	public final static String SELECT_ALL = "selectAll";
	public final static String REMOVED = "removed";
	public final static String CONNECT = "connect";
	public final static String SCROLL = "scroll";
	public final static String OPEN = "open";
	public final static String CLOSE = "close";
	public final static String MOUSE_LEAVE = "mouseLeave";
	public final static String ADDED = "added";
	public final static String REMOVED_FROM_STAGE = "removedFromStage";
	public final static String EXIT_FRAME = "exitFrame";
	public final static String TAB_INDEX_CHANGE = "tabIndexChange";
	public final static String PASTE = "paste";
	public final static String DEACTIVATE = "deactivate";
	public final static String COPY = "copy";
	public final static String ID3 = "id3";
	public final static String ACTIVATE = "activate";
	public final static String SELECT = "select";
}
