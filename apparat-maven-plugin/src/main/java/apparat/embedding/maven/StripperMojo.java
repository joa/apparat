package apparat.embedding.maven;

import apparat.tools.stripper.Stripper;
import apparat.tools.stripper.StripperConfiguration;

import java.io.File;

/**
 * @author Joa Ebert
 * @phase package
 * @goal stripper
 * @threadSafe
 */
public final class StripperMojo extends AbstractApparatMojo {
	@Override protected void processFile(final File file) {
		if(getLog().isDebugEnabled()) {
			getLog().debug("Running "+file+" through Stripper ...");
		}

		final Stripper.StripperTool tool = new Stripper.StripperTool();
		final StripperConfiguration config = new StripperConfiguration() {
			@Override public File input() { return file; }
			@Override public File output() { return file; }
		};

		tool.configure(config);
		tool.run();
	}
}
