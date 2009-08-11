/**
 * 
 */
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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.joa_ebert.apparat.swf.io.SwfInputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class SwfInputStreamIntegerTest
{
	private static SwfInputStream swfInputStream;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		swfInputStream = new SwfInputStream( new ByteArrayInputStream(
				new byte[] {
						0x20, 0x71, 0x6e, -1
				} ) );
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		swfInputStream = null;
	}

	@Before
	public void setUpBefore() throws Exception
	{
		swfInputStream.reset();
	}

	@Test
	public void testRead() throws IOException
	{
		assertEquals( 0x20, swfInputStream.read() );
		assertEquals( 0x71, swfInputStream.read() );
		assertEquals( 0x6e, swfInputStream.read() );
		assertEquals( 0xff, swfInputStream.read() );

		assertEquals( 0, swfInputStream.available() );
	}

	@Test
	public void testSI08() throws Exception
	{
		assertEquals( 0x20, swfInputStream.readSI08() );
		assertEquals( 0x71, swfInputStream.readSI08() );
		assertEquals( 0x6e, swfInputStream.readSI08() );
		assertEquals( -1, swfInputStream.readSI08() );

		assertEquals( 0, swfInputStream.available() );
	}

	@Test
	public void testSI16() throws Exception
	{
		assertEquals( 0x7120, swfInputStream.readSI16() );
		assertEquals( -146, swfInputStream.readSI16() );

		assertEquals( 0, swfInputStream.available() );
	}

	@Test
	public void testSI32() throws Exception
	{
		assertEquals( -9539296, swfInputStream.readSI32() );
	}

	@Test
	public void testUI16() throws Exception
	{
		assertEquals( 0x7120, swfInputStream.readUI16() );
		assertEquals( 0xff6e, swfInputStream.readUI16() );

		assertEquals( 0, swfInputStream.available() );
	}

	@Test
	public void testUI32() throws Exception
	{
		assertEquals( 0xff6e7120, swfInputStream.readUI32() );

		assertEquals( 0, swfInputStream.available() );
	}

	@Test
	public void testUI8() throws Exception
	{
		assertEquals( 0x20, swfInputStream.readUI08() );
		assertEquals( 0x71, swfInputStream.readUI08() );
		assertEquals( 0x6e, swfInputStream.readUI08() );
		assertEquals( 0xff, swfInputStream.readUI08() );

		assertEquals( 0, swfInputStream.available() );
	}
}
