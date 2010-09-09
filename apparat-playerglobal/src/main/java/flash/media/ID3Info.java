package flash.media;

import java.util.HashMap;

/**
 * @author Joa Ebert
 */
public final class ID3Info extends jitb.lang.Object {
	public String album;
	public String artist;
	public String comment;
	public String genre;
	public String songName;
	public String track;
	public String year;

	private final HashMap<String, Object> _dynamic = new HashMap<String, Object>();
	@Override public void JITB$setProperty(String property, Object value) { _dynamic.put(property, value); }
	@Override public Object JITB$getProperty(String property) { return _dynamic.get(property); }
}
