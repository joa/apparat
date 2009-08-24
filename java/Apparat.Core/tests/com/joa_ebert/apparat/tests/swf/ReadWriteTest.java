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

import org.junit.Assert;
import org.junit.Test;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.bytecode.analysis.DeadCodeElimination;
import com.joa_ebert.apparat.abc.bytecode.permutations.DecrementIntPattern;
import com.joa_ebert.apparat.abc.bytecode.permutations.IBytecodePermutation;
import com.joa_ebert.apparat.abc.bytecode.permutations.IntegerCalculus;
import com.joa_ebert.apparat.abc.bytecode.permutations.NamespaceSetCleanup;
import com.joa_ebert.apparat.abc.bytecode.permutations.PermutationChain;
import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class ReadWriteTest
{
	@Test
	public void testIO() throws Exception
	{
		final Swf swf0 = new Swf();
		final Swf swf1 = new Swf();

		swf0.read( "assets/Example04.swf" );

		swf0.accept( new ITagVisitor()
		{
			public void visit( final ITag tag )
			{
				if( tag instanceof DoABCTag )
				{
					final DoABCTag doABC = (DoABCTag)tag;
					final Abc abc = new Abc();
					ByteArrayOutputStream buffer;

					try
					{
						abc.read( new ByteArrayInputStream( doABC.abcData ) );
						abc.accept( new DeadCodeElimination( true ) );
						abc.accept( new PermutationChain(
								new IBytecodePermutation[] {
										new DecrementIntPattern(),
										new IntegerCalculus(),
										new NamespaceSetCleanup(),
								} ) );
						abc.write( buffer = new ByteArrayOutputStream(
								doABC.abcData.length ) );

						final byte[] newData = buffer.toByteArray();

						// DebugUtil.hexDump( doABC.abcData );
						// DebugUtil.hexDump( newData );

						// Assert.assertArrayEquals( doABC.abcData, newData );

						doABC.abcData = newData;
					}
					catch( final Exception e )
					{
						e.printStackTrace();
						Assert.fail( e.getMessage() );
					}
				}
			}
		} );

		swf0.write( "assets/output.swf" );

		swf1.read( "assets/output.swf" );

		Assert.assertEquals( swf0.frameCount, swf1.frameCount );
		Assert.assertEquals( swf0.frameRate, swf1.frameRate );
		Assert.assertTrue( swf0.frameSize.equals( swf1.frameSize ) );
		Assert.assertEquals( swf0.version, swf1.version );
		Assert.assertEquals( swf0.isCompressed, swf1.isCompressed );
	}
}
