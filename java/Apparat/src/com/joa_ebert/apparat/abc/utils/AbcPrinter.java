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

package com.joa_ebert.apparat.abc.utils;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;

import com.joa_ebert.apparat.abc.Abc;
import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.AbstractMultiname;
import com.joa_ebert.apparat.abc.AbstractTrait;
import com.joa_ebert.apparat.abc.Class;
import com.joa_ebert.apparat.abc.ExceptionHandler;
import com.joa_ebert.apparat.abc.Instance;
import com.joa_ebert.apparat.abc.Metadata;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.MethodBody;
import com.joa_ebert.apparat.abc.Parameter;
import com.joa_ebert.apparat.abc.Script;
import com.joa_ebert.apparat.abc.bytecode.Bytecode;
import com.joa_ebert.apparat.abc.bytecode.analysis.BytecodePrinter;
import com.joa_ebert.apparat.abc.traits.TraitClass;
import com.joa_ebert.apparat.abc.traits.TraitConst;
import com.joa_ebert.apparat.abc.traits.TraitFunction;
import com.joa_ebert.apparat.abc.traits.TraitGetter;
import com.joa_ebert.apparat.abc.traits.TraitMethod;
import com.joa_ebert.apparat.abc.traits.TraitSetter;
import com.joa_ebert.apparat.abc.traits.TraitSlot;
import com.joa_ebert.apparat.utils.IndentingPrintWriter;

/**
 * @author Joa Ebert
 * 
 */
public final class AbcPrinter
{
	private final IndentingPrintWriter output;
	private final BytecodePrinter bytecodePrinter;

	public AbcPrinter( final PrintWriter output )
	{
		this.output = new IndentingPrintWriter( output );

		bytecodePrinter = new BytecodePrinter( this.output );
		bytecodePrinter.setPrintName( false );
	}

	public void print( final Abc abc )
	{
		output.println( "Abc:" );
		output.pushIndent();
		{
			output.println( "Version: " + abc.majorVersion + ", "
					+ abc.minorVersion );

			new ConstantPoolPrinter( output ).print( abc.constantPool );

			printHeader( abc.scripts, "Scripts" );
			output.pushIndent();
			{
				for( final Script script : abc.scripts )
				{
					print( script );
				}
			}
			output.popIndent();
		}
		output.popIndent();
		output.flush();
	}

	public void print( final AbstractTrait trait )
	{
		switch( trait.kind )
		{
			case Class:
				print( (TraitClass)trait );
				break;

			case Const:
				print( (TraitConst)trait );
				break;

			case Function:
				print( (TraitFunction)trait );
				break;

			case Getter:
				print( (TraitGetter)trait );
				break;

			case Method:
				print( (TraitMethod)trait );
				break;

			case Setter:
				print( (TraitSetter)trait );
				break;

			case Slot:
				print( (TraitSlot)trait );
				break;

			default:
				output.println( "Trait:" );
				output.pushIndent();
				{
					output.println( "UNKNOWN" );
				}
				output.popIndent();
				break;
		}
	}

	public void print( final Bytecode bytecode )
	{
		output.println( "Bytecode:" );
		output.pushIndent();
		{
			bytecodePrinter.interpret( new AbcEnvironment( bytecode.abc ),
					bytecode );
		}
		output.popIndent();
		output.flush();
	}

	public void print( final Class klass )
	{
		output.println( "Class:" );
		output.pushIndent();
		{
			printTraits( klass.traits, true );
			printInitializer( klass.classInitializer );

			if( null != klass.instance )
			{
				print( klass.instance );
			}
		}
		output.popIndent();
		output.flush();
	}

	public void print( final ExceptionHandler exceptionHandler )
	{
		output.println( "ExceptionHandler:" );
		output.pushIndent();
		{
			output
					.println( "Type: "
							+ StringConverter
									.toString( exceptionHandler.exceptionType ) );
			output
					.println( "VariableName: "
							+ StringConverter
									.toString( exceptionHandler.variableName ) );
			output.println( "From: "
					+ StringConverter.toString( exceptionHandler.from ) );
			output.println( "To: "
					+ StringConverter.toString( exceptionHandler.to ) );
			output.println( "Target: "
					+ StringConverter.toString( exceptionHandler.target ) );
		}
		output.popIndent();
		output.flush();
	}

