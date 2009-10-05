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

package com.joa_ebert.apparat.tests.taas;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.swc.Swc;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import com.joa_ebert.apparat.taas.compiler.TaasCompiler;
import com.joa_ebert.apparat.tests.FlashPlayerTest;
import com.joa_ebert.apparat.tools.io.TagIO;

/**
 * @author Joa Ebert
 * 
 */
public class TaasCompilerTests
{
	static private Swc playerGlobal;

	@AfterClass
	public static void aftetClass()
	{
		playerGlobal = null;
	}

	@BeforeClass
	public static void beforeClass() throws Exception
	{
		final File file = new File( "assets/playerglobal.swc" );

		Assert.assertTrue( file.exists() );

		playerGlobal = new Swc();
		playerGlobal.read( file );
	}

	public void compile( final File input ) throws Exception
	{
		Assert.assertTrue( input.exists() );

		if( input.getName().endsWith( ".abc" ) )
		{
			throw new UnsupportedOperationException();

			// final Abc abc = new Abc();
			//
			// abc.read( inputFile );
			//
			// TODO
		}
		else
		{
			final TagIO tagIO = new TagIO( input );
			final Map<Abc, DoABCTag> abcMap = new LinkedHashMap<Abc, DoABCTag>();

			tagIO.read();

			for( final ITag tag : tagIO.getTags() )
			{
				if( tag.getType() == Tags.DoABC )
				{
					final DoABCTag doABC = (DoABCTag)tag;

					final Abc abc = new Abc();

					abc.read( doABC );

					abcMap.put( abc, doABC );
				}
			}

			final TaasCompiler compiler = new TaasCompiler();

			compiler.addLibrary( playerGlobal );

			compiler.getAbcEnvironment().addAll( abcMap.keySet() );

			for( final Entry<Abc, DoABCTag> entry : abcMap.entrySet() )
			{
				final Abc abc = entry.getKey();

				abc.accept( compiler );
				abc.write( entry.getValue() );
			}

			final String name = input.getName();
			final String extension = name.substring( name.length() - 3, name
					.length() );
			final String newname = name.substring( 0, name.length() - 3 )
					+ "output." + extension;
			final File output = new File( input.getParentFile()
					.getAbsolutePath()
					+ File.separator + newname );

			tagIO.write( output );
			tagIO.close();

			final FlashPlayerTest playerTest = new FlashPlayerTest();

			playerTest.spawn( input, 1500 );
			// playerTest.printLog( System.out );
			playerTest.assertNoError();

			final String[] logBefore = playerTest.getLog();

			playerTest.spawn( output, 1500 );
			// playerTest.printLog( System.out );
			playerTest.assertNoError();

			final String[] logAfter = playerTest.getLog();

			Assert.assertArrayEquals( logBefore, logAfter );
		}
	}

	public void compile( final String input ) throws Exception
	{
		compile( new File( input ) );
	}

	@Test
	public void test00() throws Exception
	{
		compile( "assets/Test00.swf" );
	}

	@Test
	public void test01() throws Exception
	{
		compile( "assets/Test01.swf" );
	}

	@Test
	public void test02() throws Exception
	{
		compile( "assets/Test02.swf" );
	}

	@Test
	public void test03() throws Exception
	{
		compile( "assets/Test03.swf" );
	}

	@Test
	public void test04() throws Exception
	{
		compile( "assets/Test04.swf" );
	}

	@Test
	public void test05() throws Exception
	{
		compile( "assets/Test05.swf" );
	}

	@Test
	public void test06() throws Exception
	{
		compile( "assets/Test06.swf" );
	}

	@Test
	public void test07() throws Exception
	{
		compile( "assets/Test07.swf" );
	}

	@Test
	public void test08() throws Exception
	{
		compile( "assets/Test08.swf" );
	}

	@Test
	public void test09() throws Exception
	{
		compile( "assets/Test09.swf" );
	}

	@Test
	public void test10() throws Exception
	{
		compile( "assets/Test10.swf" );
	}

	@Test
	public void test11() throws Exception
	{
		compile( "assets/Test11.swf" );
	}

	@Test
	public void test12() throws Exception
	{
		compile( "assets/Test12.swf" );
	}

	@Test
	public void test13() throws Exception
	{
		compile( "assets/Test13.swf" );
	}

	@Test
	public void test14() throws Exception
	{
		compile( "assets/Test14.swf" );
	}

	@Test
	@Ignore
	public void testLorenz() throws Exception
	{
		compile( "assets/lorenz.swf" );
	}
}
