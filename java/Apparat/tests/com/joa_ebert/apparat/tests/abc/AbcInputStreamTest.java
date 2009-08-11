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

package com.joa_ebert.apparat.tests.abc;

import java.io.ByteArrayInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.joa_ebert.apparat.abc.io.AbcInputStream;
import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class AbcInputStreamTest
{

	private static byte[] abcData;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		final Swf swf = new Swf();

		swf.read( "assets/640_480.swf" );

		final ITagVisitor visitor = new ITagVisitor()
		{
			public void visit( final ITag tag )
			{
				if( tag.getType() == Tags.DoABC )
				{
					abcData = ( (DoABCTag)tag ).abcData;
				}
			}
		};

		swf.accept( visitor );
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		abcData = null;
	}

	private AbcInputStream input;

	@Before
	public void setUp() throws Exception
	{
		input = new AbcInputStream( new ByteArrayInputStream( abcData ) );
	}

	@After
	public void tearDown() throws Exception
	{
		input.close();

		input = null;
	}

	@Test
	public void testRead() throws Exception
	{
		Assert.assertEquals( 16, input.readU16() );
		Assert.assertEquals( 46, input.readU16() );
	}
}
