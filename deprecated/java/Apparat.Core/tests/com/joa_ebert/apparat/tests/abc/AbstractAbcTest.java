/*
 * This inputFile is part of Apparat.
 * 
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 * 
 */

package com.joa_ebert.apparat.tests.abc;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import com.joa_ebert.apparat.tests.FlashPlayerTest;
import com.joa_ebert.apparat.tools.io.TagIO;
import org.junit.Test;

import java.io.File;

/**
 * @author Joa Ebert
 */
public abstract class AbstractAbcTest {
	private File inputFile;
	private boolean doWriteCheck;

	protected abstract void compute(Abc abc);

	protected void input(final File file) {
		this.inputFile = file;
	}

	protected void input(final String file) {
		input(new File(file));
	}

	protected void output() {
		doWriteCheck = true;
	}

	@Test
	public void test() throws Exception {
		if (inputFile.getName().endsWith(".abc")) {
			final Abc abc = new Abc();

			abc.read(inputFile);

			compute(abc);
		} else {
			final TagIO tagIO = new TagIO(inputFile);

			tagIO.read();

			for (final ITag tag : tagIO.getTags()) {
				if (tag instanceof DoABCTag) {
					final DoABCTag doABC = (DoABCTag) tag;

					final Abc abc = new Abc();

					abc.read(doABC);

					compute(abc);

					abc.write(doABC);
				}
			}

			if (doWriteCheck) {
				final String name = inputFile.getName();
				final String extension = name.substring(name.length() - 3,
						name.length());
				final String newname = name.substring(0, name.length() - 3)
						+ "output." + extension;
				final File output = new File(inputFile.getParentFile()
						.getAbsolutePath()
						+ File.separator + newname);

				tagIO.write(output);

				final FlashPlayerTest playerTest = new FlashPlayerTest();

				playerTest.spawn(output, 5000);
				playerTest.assertNoError();
			}

			tagIO.close();
		}
	}
}
