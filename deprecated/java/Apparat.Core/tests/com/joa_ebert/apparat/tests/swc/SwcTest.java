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

package com.joa_ebert.apparat.tests.swc;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.joa_ebert.apparat.swc.Swc;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class SwcTest
{
	@Test
	public void testRead() throws Exception
	{
		final Swc swc = new Swc();

		swc.read( new File( "assets/playerglobal.swc" ) );

		Assert.assertNotNull( swc.catalog );
		Assert.assertNotNull( swc.library );
	}

	@Test
	@Ignore
	public void testReadWrite() throws Exception
	{
		final Swc swc = new Swc();

		byte[] catalog;
		byte[] library;

		swc.read( new File( "assets/playerglobal.swc" ) );

		Assert.assertNotNull( swc.catalog );
		Assert.assertNotNull( swc.library );

		catalog = swc.catalog;
		library = swc.library;

		swc.write( new File( "assets/output.swc" ) );

		final Swc swc2 = new Swc();

		swc2.read( new File( "assets/output.swc" ) );

		Assert.assertEquals( catalog, swc2.catalog );
		Assert.assertEquals( library, swc2.library );
	}
}
