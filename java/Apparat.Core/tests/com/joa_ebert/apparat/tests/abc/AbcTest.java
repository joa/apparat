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

import com.joa_ebert.apparat.abc.*;
import com.joa_ebert.apparat.abc.Class;
import com.joa_ebert.apparat.abc.io.AbcInputStream;
import com.joa_ebert.apparat.abc.multinames.*;
import com.joa_ebert.apparat.abc.traits.*;
import com.joa_ebert.apparat.swf.Swf;
import com.joa_ebert.apparat.swf.tags.ITag;
import com.joa_ebert.apparat.swf.tags.ITagVisitor;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;
import org.junit.*;

import java.io.ByteArrayInputStream;

/**
 * @author Joa Ebert
 */
public class AbcTest {
	private static byte[] abcData;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final Swf swf = new Swf();

		swf.read("assets/640_480.swf");

		final ITagVisitor visitor = new ITagVisitor() {
			public void visit(final ITag tag) {
				if (tag instanceof DoABCTag) {
					abcData = ((DoABCTag) tag).abcData;
				}
			}
		};

		swf.accept(visitor);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		abcData = null;
	}

	private AbcInputStream input;

	@Before
	public void setUp() throws Exception {
		input = new AbcInputStream(new ByteArrayInputStream(abcData));
	}

	@After
	public void tearDown() throws Exception {
		input.close();

		input = null;
	}

	@Test
	public void testRead() throws Exception {
		final Abc abc = new Abc();

		abc.read(input);
		abc.accept(new IAbcVisitor() {
			public void visit(final AbcContext context, final Abc abc) {
			}

			public void visit(final AbcContext context,
							  final AbstractMultiname multiname) {
			}

			public void visit(final AbcContext context,
							  final AbstractTrait trait) {
			}

			public void visit(final AbcContext context, final Class klass) {
			}

			public void visit(final AbcContext context,
							  final ConstantPool constantPool) {
			}

			public void visit(final AbcContext context,
							  final ExceptionHandler exceptionHandler) {
			}

			public void visit(final AbcContext context, final Instance instance) {
			}

			public void visit(final AbcContext context, final Metadata metadata) {
			}

			public void visit(final AbcContext context, final Method method) {
				// System.out.println( method.name );
			}

			public void visit(final AbcContext context,
							  final MethodBody methodBody) {
				// final BytecodeDecoder decoder = new BytecodeDecoder(
				// methodBody );
				//
				// try
				// {
				// final OperationList ops = decoder.decode( context );
				//
				// for( final AbstractOperation op : ops )
				// {
				// System.out.println( Op.opToString( op.getCode() ) );
				// }
				// }
				// catch( final IOException e )
				// {
				// e.printStackTrace();
				// Assert.fail( e.getMessage() );
				// }
			}

			public void visit(final AbcContext context,
							  final Multiname multiname) {
			}

			public void visit(final AbcContext context,
							  final MultinameA multinameA) {
			}

			public void visit(final AbcContext context,
							  final MultinameL multinameL) {
			}

			public void visit(final AbcContext context,
							  final MultinameLA multinameLA) {
			}

			public void visit(final AbcContext context,
							  final Namespace namespace) {
			}

			public void visit(final AbcContext context,
							  final NamespaceSet namespaceSet) {
			}

			public void visit(final AbcContext context,
							  final Parameter parameter) {
			}

			public void visit(final AbcContext context, final QName name) {
			}

			public void visit(final AbcContext context, final QNameA nameA) {
			}

			public void visit(final AbcContext context, final RTQName rtqName) {
			}

			public void visit(final AbcContext context, final RTQNameA rtqNameA) {
			}

			public void visit(final AbcContext context, final RTQNameL rtqNameL) {
			}

			public void visit(final AbcContext context,
							  final RTQNameLA rtqNameLA) {
			}

			public void visit(final AbcContext context, final Script script) {
			}

			public void visit(final AbcContext context, final TraitClass klass) {
			}

			public void visit(final AbcContext context, final TraitConst konst) {
			}

			public void visit(final AbcContext context,
							  final TraitFunction function) {
			}

			public void visit(final AbcContext context,
							  final TraitGetter getter) {
			}

			public void visit(final AbcContext context,
							  final TraitMethod method) {
			}

			public void visit(final AbcContext context,
							  final TraitSetter setter) {
			}

			public void visit(final AbcContext context, final TraitSlot slot) {
			}

			public void visit(final AbcContext context, final Typename typename) {
			}
		});

	}
}
