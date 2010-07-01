package apparat.embedding.maven;

import apparat.tools.reducer.Reducer;
import apparat.tools.reducer.ReducerConfiguration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * @author Joa Ebert
 * @phase package
 * @goal reducer
 * @threadSafe
 */
public final class ReducerMojo extends AbstractMojo {
	/**
	 * The JPEG compression quality.
	 *
	 * @parameter default-value=0.96f
	 * @required
	 * @readonly
	 */
	private float quality;

	/**
	 * The strength of Flash Player's deblocking filter.
	 *
	 * @parameter default-value=1.0f
	 * @required
	 * @readonly
	 */
	private float deblock;

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
		final Artifact artifact = project.getArtifact();
		final String artifactType = artifact.getType();

		if(artifactType.equals("swc") || artifactType.equals("swf")) {
			if(getLog().isDebugEnabled()) {
				getLog().debug("Running "+artifact.getFile()+" through Reducer ...");
			}

			final Reducer.ReducerTool tool = new Reducer.ReducerTool();
			final ReducerConfiguration config = new ReducerConfiguration() {
				@Override public File input() { return artifact.getFile(); }
				@Override public File output() { return artifact.getFile(); }
				@Override public float quality() { return quality; }
				@Override public float deblock() { return deblock; }
			};

			try {
				tool.configure(config);
				tool.run();
			} catch(final Throwable cause) {
				throw new MojoExecutionException("Apparat Reducer failed.", cause);
			}
		}
	}
}
