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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;

import com.joa_ebert.apparat.abc.analysis.AbcBindingBuilder;
import com.joa_ebert.apparat.abc.analysis.AbcBindingSolver;
import com.joa_ebert.apparat.abc.analysis.ConstantPoolBuilder;
import com.joa_ebert.apparat.abc.analysis.MetadataBuilder;
import com.joa_ebert.apparat.abc.bytecode.BytecodeDecoder;
import com.joa_ebert.apparat.abc.bytecode.BytecodeEncoder;
import com.joa_ebert.apparat.abc.bytecode.MarkerException;
import com.joa_ebert.apparat.abc.bytecode.MarkerManager;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodePrinter;
import com.joa_ebert.apparat.abc.io.AbcInputStream;
import com.joa_ebert.apparat.abc.io.AbcOutputStream;
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
import com.joa_ebert.apparat.abc.utils.AbcPrinter;
import com.joa_ebert.apparat.swf.tags.control.DoABCTag;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class Abc
{
	public static final int SUPPORTED_MAJOR = 46;
	public static final int SUPPORTED_MINOR = 16;

	public int minorVersion;
	public int majorVersion;

	public ConstantPool constantPool;

	public List<Method> methods;
	public List<Metadata> metadata;
	public List<Instance> instances;
	public List<Class> classes;
	public List<Script> scripts;

	private BytecodeDecoder bytecodeDecoder;
	private BytecodeEncoder bytecodeEncoder;

	public void accept( final IAbcVisitor visitor )
	{
		final AbcContext context = new AbcContext( this );

		visitor.visit( context, this );

		if( null != constantPool )
		{
			constantPool.accept( context, visitor );
		}

		if( null != methods )
		{
			for( final Method method : methods )
			{
				method.accept( context, visitor );
			}
		}

		if( null != metadata )
		{
			for( final Metadata meta : metadata )
			{
				meta.accept( context, visitor );
			}
		}

		if( null != instances )
		{
			for( final Instance instance : instances )
			{
				instance.accept( context, visitor );
			}
		}

		if( null != classes )
		{
			for( final Class klass : classes )
			{
				klass.accept( context, visitor );
			}
		}

		if( null != scripts )
		{
			for( final Script script : scripts )
			{
				script.accept( context, visitor );
			}
		}
	}

	public void accept( final IMethodVisitor visitor )
	{
		final AbcContext context = new AbcContext( this );

		if( null != methods )
		{
			for( final Method method : methods )
			{
				method.accept( context, visitor );
			}
		}
	}

	public void debug( final OutputStream output )
	{
		PrintWriter writer = null;

		try
		{
			writer = new PrintWriter( output );
			debug( writer );
		}
		finally
		{
			if( null != writer )
			{
				writer.flush();
				writer.close();
			}
		}
	}

	public void debug( final PrintWriter output )
	{
		new AbcPrinter( output ).print( this );
	}

	public Class getClass( final int index )
	{
		return classes.get( index );
	}

	public int getIndex( final Class value )
	{
		if( null == value )
		{
			return 0;
		}

		final int index = classes.indexOf( value );

		if( -1 == index )
		{
			classes.add( value );

			return classes.size() - 1;
		}

		return index;
	}

	public int getIndex( final Metadata value )
	{
		if( null == value )
		{
			return 0;
		}

		final int n = metadata.size();

		for( int i = 0; i < n; ++i )
		{
			if( metadata.get( i ).equals( value ) )
			{
				return i;
			}
		}

		metadata.add( value );

		return n;
	}

	public int getIndex( final Method value )
	{
		if( null == value )
		{
			return 0;
		}

		final int index = methods.indexOf( value );

		if( -1 == index )
		{
			methods.add( value );

			return methods.size() - 1;
		}

		return index;
	}

	public Metadata getMetadata( final int index )
	{
		return metadata.get( index );
	}

	public Method getMethod( final int index )
	{
		return methods.get( index );
	}

	public void read( final byte[] data ) throws IOException, AbcException
	{
		ByteArrayInputStream input = null;
		try
		{
			input = new ByteArrayInputStream( data );
			read( input );
		}
		finally
		{
			if( null != input )
			{
				try
				{
					input.close();
				}
				catch( final IOException exception )
				{
				}
			}
		}
	}

	public void read( final DoABCTag doABC ) throws IOException, AbcException
	{
		read( doABC.abcData );
	}

	public void read( final File file ) throws FileNotFoundException,
			IOException, DataFormatException, AbcException
	{
		final InputStream input = new FileInputStream( file );

		read( input );

		input.close();
	}

	public void read( final InputStream input ) throws IOException,
			AbcException
	{
		final AbcInputStream abcInput = ( input instanceof AbcInputStream ) ? (AbcInputStream)input
				: new AbcInputStream( input );

		minorVersion = abcInput.readU16();
		majorVersion = abcInput.readU16();

		if( majorVersion != SUPPORTED_MAJOR )
		{
			throw new AbcException( "Unsupported major version "
					+ Integer.toString( majorVersion ) );
		}

		if( minorVersion != SUPPORTED_MINOR )
		{
			throw new AbcException( "Unsupported minor version "
					+ Integer.toString( minorVersion ) );
		}

		readConstantPool( abcInput );

		bytecodeDecoder = new BytecodeDecoder( constantPool );
		bytecodeEncoder = new BytecodeEncoder( constantPool );

		readMethods( abcInput );
		readMetadata( abcInput );
		readInstances( abcInput );
		readClasses( abcInput );
		readScripts( abcInput );
		readMethodBodies( abcInput );

		accept( new AbcBindingSolver() );
	}

	public void read( final String pathname ) throws FileNotFoundException,
			IOException, DataFormatException, AbcException
	{
		read( new File( pathname ) );
	}

	private AbstractMultiname readAbstractMultiname( final AbcInputStream input )
			throws AbcException, IOException
	{
		final MultinameKind kind = MultinameKind.getKind( input.readU08() );

		AbstractMultiname result;

		switch( kind )
		{
			case QName:
				result = readQName( input );
				break;
			case QNameA:
				result = readQNameA( input );
				break;
			case RTQName:
				result = readRTQName( input );
				break;
			case RTQNameA:
				result = readRTQNameA( input );
				break;
			case RTQNameL:
				result = new RTQNameL();
				break;
			case RTQNameLA:
				result = new RTQNameLA();
				break;
			case Multiname:
				result = readMultiname( input );
				break;
			case MultinameA:
				result = readMultinameA( input );
				break;
			case MultinameL:
				result = readMultinameL( input );
				break;
			case MultinameLA:
				result = readMultinameLA( input );
				break;
			case Typename:
				result = readTypename( input );
				break;

			default:
				throw new AbcException();
		}

		return result;
	}

	private void readClasses( final AbcInputStream input ) throws IOException,
			AbcException
	{
		final int classCount = instances.size();

		classes = new ArrayList<Class>( classCount );

		for( int i = 0; i < classCount; ++i )
		{
			final Class klass = new Class();
			final Instance instance = instances.get( i );

			instance.klass = klass;

			klass.abc = this;
			klass.instance = instance;
			klass.classInitializerIndex = input.readU30();
			klass.traits = readTraits( input );

			classes.add( klass );
		}
	}

	private void readConstantPool( final AbcInputStream input )
			throws IOException, AbcException
	{
		constantPool = new ConstantPool();

		constantPool.abc = this;

		readIntegers( input );
		readUnsignedIntegers( input );
		readDoubles( input );
		readStrings( input );
		readNamespaces( input );
		readNamespaceSets( input );
		readMultinames( input );
	}

	private void readDoubles( final AbcInputStream input ) throws IOException
	{
		final int doubleCount = input.readU30();

		constantPool.doubleTable = new ArrayList<Double>( doubleCount );
		constantPool.doubleTable.add( Double.NaN );

		for( int i = 1; i < doubleCount; ++i )
		{
			constantPool.doubleTable.add( input.readD64() );
		}
	}

	private List<ExceptionHandler> readExceptions( final MethodBody body,
			final AbcInputStream input ) throws IOException, MarkerException
	{
		final int exceptionCount = input.readU30();
		final List<ExceptionHandler> result = new ArrayList<ExceptionHandler>(
				exceptionCount );

		final MarkerManager markers = body.code.markers;

		for( int i = 0; i < exceptionCount; ++i )
		{
			final ExceptionHandler exceptionHandler = new ExceptionHandler();

			exceptionHandler.from = markers.putMarkerAt( input.readU30() );
			exceptionHandler.to = markers.putMarkerAt( input.readU30() );
			exceptionHandler.target = markers.putMarkerAt( input.readU30() );
			exceptionHandler.exceptionType = constantPool.getMultiname( input
					.readU30() );
			exceptionHandler.variableName = constantPool.getMultiname( input
					.readU30() );

			result.add( exceptionHandler );
		}

		markers.solve();

		return result;
	}

	private void readInstances( final AbcInputStream input )
			throws IOException, AbcException
	{
		final int instanceCount = input.readU30();

		instances = new ArrayList<Instance>( instanceCount );

		for( int i = 0; i < instanceCount; ++i )
		{
			final Instance instance = new Instance();

			instance.abc = this;

			final int nameIndex = input.readU30();

			if( 0 == nameIndex )
			{
				throw new AbcException( "Instance name index must not be zero." );
			}

			final AbstractMultiname name = constantPool
					.getMultiname( nameIndex );

			if( name.kind != MultinameKind.QName )
			{
				throw new AbcException( "Instance name has to be a QName." );
			}

			instance.name = (QName)name;

			final int baseNameIndex = input.readU30();

			if( 0 != baseNameIndex )
			{
				instance.base = constantPool.getMultiname( baseNameIndex );
			}

			final int flags = input.readU08();

			instance.isSealed = 0 != ( flags & 0x01 );
			instance.isFinal = 0 != ( flags & 0x02 );
			instance.isInterface = 0 != ( flags & 0x04 );

			if( 0 != ( flags & 0x08 ) )
			{
				instance.protectedNamespace = constantPool.getNamespace( input
						.readU30() );
			}

			final int interfaceCount = input.readU30();

			instance.interfaces = new ArrayList<AbstractMultiname>(
					interfaceCount );

			for( int j = 0; j < interfaceCount; ++j )
			{
				final int interfaceIndex = input.readU30();

				if( 0 == interfaceIndex )
				{
					throw new AbcException( "Interface index must not be zero." );
				}

				instance.interfaces.add( constantPool
						.getMultiname( interfaceIndex ) );
			}

			instance.instanceInitializerIndex = input.readU30();

			instance.traits = readTraits( input );

			instances.add( instance );
		}
	}

	private void readIntegers( final AbcInputStream input ) throws IOException
	{
		final int intCount = input.readU30();

		constantPool.intTable = new ArrayList<Integer>( intCount );
		constantPool.intTable.add( 0 );

		for( int i = 1; i < intCount; ++i )
		{
			constantPool.intTable.add( input.readS32() );
		}
	}

	private void readMetadata( final AbcInputStream input ) throws IOException
	{
		final int metaCount = input.readU30();

		metadata = new ArrayList<Metadata>( metaCount );

		for( int i = 0; i < metaCount; ++i )
		{
			final Metadata meta = new Metadata();

			meta.name = constantPool.getString( input.readU30() );

			final int itemCount = input.readU30();

			//
			// Undocumented: The documentation states that metadata is stored
			// stored using key-value pairs (page 27).
			//
			// Actually all keys are stored and then afterwards the values.
			//
			// key0
			// key1
			// key2
			// value0
			// value1
			// value2
			//

			final ArrayList<String> keys = new ArrayList<String>( itemCount );

			for( int j = 0; j < itemCount; ++j )
			{
				keys.add( constantPool.getString( input.readU30() ) );
			}

			for( int j = 0; j < itemCount; ++j )
			{
				meta.attributes.put( keys.get( j ), constantPool
						.getString( input.readU30() ) );
			}

			metadata.add( meta );
		}
	}

	private void readMethodBodies( final AbcInputStream input )
			throws IOException, AbcException
	{
		final int methodBodyCount = input.readU30();

		for( int i = 0; i < methodBodyCount; ++i )
		{
			final MethodBody methodBody = new MethodBody();
			final Method method = methods.get( input.readU30() );

			method.body = methodBody;

			methodBody.method = method;
			methodBody.maxStack = input.readU30();
			methodBody.localCount = input.readU30();
			methodBody.initScopeDepth = input.readU30();
			methodBody.maxScopeDepth = input.readU30();

			final int codeLength = input.readU30();

			final byte[] buffer = new byte[ codeLength ];

			int offset = 0;

			while( offset < codeLength )
			{
				offset += input.read( buffer, offset, codeLength - offset );
			}

			methodBody.code = bytecodeDecoder.decode( buffer );
			methodBody.code.abc = this;
			methodBody.code.method = method;
			methodBody.code.methodBody = methodBody;
			methodBody.exceptions = readExceptions( methodBody, input );
			methodBody.traits = readTraits( input );
		}
	}

	private void readMethods( final AbcInputStream input ) throws IOException,
			AbcException
	{
		final int methodCount = input.readU30();

		methods = new ArrayList<Method>( methodCount );

		for( int i = 0; i < methodCount; ++i )
		{
			final Method method = new Method();

			method.abc = this;

			final int paramCount = input.readU30();

			method.returnType = constantPool.getMultiname( input.readU30() );
			method.parameters = new ArrayList<Parameter>( paramCount );

			for( int j = 0; j < paramCount; ++j )
			{
				final Parameter parameter = new Parameter();

				parameter.type = constantPool.getMultiname( input.readU30() );

				method.parameters.add( parameter );
			}

			method.name = constantPool.getString( input.readU30() );

			final int flags = input.readU08();

			method.needsArguments = 0 != ( flags & 0x01 );
			method.needsActivation = 0 != ( flags & 0x02 );
			method.needsRest = 0 != ( flags & 0x04 );
			method.hasOptionalParameters = 0 != ( flags & 0x08 );
			method.setsDXNS = 0 != ( flags & 0x40 );
			method.hasParameterNames = 0 != ( flags & 0x80 );

			if( method.hasOptionalParameters )
			{
				final int optionalCount = input.readU30();

				if( optionalCount > paramCount )
				{
					throw new AbcException(
							"Optional parameter count is greater than formal parameter count." );
				}

				for( int j = paramCount - optionalCount; j < paramCount; ++j )
				{
					final Parameter parameter = method.parameters.get( j );
					final int valueIndex = input.readU30();

					parameter.isOptional = true;
					parameter.optionalType = ConstantType.getKind( input
							.readU08() );

					parameter.optionalValue = constantPool.getConstantValue(
							parameter.optionalType, valueIndex );
				}
			}

			if( method.hasParameterNames )
			{
				for( int j = 0; j < paramCount; ++j )
				{
					method.parameters.get( j ).name = constantPool
							.getString( input.readU30() );
				}
			}

			methods.add( method );
		}
	}

	private Multiname readMultiname( final AbcInputStream input )
			throws IOException
	{
		final Multiname result = new Multiname();

		result.name = constantPool.getString( input.readU30() );
		result.namespaceSet = constantPool.getNamespaceSet( input.readU30() );

		return result;
	}

	private Multiname readMultinameA( final AbcInputStream input )
			throws IOException
	{
		final MultinameA result = new MultinameA();

		result.name = constantPool.getString( input.readU30() );
		result.namespaceSet = constantPool.getNamespaceSet( input.readU30() );

		return result;
	}

	private MultinameL readMultinameL( final AbcInputStream input )
			throws IOException
	{
		final MultinameL result = new MultinameL();

		result.namespaceSet = constantPool.getNamespaceSet( input.readU30() );

		return result;
	}

	private MultinameLA readMultinameLA( final AbcInputStream input )
			throws IOException
	{
		final MultinameLA result = new MultinameLA();

		result.namespaceSet = constantPool.getNamespaceSet( input.readU30() );

		return result;
	}

	private void readMultinames( final AbcInputStream input )
			throws IOException, AbcException
	{
		final int multinameCount = input.readU30();

		constantPool.multinameTable = new ArrayList<AbstractMultiname>(
				multinameCount );
		constantPool.multinameTable.add( ConstantPool.EMPTY_MULTINAME );

		for( int i = 1; i < multinameCount; ++i )
		{
			constantPool.multinameTable.add( readAbstractMultiname( input ) );
		}
	}

	private void readNamespaces( final AbcInputStream input )
			throws IOException, AbcException
	{
		final int namespaceCount = input.readU30();

		constantPool.namespaceTable = new ArrayList<Namespace>( namespaceCount );
		constantPool.namespaceTable.add( ConstantPool.ANY_NAMESPACE );

		for( int i = 1; i < namespaceCount; ++i )
		{
			final Namespace namespace = new Namespace();

			namespace.kind = NamespaceKind.getKind( input.readU08() );
			namespace.name = constantPool.getString( input.readU30() );

			constantPool.namespaceTable.add( namespace );
		}
	}

	private void readNamespaceSets( final AbcInputStream input )
			throws IOException
	{
		final int namespaceSetCount = input.readU30();

		constantPool.namespaceSetTable = new ArrayList<NamespaceSet>(
				namespaceSetCount );
		constantPool.namespaceSetTable.add( ConstantPool.EMPTY_NAMESPACESET );

		for( int i = 1; i < namespaceSetCount; ++i )
		{
			final int namespaceCount = input.readU08();
			final NamespaceSet namespaceSet = new NamespaceSet( namespaceCount );

			for( int j = 0; j < namespaceCount; ++j )
			{
				namespaceSet.add( constantPool.getNamespace( input.readU30() ) );
			}

			constantPool.namespaceSetTable.add( namespaceSet );
		}
	}

	private QName readQName( final AbcInputStream input ) throws IOException
	{
		final QName result = new QName();

		result.namespace = constantPool.getNamespace( input.readU30() );
		result.name = constantPool.getString( input.readU30() );

		return result;
	}

	private QNameA readQNameA( final AbcInputStream input ) throws IOException
	{
		final QNameA result = new QNameA();

		result.namespace = constantPool.getNamespace( input.readU30() );
		result.name = constantPool.getString( input.readU30() );

		return result;
	}

	private RTQName readRTQName( final AbcInputStream input )
			throws IOException
	{
		final RTQName result = new RTQName();

		result.name = constantPool.getString( input.readU30() );

		return result;
	}

	private RTQNameA readRTQNameA( final AbcInputStream input )
			throws IOException
	{
		final RTQNameA result = new RTQNameA();

		result.name = constantPool.getString( input.readU30() );

		return result;
	}

	private void readScripts( final AbcInputStream input ) throws IOException,
			AbcException
	{
		final int scriptCount = input.readU30();

		scripts = new ArrayList<Script>( scriptCount );

		for( int i = 0; i < scriptCount; ++i )
		{
			final Script script = new Script();

			script.abc = this;

			script.initializerIndex = input.readU30();
			script.traits = readTraits( input );

			scripts.add( script );
		}
	}

	private void readStrings( final AbcInputStream input ) throws IOException
	{
		final int stringCount = input.readU30();

		constantPool.stringTable = new ArrayList<String>( stringCount );
		constantPool.stringTable.add( ConstantPool.EMPTY_STRING );

		for( int i = 1; i < stringCount; ++i )
		{
			constantPool.stringTable.add( input.readString() );
		}
	}

	private TraitClass readTraitClass( final AbcInputStream input )
			throws IOException
	{
		final TraitClass result = new TraitClass();

		result.slotIndex = input.readU30();
		result.classIndex = input.readU30();

		return result;
	}

	private TraitConst readTraitConst( final AbcInputStream input )
			throws IOException, AbcException
	{
		final TraitConst result = new TraitConst();

		result.slotIndex = input.readU30();
		result.type = constantPool.getMultiname( input.readU30() );

		final int valueIndex = input.readU30();

		if( 0 != valueIndex )
		{
			result.valueType = ConstantType.getKind( input.readU08() );

			result.value = constantPool.getConstantValue( result.valueType,
					valueIndex );
		}

		return result;
	}

	private TraitFunction readTraitFunction( final AbcInputStream input )
			throws IOException
	{
		final TraitFunction result = new TraitFunction();

		result.slotIndex = input.readU30();
		result.functionIndex = input.readU30();

		return result;
	}

	private TraitGetter readTraitGetter( final int attributes,
			final AbcInputStream input ) throws IOException
	{
		final TraitGetter result = new TraitGetter();

		result.dispIndex = input.readU30();
		result.methodIndex = input.readU30();

		result.isFinal = 0 != ( attributes & 0x01 );
		result.isOverride = 0 != ( attributes & 0x02 );

		return result;
	}

	private TraitMethod readTraitMethod( final int attributes,
			final AbcInputStream input ) throws IOException
	{
		final TraitMethod result = new TraitMethod();

		result.dispIndex = input.readU30();
		result.methodIndex = input.readU30();

		result.isFinal = 0 != ( attributes & 0x01 );
		result.isOverride = 0 != ( attributes & 0x02 );

		return result;
	}

	private List<AbstractTrait> readTraits( final AbcInputStream input )
			throws AbcException, IOException
	{
		final int traitCount = input.readU30();
		final List<AbstractTrait> traits = new ArrayList<AbstractTrait>(
				traitCount );

		for( int i = 0; i < traitCount; ++i )
		{
			final int traitNameIndex = input.readU30();

			if( 0 == traitNameIndex )
			{
				throw new AbcException( "Trait name index must not be zero." );
			}

			final AbstractMultiname traitName = constantPool
					.getMultiname( traitNameIndex );

			if( traitName.kind != MultinameKind.QName )
			{
				throw new AbcException( "Trait name has to be a QName." );
			}

			final int kind = input.readU08();
			final int attributes = ( kind & 0xf0 ) >> 4;

			final TraitKind traitKind = TraitKind.getKind( kind & 0x0f );

			AbstractTrait trait;

			switch( traitKind )
			{
				case Slot:
					trait = readTraitSlot( input );
					break;

				case Const:
					trait = readTraitConst( input );
					break;

				case Class:
					trait = readTraitClass( input );
					break;

				case Function:
					trait = readTraitFunction( input );
					break;

				case Method:
					trait = readTraitMethod( attributes, input );
					break;

				case Getter:
					trait = readTraitGetter( attributes, input );
					break;

				case Setter:
					trait = readTraitSetter( attributes, input );
					break;

				default:
					throw new AbcException();
			}

			trait.abc = this;
			trait.name = (QName)traitName;

			if( 0 != ( attributes & 0x04 ) )
			{
				final int metaCount = input.readU30();

				trait.metadata = new ArrayList<Metadata>( metaCount );

				for( int j = 0; j < metaCount; ++j )
				{
					trait.metadata.add( getMetadata( input.readU30() ) );
				}
			}

			traits.add( trait );
		}

		return traits;
	}

	private TraitSetter readTraitSetter( final int attributes,
			final AbcInputStream input ) throws IOException
	{
		final TraitSetter result = new TraitSetter();

		result.dispIndex = input.readU30();
		result.methodIndex = input.readU30();

		result.isFinal = 0 != ( attributes & 0x01 );
		result.isOverride = 0 != ( attributes & 0x02 );

		return result;
	}

	private TraitSlot readTraitSlot( final AbcInputStream input )
			throws IOException, AbcException
	{
		final TraitSlot result = new TraitSlot();

		result.slotIndex = input.readU30();
		result.type = constantPool.getMultiname( input.readU30() );

		final int valueIndex = input.readU30();

		if( 0 != valueIndex )
		{
			result.valueType = ConstantType.getKind( input.readU08() );

			result.value = constantPool.getConstantValue( result.valueType,
					valueIndex );
		}

		return result;
	}

	private Typename readTypename( final AbcInputStream input )
			throws IOException, AbcException
	{
		final Typename result = new Typename();

		result.name = (QName)constantPool.getMultiname( input.readU30() );

		final int paramCount = input.readU30();

		result.parameters = new ArrayList<AbstractMultiname>( paramCount );

		for( int i = 0; i < paramCount; ++i )
		{
			result.parameters
					.add( constantPool.getMultiname( input.readU30() ) );
		}

		return result;
	}

	private void readUnsignedIntegers( final AbcInputStream input )
			throws IOException
	{
		final int uintCount = input.readU30();

		constantPool.uintTable = new ArrayList<Long>( uintCount );
		constantPool.uintTable.add( 0L );

		for( int i = 1; i < uintCount; ++i )
		{
			constantPool.uintTable.add( input.readU32() );
		}
	}

	public byte[] toByteArray() throws IOException, AbcException
	{
		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		write( output );

		return output.toByteArray();
	}

	public void write( final DoABCTag tag ) throws IOException, AbcException
	{
		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		write( output );

		output.flush();

		tag.abcData = output.toByteArray();

		output.close();
	}

	public void write( final File file ) throws IOException, AbcException
	{
		final OutputStream output = new FileOutputStream( file );

		write( output );

		output.flush();
		output.close();
	}

	public void write( final OutputStream output ) throws IOException,
			AbcException
	{
		final AbcOutputStream abcOutput = new AbcOutputStream( output );

		// TODO replace with chained visitor

		accept( new ConstantPoolBuilder() );
		accept( new MetadataBuilder() );
		accept( new AbcBindingBuilder() );

		abcOutput.writeU16( minorVersion );
		abcOutput.writeU16( majorVersion );

		writeConstantPool( abcOutput );
		writeMethods( abcOutput );
		writeMetadata( abcOutput );
		writeInstances( abcOutput );
		writeClasses( abcOutput );
		writeScripts( abcOutput );
		writeMethodBodies( abcOutput );
	}

	public void write( final String pathname ) throws IOException, AbcException
	{
		write( new File( pathname ) );
	}

	private void writeClasses( final AbcOutputStream output )
			throws IOException
	{
		//
		// TODO topsort dependency graph
		//

		final int classCount = classes.size();

		for( int i = 0; i < classCount; ++i )
		{
			final Class klass = classes.get( i );

			output.writeU30( klass.classInitializerIndex );
			writeTraits( klass.traits, output );
		}
	}

	private void writeConstantPool( final AbcOutputStream output )
			throws IOException
	{
		//
		// This is not documented anywhere: But if the size is exactly 1, which
		// is the constant part of the pool, we write 0. Otherwise the full size
		// including the constant which is dropped.
		//

		int n;

		if( constantPool.intTable.size() > 1 )
		{
			output.writeU30( ( n = constantPool.intTable.size() ) );

			for( int i = 1; i < n; ++i )
			{
				output.writeS32( constantPool.intTable.get( i ) );
			}
		}
		else
		{
			output.writeU30( 0 );
		}

		if( constantPool.uintTable.size() > 1 )
		{
			output.writeU30( ( n = constantPool.uintTable.size() ) );

			for( int i = 1; i < n; ++i )
			{
				output.writeU32( constantPool.uintTable.get( i ) );
			}
		}
		else
		{
			output.writeU30( 0 );
		}

		if( constantPool.doubleTable.size() > 1 )
		{
			output.writeU30( ( n = constantPool.doubleTable.size() ) );

			for( int i = 1; i < n; ++i )
			{
				output.writeD64( constantPool.doubleTable.get( i ) );
			}
		}
		else
		{
			output.writeU30( 0 );
		}

		if( constantPool.stringTable.size() > 1 )
		{
			output.writeU30( ( n = constantPool.stringTable.size() ) );

			for( int i = 1; i < n; ++i )
			{
				output.writeString( constantPool.stringTable.get( i ) );
			}
		}
		else
		{
			output.writeU30( 0 );
		}

		if( constantPool.namespaceTable.size() > 1 )
		{
			output.writeU30( ( n = constantPool.namespaceTable.size() ) );

			for( int i = 1; i < n; ++i )
			{
				final Namespace namespace = constantPool.namespaceTable.get( i );

				output.writeU08( namespace.kind.getByte() );
				output.writeU30( constantPool.getIndex( namespace.name ) );
			}
		}
		else
		{
			output.writeU30( 0 );
		}

		if( constantPool.namespaceSetTable.size() > 1 )
		{
			output.writeU30( ( n = constantPool.namespaceSetTable.size() ) );

			for( int i = 1; i < n; ++i )
			{
				final NamespaceSet namespaceSet = constantPool.namespaceSetTable
						.get( i );

				output.writeU08( namespaceSet.size() );

				for( final Namespace namespace : namespaceSet )
				{
					output.writeU30( constantPool.getIndex( namespace ) );
				}
			}
		}
		else
		{
			output.writeU30( 0 );
		}

		if( constantPool.multinameTable.size() > 1 )
		{
			output.writeU30( ( n = constantPool.multinameTable.size() ) );

			for( int i = 1; i < n; ++i )
			{
				final AbstractMultiname multiname = constantPool.multinameTable
						.get( i );

				output.writeU08( multiname.kind.getByte() );

				switch( multiname.kind )
				{
					case QName:
					case QNameA:
						writeQName( (QName)multiname, output );
						break;

					case RTQName:
					case RTQNameA:
						writeRTQName( (RTQName)multiname, output );
						break;

					case Multiname:
					case MultinameA:
						writeMultiname( (Multiname)multiname, output );
						break;

					case MultinameL:
					case MultinameLA:
						writeMultinameL( (MultinameL)multiname, output );
						break;

					case Typename:
						writeTypename( (Typename)multiname, output );
						break;
				}
			}
		}
		else
		{
			output.writeU30( 0 );
		}
	}

	private void writeExceptions( final MethodBody methodBody,
			final List<ExceptionHandler> exceptions,
			final AbcOutputStream output ) throws IOException
	{
		final int exceptionCount = exceptions.size();

		output.writeU30( exceptionCount );

		for( int i = 0; i < exceptionCount; ++i )
		{
			final ExceptionHandler exceptionHandler = exceptions.get( i );

			output.writeU30( exceptionHandler.from.getPosition() );
			output.writeU30( exceptionHandler.to.getPosition() );
			output.writeU30( exceptionHandler.target.getPosition() );
			output.writeU30( constantPool
					.getIndex( exceptionHandler.exceptionType ) );
			output.writeU30( constantPool
					.getIndex( exceptionHandler.variableName ) );
		}
	}

	private void writeInstances( final AbcOutputStream output )
			throws IOException
	{
		//
		// TODO topsort dependency graph
		//

		final int n = instances.size();

		output.writeU30( n );

		for( int i = 0; i < n; ++i )
		{
			final Instance instance = instances.get( i );

			output.writeU30( constantPool.getIndex( instance.name ) );

			if( null != instance.base )
			{
				output.writeU30( constantPool.getIndex( instance.base ) );
			}
			else
			{
				output.writeU30( 0 );
			}

			int flags = 0;

			flags |= instance.isSealed ? 0x01 : 0x00;
			flags |= instance.isFinal ? 0x02 : 0x00;
			flags |= instance.isInterface ? 0x04 : 0x00;
			flags |= null != instance.protectedNamespace ? 0x08 : 0x00;

			output.writeU08( flags );

			if( null != instance.protectedNamespace )
			{
				output.writeU30( constantPool
						.getIndex( instance.protectedNamespace ) );
			}

			final int interfaceCount = instance.interfaces.size();

			output.writeU30( interfaceCount );

			for( int j = 0; j < interfaceCount; ++j )
			{
				output.writeU30( constantPool.getIndex( instance.interfaces
						.get( j ) ) );
			}

			output.writeU30( instance.instanceInitializerIndex );

			writeTraits( instance.traits, output );
		}
	}

	private void writeMetadata( final AbcOutputStream output )
			throws IOException
	{
		final int n = metadata.size();

		output.writeU30( n );

		for( int i = 0; i < n; ++i )
		{
			final Metadata meta = metadata.get( i );

			output.writeU30( constantPool.getIndex( meta.name ) );

			final int itemCount = meta.attributes.size();

			output.writeU30( itemCount );

			final Set<Entry<String, String>> entrySet = meta.attributes
					.entrySet();

			for( final Entry<String, String> entry : entrySet )
			{
				output.writeU30( constantPool.getIndex( entry.getKey() ) );
			}

			for( final Entry<String, String> entry : entrySet )
			{
				output.writeU30( constantPool.getIndex( entry.getValue() ) );
			}
		}
	}

	private void writeMethodBodies( final AbcOutputStream output )
			throws IOException, AbcException
	{
		final int methodCount = methods.size();
		int methodBodyCount = 0;

		for( final Method method : methods )
		{
			if( null != method.body )
			{
				++methodBodyCount;
			}
		}

		output.writeU30( methodBodyCount );

		for( int i = 0; i < methodCount; ++i )
		{
			final Method method = methods.get( i );

			if( null == method.body )
			{
				continue;
			}

			final MethodBody methodBody = method.body;
			byte[] code = null;

			try
			{
				code = bytecodeEncoder.encode( methodBody.code );
			}
			catch( final MarkerException markerException )
			{
				final StringWriter stringWriter = new StringWriter();
				final PrintWriter printWriter = new PrintWriter( stringWriter );
				final BytecodePrinter bytecodePrinter = new BytecodePrinter(
						printWriter );
				bytecodePrinter.interpret( new AbcEnvironment( new AbcContext(
						this ) ), methodBody.code );

				throw new AbcException(
						"Could not write method. Bytecode dump:\n"
								+ stringWriter.toString(), markerException );
			}

			output.writeU30( i );
			output.writeU30( methodBody.maxStack );
			output.writeU30( methodBody.localCount );
			output.writeU30( methodBody.initScopeDepth );
			output.writeU30( methodBody.maxScopeDepth );
			output.writeU30( code.length );

			output.write( code );

			writeExceptions( methodBody, methodBody.exceptions, output );
			writeTraits( methodBody.traits, output );
		}
	}

	private void writeMethods( final AbcOutputStream output )
			throws IOException
	{
		final int n = methods.size();

		output.writeU30( n );

		for( int i = 0; i < n; ++i )
		{
			final Method method = methods.get( i );

			output.writeU30( method.parameters.size() );
			output.writeU30( constantPool.getIndex( method.returnType ) );

			for( final Parameter parameter : method.parameters )
			{
				output.writeU30( constantPool.getIndex( parameter.type ) );
			}

			output.writeU30( constantPool.getIndex( method.name ) );

			int flags = 0;

			flags |= ( method.needsArguments ) ? 0x01 : 0x00;
			flags |= ( method.needsActivation ) ? 0x02 : 0x00;
			flags |= ( method.needsRest ) ? 0x04 : 0x00;
			flags |= ( method.hasOptionalParameters ) ? 0x08 : 0x00;
			flags |= ( method.setsDXNS ) ? 0x40 : 0x00;
			flags |= ( method.hasParameterNames ) ? 0x80 : 0x00;

			output.writeU08( flags );

			if( method.hasOptionalParameters )
			{
				int optionalCount = 0;

				for( final Parameter parameter : method.parameters )
				{
					if( parameter.isOptional )
					{
						++optionalCount;
					}
				}

				output.writeU30( optionalCount );

				final int paramCount = method.parameters.size();

				for( int j = paramCount - optionalCount; j < paramCount; ++j )
				{
					final Parameter parameter = method.parameters.get( j );

					output.writeU30( constantPool.getIndex(
							parameter.optionalType, parameter.optionalValue ) );
					output.writeU08( parameter.optionalType.getByte() );
				}
			}

			if( method.hasParameterNames )
			{
				for( final Parameter parameter : method.parameters )
				{
					output.writeU30( constantPool.getIndex( parameter.name ) );
				}
			}
		}
	}

	private void writeMultiname( final Multiname multiname,
			final AbcOutputStream output ) throws IOException
	{
		output.writeU30( constantPool.getIndex( multiname.name ) );
		output.writeU30( constantPool.getIndex( multiname.namespaceSet ) );
	}

	private void writeMultinameL( final MultinameL multinameL,
			final AbcOutputStream output ) throws IOException
	{
		output.writeU30( constantPool.getIndex( multinameL.namespaceSet ) );
	}

	private void writeQName( final QName qName, final AbcOutputStream output )
			throws IOException
	{
		output.writeU30( constantPool.getIndex( qName.namespace ) );
		output.writeU30( constantPool.getIndex( qName.name ) );
	}

	private void writeRTQName( final RTQName rtqName,
			final AbcOutputStream output ) throws IOException
	{
		output.writeU30( constantPool.getIndex( rtqName.name ) );
	}

	private void writeScripts( final AbcOutputStream output )
			throws IOException
	{
		final int scriptCount = scripts.size();

		output.writeU30( scriptCount );

		for( int i = 0; i < scriptCount; ++i )
		{
			final Script script = scripts.get( i );

			output.writeU30( script.initializerIndex );
			writeTraits( script.traits, output );
		}
	}

	private void writeTraits( final List<AbstractTrait> traits,
			final AbcOutputStream output ) throws IOException
	{
		final int n = traits.size();

		output.writeU30( n );

		for( int i = 0; i < n; ++i )
		{
			final AbstractTrait trait = traits.get( i );
			final TraitKind traitKind = trait.kind;
			final boolean hasMetadata = null != trait.metadata
					&& trait.metadata.size() > 0;

			output.writeU30( constantPool.getIndex( trait.name ) );

			final int kind = traitKind.getByte();
			int attributes = hasMetadata ? 0x04 : 0x00;

			switch( traitKind )
			{
				case Method:
					final TraitMethod traitMethod = (TraitMethod)trait;
					attributes |= traitMethod.isFinal ? 0x01 : 0x00;
					attributes |= traitMethod.isOverride ? 0x02 : 0x00;
					break;
				case Getter:
					final TraitGetter traitGetter = (TraitGetter)trait;
					attributes |= traitGetter.isFinal ? 0x01 : 0x00;
					attributes |= traitGetter.isOverride ? 0x02 : 0x00;
					break;
				case Setter:
					final TraitSetter traitSetter = (TraitSetter)trait;
					attributes |= traitSetter.isFinal ? 0x01 : 0x00;
					attributes |= traitSetter.isOverride ? 0x02 : 0x00;
					break;
			}

			output.writeU08( ( attributes << 4 ) | kind );

			switch( traitKind )
			{
				case Slot:
					final TraitSlot traitSlot = (TraitSlot)trait;

					output.writeU30( traitSlot.slotIndex );
					output.writeU30( constantPool.getIndex( traitSlot.type ) );

					if( null != traitSlot.value )
					{
						output.writeU30( constantPool.getIndex(
								traitSlot.valueType, traitSlot.value ) );
						output.writeU08( traitSlot.valueType.getByte() );
					}
					else
					{
						output.writeU30( 0 );
					}
					break;

				case Const:
					final TraitConst traitConst = (TraitConst)trait;

					output.writeU30( traitConst.slotIndex );
					output.writeU30( constantPool.getIndex( traitConst.type ) );

					if( null != traitConst.value )
					{
						output.writeU30( constantPool.getIndex(
								traitConst.valueType, traitConst.value ) );
						output.writeU08( traitConst.valueType.getByte() );
					}
					else
					{
						output.writeU30( 0 );
					}
					break;

				case Class:
					final TraitClass traitClass = (TraitClass)trait;

					output.writeU30( traitClass.slotIndex );
					output.writeU30( traitClass.classIndex );
					break;

				case Function:
					final TraitFunction traitFunction = (TraitFunction)trait;

					output.writeU30( traitFunction.slotIndex );
					output.writeU30( traitFunction.functionIndex );
					break;

				case Method:
					final TraitMethod traitMethod = (TraitMethod)trait;

					output.writeU30( traitMethod.dispIndex );
					output.writeU30( traitMethod.methodIndex );
					break;

				case Getter:
					final TraitGetter traitGetter = (TraitGetter)trait;

					output.writeU30( traitGetter.dispIndex );
					output.writeU30( traitGetter.methodIndex );
					break;

				case Setter:
					final TraitSetter traitSetter = (TraitSetter)trait;

					output.writeU30( traitSetter.dispIndex );
					output.writeU30( traitSetter.methodIndex );
					break;
			}

			if( hasMetadata )
			{
				final int m = trait.metadata.size();

				output.writeU30( m );

				for( int j = 0; j < m; ++j )
				{
					output.writeU30( getIndex( trait.metadata.get( j ) ) );
				}
			}
		}
	}

	private void writeTypename( final Typename typename,
			final AbcOutputStream output ) throws IOException
	{
		output.writeU30( constantPool.getIndex( typename.name ) );
		output.writeU30( typename.parameters.size() );

		for( final AbstractMultiname name : typename.parameters )
		{
			output.writeU30( constantPool.getIndex( name ) );
		}
	}
}