	public void print( final Instance instance )
	{
		output.println( "Instance:" );
		output.pushIndent();
		{
			output
					.println( "Name: "
							+ StringConverter.toString( instance.name ) );
			output
					.println( "Base: "
							+ StringConverter.toString( instance.base ) );
			output.println( "IsFinal: " + instance.isFinal );
			output.println( "IsInterface: " + instance.isInterface );
			output.println( "IsSealed: " + instance.isSealed );
			output.println( "ProtectedNamespace: "
					+ StringConverter.toString( instance.protectedNamespace ) );

			printHeader( instance.interfaces, "Interfaces" );
			output.pushIndent();
			{
				for( final AbstractMultiname interf : instance.interfaces )
				{
					output.println( StringConverter.toString( interf ) );
				}
			}
			output.popIndent();

			printInitializer( instance.instanceInitializer );
			printTraits( instance.traits, true );
		}
		output.popIndent();
		output.flush();
	}

	public void print( final Metadata metadata )
	{
		output.println( "Metadata:" );
		output.pushIndent();
		{
			output.println( "Name: " + metadata.name );
			output.pushIndent();
			{
				for( final Entry<String, String> attribute : metadata.attributes
						.entrySet() )
				{
					output.println( attribute.getKey() + ": "
							+ attribute.getValue() );
				}
			}
			output.popIndent();
		}
		output.popIndent();
		output.flush();
	}

	public void print( final Method method )
	{
		output.println( "Method:" );
		output.pushIndent();
		{
			output.println( "Name: " + method.name );
			output.println( "HasOptionalParameters: "
					+ method.hasOptionalParameters );
			output.println( "HasParameterNames: " + method.hasParameterNames );
			output.println( "NeedsActivation: " + method.needsActivation );
			output.println( "NeedsArguments: " + method.needsArguments );
			output.println( "NeedsRest: " + method.needsRest );
			output.println( "SetsDXNS: " + method.setsDXNS );
			output.println( "ReturnType: "
					+ StringConverter.toString( method.returnType ) );

			printHeader( method.parameters, "Parameters" );
			output.pushIndent();
			{
				for( final Parameter parameter : method.parameters )
				{
					print( parameter );
				}
			}
			output.popIndent();

			if( null != method.body )
			{
				print( method.body );
			}
		}
		output.popIndent();
		output.flush();
	}

	public void print( final MethodBody methodBody )
	{
		output.println( "MethodBody:" );
		output.pushIndent();
		{
			output.println( "LocalCount: " + methodBody.localCount );
			output.println( "InitScopeDepth: " + methodBody.initScopeDepth );
			output.println( "MaxScopeDepth: " + methodBody.maxScopeDepth );
			output.println( "MaxStack: " + methodBody.maxStack );

			printTraits( methodBody.traits, true );
			printExceptions( methodBody.exceptions );

			if( null != methodBody.code )
			{
				print( methodBody.code );
			}

		}
		output.popIndent();
		output.flush();
	}

	/**
	 * @param parameter
	 */
	private void print( final Parameter parameter )
	{
		output.println( "Parameter:" );
		output.pushIndent();
		{
			output.println( "Name: " + parameter.name );
			output.println( "Type: "
					+ StringConverter.toString( parameter.type ) );
			output.println( "IsOptional: " + parameter.isOptional );

			if( parameter.isOptional )
			{
				output
						.println( "OptionalType: "
								+ ( ( null != parameter.optionalType ) ? parameter.optionalType
										.toString()
										: "null" ) );

				output
						.println( "OptionalValue: "
								+ ( ( null != parameter.optionalValue ) ? parameter.optionalValue
										.toString()
										: "null" ) );
			}

		}
		output.popIndent();
		output.flush();
	}

	public void print( final Script script )
	{
		output.println( "Script:" );
		output.pushIndent();
		{
			printTraits( script.traits );
			printInitializer( script.initializer );
		}
		output.popIndent();
		output.flush();
	}

	public void print( final TraitClass trait )
	{
		output.println( "TraitClass:" );
		output.pushIndent();
		{
			printTrait( trait );

			output.println( "Slot: " + trait.slotIndex );

			print( trait.klass );
		}
		output.popIndent();
		output.flush();
	}

