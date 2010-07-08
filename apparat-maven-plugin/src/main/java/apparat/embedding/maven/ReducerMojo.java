package apparat.embedding.maven;

import apparat.tools.reducer.Reducer;
import apparat.tools.reducer.ReducerConfiguration;

import java.io.File;

/**
 * @author Joa Ebert
 * @phase package
 * @goal reducer
 * @threadSafe
 */
public final class ReducerMojo extends AbstractApparatMojo {
	/**
	 * The JPEG compression quality.
	 *
	 * @parameter default-value=0.96f
	 * @required
	 */
	private float quality;

	/**
	 * The strength of Flash Player's deblocking filter.
	 *
	 * @parameter default-value=1.0f
	 * @required
	 */
	private float deblock;

	@Override protected void processFile(final File file) {
		final Reducer.ReducerTool tool = new Reducer.ReducerTool();
		final ReducerConfiguration config = new ReducerConfiguration() {
			@Override public File input() { return file; }
			@Override public File output() { return file; }
			@Override public float quality() { return quality; }
			@Override public float deblock() { return deblock; }
		};

		tool.configure(config);
		tool.run();
	}
}
