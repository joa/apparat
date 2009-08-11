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

package com.joa_ebert.apparat.tests.swf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.joa_ebert.apparat.swf.io.SwfInputStream;
import com.joa_ebert.apparat.swf.io.SwfOutputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class SwfOutputStreamRealTest
{
	ByteArrayOutputStream buffer;
	private SwfOutputStream output;

	@Before
	public void setUp() throws Exception
	{
		buffer = new ByteArrayOutputStream();
		output = new SwfOutputStream( buffer );
	}

	@After
	public void tearDown() throws Exception
	{
		output.close();
	}

	@Test
	public void testFIXED() throws IOException
	{
		output.writeFIXED( 12.34f );
		output.flush();

		final SwfInputStream input = new SwfInputStream(
				new ByteArrayInputStream( buffer.toByteArray() ) );

		Assert.assertEquals( 12.34f, input.readFIXED(), 0.0001f );
	}

	@Test
	public void testFIXED8() throws IOException
	{
		output.writeFIXED8( 12.3f );
		output.flush();

		final SwfInputStream input = new SwfInputStream(
				new ByteArrayInputStream( buffer.toByteArray() ) );

		Assert.assertEquals( 12.3f, input.readFIXED8(), 0.01f );
	}
}