	public void print( final TraitConst trait )
	{
		output.println( "TraitConst:" );
		output.pushIndent();
		{
			printTrait( trait );

			output.println( "Slot: " + trait.slotIndex );
			output.println( "Type: " + StringConverter.toString( trait.type ) );
			output.println( "Value: "
					+ ( ( null != trait.value ) ? trait.value.toString()
							: "null" ) );
			output.println( "ValueType: "
					+ ( ( null != trait.valueType ) ? trait.valueType
							.toString() : "null" ) );
		}
		output.popIndent();
		output.flush();
	}

	public void print( final TraitFunction trait )
	{
		output.println( "TraitFunction:" );
		output.pushIndent();
		{
			printTrait( trait );

			output.println( "Slot: " + trait.slotIndex );

			output.pushIndent();
			{
				print( trait.function );
			}
			output.popIndent();
		}
		output.popIndent();
		output.flush();
	}

	public void print( final TraitGetter trait )
	{
		output.println( "TraitGetter:" );
		output.pushIndent();
		{
			printTrait( trait );

			output.println( "DispIndex: " + trait.dispIndex );
			output.println( "IsFinal: " + trait.isFinal );
			output.println( "IsOverride: " + trait.isOverride );

			output.pushIndent();
			{
				print( trait.method );
			}
			output.popIndent();
		}
		output.popIndent();
		output.flush();
	}

	public void print( final TraitMethod trait )
	{
		output.println( "TraitMethod:" );
		output.pushIndent();
		{
			printTrait( trait );

			output.println( "DispIndex: " + trait.dispIndex );
			output.println( "IsFinal: " + trait.isFinal );
			output.println( "IsOverride: " + trait.isOverride );

			output.pushIndent();
			{
				print( trait.method );
			}
			output.popIndent();
		}
		output.popIndent();
		output.flush();
	}

	public void print( final TraitSetter trait )
	{
		output.println( "TraitSetter:" );
		output.pushIndent();
		{
			printTrait( trait );

			output.println( "DispIndex: " + trait.dispIndex );
			output.println( "IsFinal: " + trait.isFinal );
			output.println( "IsOverride: " + trait.isOverride );

			output.pushIndent();
			{
				print( trait.method );
			}
			output.popIndent();
		}
		output.popIndent();
		output.flush();
	}

	public void print( final TraitSlot trait )
	{
		output.println( "TraitSlot:" );
		output.pushIndent();
		{
			printTrait( trait );

			output.println( "SlotIndex: " + trait.slotIndex );
			output.println( "Type: " + StringConverter.toString( trait.type ) );
			output.println( "Value: "
					+ ( ( null != trait.value ) ? trait.value.toString()
							: "null" ) );
			output.println( "ValueType: "
					+ ( ( null != trait.valueType ) ? trait.valueType
							.toString() : "null" ) );
		}
		output.popIndent();
		output.flush();
	}

	private void printExceptions( final List<ExceptionHandler> exceptions )
	{
		printHeader( exceptions, "ExceptionHandlers" );
		output.pushIndent();
		{
			for( final ExceptionHandler exceptionHandler : exceptions )
			{
				print( exceptionHandler );
			}
		}
		output.popIndent();
	}

	private void printHeader( final List<?> list, final String name )
	{
		output.println( name + " (" + list.size() + " total):" );
	}

	private void printInitializer( final Method initializer )
	{
		output.println( "Initializer:" );
		output.pushIndent();
		{
			print( initializer );
		}
		output.popIndent();
	}

	private void printMetadata( final List<Metadata> metadata )
	{
		printHeader( metadata, "Metadata" );
		output.pushIndent();
		{
			for( final Metadata meta : metadata )
			{
				print( meta );
			}
		}
		output.popIndent();
	}

	private void printTrait( final AbstractTrait trait )
	{
		output.println( "Name: " + StringConverter.toString( trait.name ) );
		printMetadata( trait.metadata );
	}

	private void printTraits( final List<AbstractTrait> traits )
	{
		printTraits( traits, false );
	}

	private void printTraits( final List<AbstractTrait> traits,
			final boolean ignoreClass )
	{
		printHeader( traits, "Traits" );
		output.pushIndent();
		{
			for( final AbstractTrait trait : traits )
			{
				if( ignoreClass )
				{
					if( !( trait instanceof TraitClass ) )
					{
						print( trait );
					}
				}
				else
				{
					print( trait );
				}
			}
		}
		output.popIndent();
	}
}
