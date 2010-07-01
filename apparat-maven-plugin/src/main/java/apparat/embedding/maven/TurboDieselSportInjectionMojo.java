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
public final class TurboDieselSportInjectionMojo extends AbstractMojo {
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
		if(!alchemyExpansion && !macroExpansion && !inlineExpansion) {
			getLog().warn("TurboDieselSportInjection has been disabled since all its features are turned off.");
			return;
		}
		
		final Artifact artifact = project.getArtifact();
		final String artifactType = artifact.getType();

		if(artifactType.equals("swc") || artifactType.equals("swf")) {
			if(getLog().isDebugEnabled()) {
				getLog().debug("Running "+artifact.getFile()+"through TurboDieselSportInjection ...");
			}

			final TDSITool tool = new TDSITool();
			final TDSIConfiguration config = new TDSIConfiguration() {
				@Override public File input() { return artifact.getFile(); }
				@Override public File output() { return artifact.getFile(); }
				@Override public boolean alchemyExpansion() { return alchemyExpansion; }
				@Override public boolean macroExpansion() { return macroExpansion; }
				@Override public boolean inlineExpansion() { return inlineExpansion; }
			};

			try {
				tool.configure(config);
				tool.run();
			} catch(final Throwable cause) {
				throw new MojoExecutionException("TurboDieselSportInjection failed.", cause);
			}
		}
	}
}
