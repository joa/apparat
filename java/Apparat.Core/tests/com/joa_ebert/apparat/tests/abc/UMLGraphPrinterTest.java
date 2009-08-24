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

import java.io.PrintWriter;

import org.junit.Test;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.utils.UMLGraphPrinter;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import com.joa_ebert.apparat.tools.io.TagIO;

/**
 * @author Joa Ebert
 * 
 */
public class UMLGraphPrinterTest
{
	private void test( final DoABCTag tag ) throws Exception
	{
		final Abc abc = new Abc();

		abc.read( tag );

		new UMLGraphPrinter( new PrintWriter( System.out ) ).print( abc );
	}

	@Test
	public void testReadWrite() throws Exception
	{
		final TagIO tagIO = new TagIO( "assets/InheritanceTest.swf" );

		tagIO.read();

		for( final ITag tag : tagIO.getTags() )
		{
			if( tag.getType() == Tags.DoABC )
			{
				test( (DoABCTag)tag );
			}
		}
	}
}
