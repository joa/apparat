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
