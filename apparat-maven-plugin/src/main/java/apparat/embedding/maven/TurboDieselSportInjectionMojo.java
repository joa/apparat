package apparat.embedding.maven;

import apparat.tools.tdsi.TDSIConfiguration;
import apparat.tools.tdsi.TurboDieselSportInjection;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * @author Joa Ebert
 * @phase package
 * @goal tdsi
 * @threadSafe
 */
public final class TurboDieselSportInjectionMojo extends AbstractApparatMojo {
	/**
	 * Whether or not to expand Alchemy instructions.
	 *
	 * @parameter default-value=true
	 * @required
	 * @readonly
	 */
	private boolean alchemyExpansion;

	/**
	 * Whether or not to perform macro expansion.
	 *
	 * @parameter default-value=true
	 * @required
	 * @readonly
	 */
	private boolean macroExpansion;

	/**
	 * Whether or not to perform macro expansion.
	 *
	 * @parameter default-value=true
	 * @required
	 * @readonly
	 */
	private boolean inlineExpansion;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(!alchemyExpansion && !macroExpansion && !inlineExpansion) {
			getLog().warn("TurboDieselSportInjection has been disabled since all its features are turned off.");
			return;
		}

		super.execute();
	}

	@Override
	protected void processFile(final File file) {
		if(getLog().isDebugEnabled()) {
			getLog().debug("Running "+file+" through TurboDieselSportInjection ...");
		}

		final TurboDieselSportInjection.TDSITool tool = new TurboDieselSportInjection.TDSITool();
		final TDSIConfiguration config = new TDSIConfiguration() {
			@Override public File input() { return file; }
			@Override public File output() { return file;}
			@Override public boolean alchemyExpansion() { return alchemyExpansion; }
			@Override public boolean macroExpansion() { return macroExpansion; }
			@Override public boolean inlineExpansion() { return inlineExpansion; }
		};

		tool.configure(config);
		tool.run();
	}
}
