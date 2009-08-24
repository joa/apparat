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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.SwfFormatException;
import com.joa_ebert.apparat.swf.io.SwfInputStream;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class HeaderTest
{
	private static SwfInputStream inputStream;
	private static Swf swf;

	@Before
	public void setUp() throws Exception
	{
		try
		{
			inputStream = new SwfInputStream( new FileInputStream(
					"assets/640_480.swf" ) );
		}
		catch( final FileNotFoundException e )
		{
			fail( e.getMessage() );
		}

		swf = new Swf();
	}

	@After
	public void tearDown() throws Exception
	{
		inputStream.close();
		inputStream = null;

		swf = null;
	}

	@Test
	public void testRead() throws IOException, DataFormatException,
			SwfFormatException
	{
		swf.read( inputStream, new File( "assets/640_480.swf" ).length() );

		Assert.assertTrue( swf.isCompressed );
		Assert.assertEquals( 10, swf.version );
		Assert.assertEquals( 0.0, swf.frameSize.minX / 20.0 );
		Assert.assertEquals( 640.0, swf.frameSize.maxX / 20.0 );
		Assert.assertEquals( 0.0, swf.frameSize.minY / 20.0 );
		Assert.assertEquals( 480.0, swf.frameSize.maxY / 20.0 );
		Assert.assertEquals( 32.0f, swf.frameRate, 0.0f );
		Assert.assertEquals( 1, swf.frameCount );
	}
}
