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
	 * @parameter default-value=0.99f
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

	/**
	 * Whether or not to merge ABC files into a single one.
	 * @param default-value=false
	 * @required
	 */
	private boolean mergeABC;

	/**
	 * Whether or not to use LZMA compression. Only available with
	 * SWF files.
	 * @param default-value=false
	 * @required
	 */
	private boolean lzma;

	/**
	 * Which Matryoshka implementation to use. Either "quiet" or "preloader".
	 *
	 * @param default-value="quiet"
	 * @required
	 */
	private String matryoshka;

	@Override protected void processFile(final File file) {
		if(getLog().isDebugEnabled()) {
			getLog().debug("Running "+file+" through Reducer ...");
		}

		final Reducer.ReducerTool tool = new Reducer.ReducerTool();
		final ReducerConfiguration config = new ReducerConfiguration() {
			@Override public File input() { return file; }
			@Override public File output() { return file; }
			@Override public float quality() { return quality; }
			@Override public float deblock() { return deblock; }
			@Override public boolean mergeABC() { return mergeABC; }
		};

		tool.configure(config);
		tool.run();
	}
}
