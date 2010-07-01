package apparat.embedding.maven;

import apparat.tools.tdsi.TDSIConfiguration;
import apparat.tools.tdsi.TurboDieselSportInjection.TDSITool;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * @author Joa Ebert
 * @phase package
 * @goal tdsi
 */
public final class TDSIMojo extends AbstractMojo {
	private final class Configuration implements TDSIConfiguration {
		private final File _input;
		private final File _output;
		private final boolean _alchemyExpansion;
		private final boolean _macroExpansion;
		private final boolean _inlineExpansion;

		public Configuration(final File input, final File output, final boolean alchemyExpansion,
							 final boolean macroExpansion, final boolean inlineExpansion) {
			_input = input;
			_output = output;
			_alchemyExpansion = alchemyExpansion;
			_macroExpansion = macroExpansion;
			_inlineExpansion = inlineExpansion;
		}

		@Override
		public File input() {
			return _input;
		}

		@Override
		public File output() {
			return _output;
		}

		@Override
		public boolean alchemyExpansion() {
			return _alchemyExpansion;
		}

		@Override
		public boolean macroExpansion() {
			return _macroExpansion;
		}

		@Override
		public boolean inlineExpansion() {
			return _inlineExpansion;
		}
	}

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

	/**
	 * The Maven project.
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		for(final Artifact artifact : project.getAttachedArtifacts()) {
			final String artifactFileName = artifact.getFile().getName().toLowerCase();

			if(artifactFileName.endsWith("swc") || artifactFileName.endsWith("swf")) {
				final TDSITool tool = new TDSITool();
				final Configuration config = new Configuration(artifact.getFile(), artifact.getFile(),
						alchemyExpansion, macroExpansion, inlineExpansion);
				tool.configure(config);

				try {
					tool.run();
				} catch(final Throwable cause) {
					throw new MojoExecutionException("TurboDieselSportInjection failed.", cause);
				}
			}
		}
	}
}
