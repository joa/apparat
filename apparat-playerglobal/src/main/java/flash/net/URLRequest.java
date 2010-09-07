package flash.net;

/**
 * @author Joa Ebert
 */
public final class URLRequest extends jitb.lang.Object {
	private String _url;

	public URLRequest() { this(null); }
	public URLRequest(final String url) {
		_url = url;
	}

	public String url() { return _url; }
	public void url(final String value) { _url = value; }
}
