package flash.media;

import jitb.lang.AVM;

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
	
	@Override public void JITB$setProperty(final String property, final Object value) {
		if(property.equals("album")) {
			AVM.coerce(value, String.class);
			album = (String)value;
		} else if(property.equals("artist")) {
			AVM.coerce(value, String.class);
			artist = (String)value;
		} else if(property.equals("comment")) {
			AVM.coerce(value, String.class);
			comment = (String)value;
		} else if(property.equals("genre")) {
			AVM.coerce(value, String.class);
			genre = (String)value;
		} else if(property.equals("songName")) {
			AVM.coerce(value, String.class);
			songName = (String)value;
		} else if(property.equals("track")) {
			AVM.coerce(value, String.class);
			track = (String)value;
		} else if(property.equals("year")) {
			AVM.coerce(value, String.class);
			year = (String)value;
		} else {
			_dynamic.put(property, value);
		}
	}
	
	@Override public Object JITB$getProperty(final String property) {
		if(property.equals("album")) {
			return album;
		} else if(property.equals("artist")) {
			return artist;
		} else if(property.equals("comment")) {
			return comment;
		} else if(property.equals("genre")) {
			return genre;
		} else if(property.equals("songName")) {
			return songName;
		} else if(property.equals("track")) {
			return track;
		} else if(property.equals("year")) {
			return year;
		} else {
			return _dynamic.get(property);
		}
	}
}
