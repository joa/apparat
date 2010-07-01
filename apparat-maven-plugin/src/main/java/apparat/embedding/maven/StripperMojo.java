package apparat.embedding.maven;

import apparat.tools.stripper.Stripper;
import apparat.tools.stripper.StripperConfiguration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * @author Joa Ebert
 * @phase package
 * @goal stripper
 * @threadSafe
 */
public final class StripperMojo extends AbstractMojo {
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
				getLog().debug("Running "+artifact.getFile()+" through Stripper ...");
			}

			final Stripper.StripperTool tool = new Stripper.StripperTool();
			final StripperConfiguration config = new StripperConfiguration() {
				@Override public File input() { return artifact.getFile(); }
				@Override public File output() { return artifact.getFile(); }
			};

			try {
				tool.configure(config);
				tool.run();
			} catch(final Throwable cause) {
				throw new MojoExecutionException("Apparat Stripper failed.", cause);
			}
		}
	}
}
