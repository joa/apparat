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
import java.io.IOException;

import junit.framework.Assert;

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
public class SwfInputStreamBitTest
{
	private static SwfInputStream swfInputStream;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		swfInputStream = new SwfInputStream( new ByteArrayInputStream(
				new byte[] {
					0x78
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
	public void testSB() throws IOException
	{
		Assert.assertEquals( 15, swfInputStream.readUB( 5 ) );
	}
}
