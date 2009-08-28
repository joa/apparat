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

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.swc.Swc;
import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.Tags;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import com.joa_ebert.apparat.taas.TaasCompiler;
import com.joa_ebert.apparat.tests.FlashPlayerTest;

/**
 * @author Joa Ebert
 * 
 */
public class TaasCompilerTest
{
	@Test
	public void testTaasCompiler() throws Exception
	{
		final Map<Abc, DoABCTag> abcs = new LinkedHashMap<Abc, DoABCTag>();
		final Abc builtin = new Abc();
		final Abc toplevel = new Abc();
		final Swc playerGlobalSwc = new Swc();
		final Swf playerGlobalSwf = new Swf();
		final Swf test = new Swf();

		builtin.read( "assets/builtin.abc" );
		toplevel.read( "assets/toplevel.abc" );
		playerGlobalSwc.read( "assets/playerglobal.swc" );
		playerGlobalSwf.read(
				new ByteArrayInputStream( playerGlobalSwc.library ),
				playerGlobalSwc.library.length );
		test.read( "assets/Test2.swf" );

		final List<Abc> playerglobal = new LinkedList<Abc>();

		for( final ITag tag : playerGlobalSwf.tags )
		{
			if( tag.getType() == Tags.DoABC )
			{
				final Abc temp = new Abc();

				temp
						.read( new ByteArrayInputStream(
								( (DoABCTag)tag ).abcData ) );

				playerglobal.add( temp );
			}
		}

		Assert.assertFalse( playerglobal.isEmpty() );

		for( final ITag tag : test.tags )
		{
			if( tag.getType() == Tags.DoABC )
			{
				final DoABCTag doAbc = (DoABCTag)tag;
				final Abc temp = new Abc();

				temp.read( doAbc );

				abcs.put( temp, doAbc );

				break;
			}
		}

		final AbcEnvironment env = new AbcEnvironment( new Abc[] {
				builtin, toplevel
		} );

		env.addAll( playerglobal );
		env.addAll( abcs.keySet() );

		final TaasCompiler compiler = new TaasCompiler( env );

		for( final Abc abc : abcs.keySet() )
		{
			abc.accept( compiler );
		}

		for( final Entry<Abc, DoABCTag> entry : abcs.entrySet() )
		{
			entry.getKey().write( entry.getValue() );
		}

		test.write( "assets/compiled.swf" );

		final FlashPlayerTest playerTest = new FlashPlayerTest();

		playerTest.spawn( "assets/compiled.swf", 1000 );
		playerTest.printLog( System.out );
		playerTest.assertNoError();
	}

}
