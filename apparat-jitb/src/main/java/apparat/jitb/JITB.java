package apparat.jitb;

import apparat.swf.Swf;

/**
 * @author Joa Ebert
 */
public final class JITB {
	public static void main(String arguments[]) {
		JITBConfiguration configuration;
		
		try {
			final JITBCliParser parser = new JITBCliParser(arguments);
			configuration = parser.getConfiguration();
		} catch(final JITBCliParserException exception) {
			System.out.println("Error: "+exception.getLocalizedMessage());
			return;
		}

		final JITB jitb = new JITB(configuration);
		jitb.run();
	}

	private final JITBConfiguration configuration;

	public JITB(final JITBConfiguration configuration) {
		this.configuration = configuration;
	}

	public void run() {
		final Swf swf = Swf.fromFile(configuration.getFile());
		//TODO
	}
}
