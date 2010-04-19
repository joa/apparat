/*
 * This file is part of Apparat.
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

package com.joa_ebert.apparat.swf.tags.control;

import com.joa_ebert.apparat.swf.SwfException;
import com.joa_ebert.apparat.swf.io.RECORDHEADER;
import com.joa_ebert.apparat.swf.io.SwfInputStream;
import com.joa_ebert.apparat.swf.io.SwfOutputStream;
import com.joa_ebert.apparat.swf.tags.Tags;

import java.io.IOException;

/**
 * @author Joa Ebert
 */
public class DoABC1Tag extends DoABCTag {

	@Override
	public int getType() {
		return Tags.DoABC1;
	}

	@Override
	public void read(final RECORDHEADER header, final SwfInputStream input)
			throws IOException, SwfException {
		final int abcLength = header.length;

		abcData = new byte[abcLength];

		int offset = 0;

		while (offset < abcLength) {
			offset += input.read(abcData, offset, abcLength - offset);
		}
	}

	@Override
	public String toString() {
		return "[DoABC1Tag dataLength: " + abcData.length + "]";
	}

	@Override
	public void write(final SwfOutputStream output) throws IOException {
		output.write(abcData);
	}
}