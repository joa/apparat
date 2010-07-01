package apparat.embedding.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * @author Joa Ebert
 */
abstract class AbstractApparatMojo extends AbstractMojo {
	/**
	 * The Maven project.
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * Additional override for the target file if it is not the default artifact of your project.
	 * @parameter
	 */
	protected File overrideArtifact;

	@Override public void execute() throws MojoExecutionException, MojoFailureException {
		if(null == overrideArtifact) {
			final Artifact artifact = project.getArtifact();
			final String artifactType = artifact.getType();

			if(artifactType.equals("swc") || artifactType.equals("swf")) {
				try {
					processFile(artifact.getFile());
				} catch(final Throwable cause) {
					throw new MojoExecutionException("Apparat execution failed.", cause);
				}
			} else {
				getLog().debug("Skipped artifact since its type is "+artifactType+".");
			}
		} else {
			if(!overrideArtifact.exists()) {
				throw new MojoFailureException("File "+overrideArtifact+" does not exist.");
			}

			try {
				processFile(overrideArtifact);
			} catch(final Throwable cause) {
				throw new MojoExecutionException("Apparat execution failed.", cause);
			}
		}
	}

	abstract protected void processFile(final File file);
}
