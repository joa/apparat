/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.embedding.maven;

import apparat.tools.reducer.MatryoshkaType;
import apparat.tools.reducer.Reducer;
import apparat.tools.reducer.ReducerConfiguration;
import org.apache.maven.plugin.MojoFailureException;
import scala.None$;
import scala.Option;
import scala.Some;
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
	 * @parameter default-value="0.99f" expression="${apparat.reducer.quality}"
	 * @required
	 */
	private float quality;

	/**
	 * The strength of Flash Player's deblocking filter.
	 *
	 * @parameter default-value="1.0f" expression="${apparat.reducer.deblock}"
	 * @required
	 */
	private float deblock;

	/**
	 * Whether or not to merge ABC files into a single one.
	 * @parameter default-value="false" expression="${apparat.reducer.mergeABC}"
	 * @required
	 */
	private boolean mergeABC;

	/**
	 * Whether or not to sort the constant pool.
	 * Only if <code>mergeABC</code> is specified.
	 *
	 * @parameter default-value="false" expression="${apparat.reducer.sortCPool}"
	 * @required
	 */
	private boolean sortCPool;

	/**
	 * Whether or not to use LZMA compression. Only available with
	 * SWF files.
	 * @parameter default-value="false" expression="${apparat.reducer.lzma}"
	 * @required
	 */
	private boolean lzma;

	/**
	 * Which Matryoshka implementation to use. Either "quiet", "preloader" or "custom".
	 * When using "custom" you have to specify a custom Matryoshka via the <code>matryoshka</code>
	 * parameter.
	 *
	 * @parameter default-value="quiet" expression="${apparat.reducer.matryoshkaType}"
	 * @required
	 */
	private String matryoshkaType;

	/**
	 * A custom Matryoshka. Only used if the matryoshkaType is set to "custom".
	 *
	 * @parameter expression="${apparat.reducer.matryoshka}"
	 */
	private File matryoshka;

	/**
	 * Whether or not to merge control flow where possible.
	 *
	 * @parameter default-value="quiet" expression="${apparat.reducer.mergeCF}"
	 */
	private boolean mergeCF;

	@Override protected void processFile(final File file) throws MojoFailureException {
		if(getLog().isDebugEnabled()) {
			getLog().debug("Running "+file+" through Reducer ...");
		}

		if(!matryoshkaType.equalsIgnoreCase("quiet") &&
				!matryoshkaType.equalsIgnoreCase("preloader") && !matryoshkaType.equalsIgnoreCase("custom")) {
			throw new MojoFailureException("\""+matryoshka+"\" is an illegal argument for the <matryoshkaType> node.");
		}

		final Reducer.ReducerTool tool = new Reducer.ReducerTool();
		final ReducerConfiguration config = new ReducerConfiguration() {
			@Override public File input() { return file; }
			@Override public File output() { return file; }
			@Override public float quality() { return quality; }
			@Override public float deblock() { return deblock; }
			@Override public boolean mergeABC() { return mergeABC; }
			@Override public boolean sortCPool() { return sortCPool; }
			@Override public boolean lzma() { return lzma; }
			@Override public int matryoshkaType() {
				if(matryoshkaType.equalsIgnoreCase("quiet")) {
					return MatryoshkaType.QUIET();
				} else if(matryoshkaType.equalsIgnoreCase("preloader")) {
					return MatryoshkaType.PRELOADER();
				} else if(matryoshkaType.equalsIgnoreCase("custom")) {
					return MatryoshkaType.CUSTOM();
				} else {
					return MatryoshkaType.NONE();
				}
			}
			@Override public Option<File> matryoshka() { return (null == matryoshka) ?
					None$.MODULE$ : new Some(matryoshka); }
			@Override public boolean mergeCF() { return mergeCF; }
		};

		tool.configure(config);
		tool.run();
	}
}
