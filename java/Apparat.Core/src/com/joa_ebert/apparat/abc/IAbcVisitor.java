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

package com.joa_ebert.apparat.abc;

import com.joa_ebert.apparat.abc.multinames.Multiname;
import com.joa_ebert.apparat.abc.multinames.MultinameA;
import com.joa_ebert.apparat.abc.multinames.MultinameL;
import com.joa_ebert.apparat.abc.multinames.MultinameLA;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.abc.multinames.QNameA;
import com.joa_ebert.apparat.abc.multinames.RTQName;
import com.joa_ebert.apparat.abc.multinames.RTQNameA;
import com.joa_ebert.apparat.abc.multinames.RTQNameL;
import com.joa_ebert.apparat.abc.multinames.RTQNameLA;
import com.joa_ebert.apparat.abc.multinames.Typename;
import com.joa_ebert.apparat.abc.traits.TraitClass;
import com.joa_ebert.apparat.abc.traits.TraitConst;
import com.joa_ebert.apparat.abc.traits.TraitFunction;
import com.joa_ebert.apparat.abc.traits.TraitGetter;
import com.joa_ebert.apparat.abc.traits.TraitMethod;
import com.joa_ebert.apparat.abc.traits.TraitSetter;
import com.joa_ebert.apparat.abc.traits.TraitSlot;

/**
 * 
 * @author Joa Ebert
 * 
 */
public interface IAbcVisitor
{
	void visit( final AbcContext context, final Abc abc );

	void visit( final AbcContext context, final AbstractMultiname multiname );

	void visit( final AbcContext context, final AbstractTrait trait );

	void visit( final AbcContext context, final Class klass );

	void visit( final AbcContext context, final ConstantPool constantPool );

	void visit( final AbcContext context,
			final ExceptionHandler exceptionHandler );

	void visit( final AbcContext context, final Instance instance );

	void visit( final AbcContext context, final Metadata metadata );

	void visit( final AbcContext context, final Method method );

	void visit( final AbcContext context, final MethodBody methodBody );

	void visit( final AbcContext context, final Multiname multiname );

	void visit( final AbcContext context, final MultinameA multinameA );

	void visit( final AbcContext context, final MultinameL multinameL );

	void visit( final AbcContext context, final MultinameLA multinameLA );

	void visit( final AbcContext context, final Namespace namespace );

	void visit( final AbcContext context, final NamespaceSet namespaceSet );

	void visit( final AbcContext context, final Parameter parameter );

	void visit( final AbcContext context, final QName qName );

	void visit( final AbcContext context, final QNameA qNameA );

	void visit( final AbcContext context, final RTQName rtqName );

	void visit( final AbcContext context, final RTQNameA rtqNameA );

	void visit( final AbcContext context, final RTQNameL rtqNameL );

	void visit( final AbcContext context, final RTQNameLA rtqNameLA );

	void visit( final AbcContext context, final Script script );

	void visit( final AbcContext context, final TraitClass klass );

	void visit( final AbcContext context, final TraitConst konst );

	void visit( final AbcContext context, final TraitFunction function );

	void visit( final AbcContext context, final TraitGetter getter );

	void visit( final AbcContext context, final TraitMethod method );

	void visit( final AbcContext context, final TraitSetter setter );

	void visit( final AbcContext context, final TraitSlot slot );

	void visit( final AbcContext context, final Typename typename );
}
