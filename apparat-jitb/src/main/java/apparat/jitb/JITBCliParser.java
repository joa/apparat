package apparat.jitb;

import java.io.File;

/**
 * @author Joa Ebert
 */
public final class JITBCliParser {
	private final String arguments[];

	public JITBCliParser(final String arguments[]) {
		this.arguments = arguments;
	}

	public JITBConfiguration getConfiguration() throws JITBCliParserException {
		if(1 != arguments.length) {
			throw new JITBCliParserException("Expected only one argument.");
		}

		final String pathname = arguments[0];
		final File file = new File(pathname);

		if(!file.exists()) {
			throw new JITBCliParserException("File "+pathname+" does not exist.");
		}

		if(!file.isFile()) {
			throw new JITBCliParserException(pathname+" is not a file.");
		}

		if(!file.canRead()) {
			throw new JITBCliParserException("Cannot read from "+file+".");
		}

		return new JITBConfiguration() {
			@Override
			public File getFile() {
				return file;
			}
		};
	}
}
