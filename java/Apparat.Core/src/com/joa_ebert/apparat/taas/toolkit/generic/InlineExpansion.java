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

package com.joa_ebert.apparat.taas.toolkit.generic;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.abc.Method;
import com.joa_ebert.apparat.abc.MultinameKind;
import com.joa_ebert.apparat.abc.NamespaceKind;
import com.joa_ebert.apparat.abc.multinames.QName;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.Taas;
import com.joa_ebert.apparat.taas.TaasBuilder;
import com.joa_ebert.apparat.taas.TaasCode;
import com.joa_ebert.apparat.taas.TaasEdge;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.compiler.TaasCompiler;
import com.joa_ebert.apparat.taas.constants.TaasMultiname;
import com.joa_ebert.apparat.taas.expr.TCallProperty;
import com.joa_ebert.apparat.taas.expr.TReturn;
import com.joa_ebert.apparat.taas.expr.TReturnVoid;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;
import com.joa_ebert.apparat.taas.types.MultinameType;
import com.joa_ebert.apparat.taas.types.TaasType;
import com.joa_ebert.apparat.taas.types.VoidType;

public class InlineExpansion implements ITaasTool
{
	private static final class InlineTarget
	{
		public final TaasVertex vertex;
		public final TaasMethod method;
		public final boolean replace;

		public InlineTarget( final TaasVertex vertex, final TaasMethod method,
				final boolean replace )
		{
			this.vertex = vertex;
			this.method = method;
			this.replace = replace;
		}
	}

	private static final boolean DEBUG = true;

	private static final Logger LOG = DEBUG ? Logger
			.getLogger( InlineExpansion.class.getName() ) : null;

	private static final Taas TAAS = new Taas();
	private final TaasBuilder builder = new TaasBuilder();

	private boolean canInlineMember( final TaasMethod method,
			final AbcEnvironment.PropertyInfo propertyInfo )
	{
		if( containsRecursion( method, propertyInfo ) )
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	private boolean containsRecursion( final TaasMethod method,
			final AbcEnvironment.PropertyInfo propertyInfo )
	{
		final TaasCode code = method.code;
		final List<TaasVertex> vertices = code.vertexList();

		for( final TaasVertex vertex : vertices )
		{
			final TaasValue value = vertex.value;
			final TCallProperty callProperty = TaasToolkit.search( value,
					TCallProperty.class );

			if( null != callProperty )
			{
				final TaasValue object = callProperty.object;
				final TaasMultiname property = callProperty.property;

				if( object.getType() instanceof MultinameType
						&& property.getType() instanceof MultinameType )
				{
					final MultinameType mobj = (MultinameType)object.getType();
					final MultinameType mprp = (MultinameType)property
							.getType();

					if( mobj.runtimeName != null || mprp.runtimeName != null )
					{
						continue;
					}

					final AbcEnvironment.PropertyInfo calledProperty = method.typer
							.findProperty( mobj, mprp );

					if( null == calledProperty )
					{
						if( DEBUG )
						{
							LOG.info( "Typer could not find property (" + mobj
									+ ", " + mprp + ")" );
						}

						continue;
					}
					else
					{
						if( propertyInfo.equals( calledProperty ) )
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private void inline( final TaasMethod targetMethod,
			final TaasVertex insertionVertex, final TaasMethod inlinedMethod,
			final boolean replace )
	{
		try
		{
			final TaasCode targetCode = targetMethod.code;

			//
			// Now copy all vertices and edges into the method.
			// We ignore of course the start and end vertex.
			//

			final TaasCode inlinedCode = inlinedMethod.code;

			for( final TaasVertex inlinedVertex : inlinedCode.vertexList() )
			{
				if( VertexKind.Default != inlinedVertex.kind )
				{
					continue;
				}

				targetCode.add( inlinedVertex );
			}

			for( final TaasEdge inlinedEdge : inlinedCode.edgeList() )
			{
				if( inlinedEdge.startVertex.kind == VertexKind.Default
						&& inlinedEdge.endVertex.kind == VertexKind.Default )
				{
					targetCode.add( inlinedEdge );
				}
			}

			//
			// Now we only have to add the proper connections of the source and
			// the sink into the new method.
			//

			List<TaasEdge> incommingOf = targetCode
					.incommingOf( insertionVertex );

			final TaasVertex sourceVertex = inlinedCode.outgoingOf(
					inlinedCode.getEntryVertex() ).get( 0 ).endVertex;

			for( final TaasEdge edge : incommingOf )
			{
				edge.endVertex = sourceVertex;
			}

			incommingOf = inlinedCode.incommingOf( inlinedCode.getExitVertex() );

			if( !replace )
			{
				for( final TaasEdge edge : incommingOf )
				{
					targetCode.add( new TaasEdge( edge.startVertex,
							insertionVertex ) );
				}
			}
			else
			{
				final TaasVertex[] returns = new TaasVertex[ incommingOf.size() ];
				int i = 0;

				for( final TaasEdge edge : incommingOf )
				{
					returns[ i++ ] = edge.startVertex;
				}

				final List<TaasEdge> outgoingOf = targetCode
						.outgoingOf( insertionVertex );

				for( final TaasEdge edge : outgoingOf )
				{
					for( final TaasVertex vertex : returns )
					{
						targetCode.add( new TaasEdge( vertex, edge.endVertex,
								edge.kind ) );
					}
				}

				TaasToolkit.remove( targetMethod, insertionVertex );
			}
		}
		catch( final ControlFlowGraphException exception )
		{
			exception.printStackTrace();
		}
	}

	public boolean manipulate( final AbcEnvironment environment,
			final TaasMethod method )
	{
		boolean changed = false;

		final TaasCode code = method.code;
		final List<TaasVertex> vertices = code.vertexList();
		InlineTarget target = null;

		for( final TaasVertex vertex : vertices )
		{
			final TaasValue value = vertex.value;
			final TCallProperty callProperty = TaasToolkit.search( value,
					TCallProperty.class );

			//
			// Inline TCallProperty expressions:
			//

			if( null != callProperty )
			{
				final TaasValue object = callProperty.object;
				final TaasMultiname property = callProperty.property;

				if( object instanceof TaasLocal )
				{
					final TaasLocal local = (TaasLocal)object;

					if( 0 == local.getIndex() )
					{
						if( !( local.getType() instanceof MultinameType ) )
						{
							//
							// We support only typed objects.
							//

							continue;
						}

						if( !( property.getType() instanceof MultinameType ) )
						{
							//
							// We support only typed properties.
							//

							continue;
						}

						final MultinameType mobj = (MultinameType)local
								.getType();
						final MultinameType mprp = (MultinameType)property
								.getType();

						if( mobj.runtimeName != null
								|| mprp.runtimeName != null )
						{
							//
							// We do not touch anything that may be changed
							// during runtime.
							//

							continue;
						}

						final AbcEnvironment.PropertyInfo propertyInfo = method.typer
								.findProperty( mobj, mprp );

						if( null == propertyInfo )
						{
							//
							// Typer could not find method.
							//

							if( DEBUG )
							{
								LOG.info( "Typer could not find property ("
										+ mobj + ", " + mprp + ")" );
							}

							continue;
						}

						if( mprp.multiname.kind == MultinameKind.QName
								&& ( (QName)mprp.multiname ).namespace.kind == NamespaceKind.PrivateNamespace )
						{
							if( null != propertyInfo.instance )
							{
								// FIXME should compare against scope of
								// original method

								if( !propertyInfo.instance.name
										.equals( mobj.multiname ) )
								{
									if( DEBUG )
									{
										LOG.info( "Property (" + mobj + ", "
												+ mprp + ") is private and "
												+ "part of a different "
												+ "instance." );
									}

									continue;
								}
							}
							else
							{
								if( DEBUG )
								{
									LOG.info( "Property (" + mobj + ", " + mprp
											+ ") is private but not "
											+ "part of an instance." );
								}

								continue;
							}
						}
						else
						{
							if( !propertyInfo.isFinal )
							{

								if( DEBUG )
								{
									LOG.info( "Property (" + mobj + ", " + mprp
											+ ") is not final." );
								}

								continue;
							}
						}

						final Method abcMethod = propertyInfo.method;

						if( null == abcMethod.body
								|| null == abcMethod.body.code )
						{
							//
							// External or native method.
							//

							continue;
						}

						final TaasMethod inlinedMethod = builder.build(
								environment, abcMethod.body.code );

						final TaasCode inlinedCode = inlinedMethod.code;

						if( !canInlineMember( inlinedMethod, propertyInfo ) )
						{
							continue;
						}

						//
						// Shift the register indices so that we have no clash.
						//

						final int offset = method.locals.numRegisters();

						inlinedMethod.locals.offset( offset );

						final List<TaasLocal> registersToContribute = inlinedMethod.locals
								.getRegisterList();

						//
						// We do not want to add the first register since it
						// stores only the scope object. So we can drop it here.
						//

						for( int i = 1, n = registersToContribute.size(); i < n; ++i )
						{
							method.locals.add( registersToContribute.get( i ) );
						}

						final TaasType returnType = method.typer
								.toNativeType( abcMethod.returnType );

						//
						// We have to set the local variables according to their
						// parameters.
						//
						// So we search for an insertion point to set those
						// local variables with values of the parameters.
						//

						final TaasVertex insertPoint = inlinedCode
								.getEntryVertex();

						//
						// We ignore the local at index 0 since it stores only
						// the scope object.
						//

						int localIndex = 1;

						for( final TaasValue param : callProperty.parameters )
						{
							//
							// Set the local variable with the value of the
							// parameter.
							//

							TaasToolkit
									.insertAfter(
											inlinedMethod,
											insertPoint,
											new TaasVertex(
													TAAS
															.setLocal(
																	inlinedMethod.locals
																			.get( offset
																					+ localIndex++ ),
																	param ) ) );
						}

						method.locals.defragment();

						final LinkedList<TaasVertex> inlinedVertices = inlinedCode
								.vertexList();

						if( callProperty.getType() == VoidType.INSTANCE )
						{
							//
							// TODO remove ReturnValue statements from methods
							// that return a value but are called just like void
							// methods ...
							//

							final List<TaasVertex> removes = new LinkedList<TaasVertex>();

							for( final TaasVertex inlinedVertex : inlinedVertices )
							{
								if( VertexKind.Default != inlinedVertex.kind )
								{
									continue;
								}

								final TaasValue inlinedValue = inlinedVertex.value;

								if( inlinedValue instanceof TReturnVoid )
								{
									removes.add( inlinedVertex );
								}
							}

							for( final TaasVertex remove : removes )
							{
								try
								{
									TaasToolkit.remove( inlinedMethod, remove );
								}
								catch( final ControlFlowGraphException exception )
								{
									throw new TaasException( exception );
								}
							}

							target = new InlineTarget( vertex, inlinedMethod,
									true );
							break;
						}
						else
						{
							//
							// Since we return a value we need a register to
							// store the result.
							//

							final TaasLocal result = TaasToolkit
									.createRegister( method );

							//
							// Also, type the register here with the return type
							// of the inlined method.
							//

							result.typeAs( returnType );

							//
							// Next: Replace all TReturn expressions with a
							// TSetLocal instead.
							//

							final Map<TaasValue, TaasValue> replacements = new LinkedHashMap<TaasValue, TaasValue>();

							for( final TaasVertex inlinedVertex : inlinedVertices )
							{
								if( VertexKind.Default != inlinedVertex.kind )
								{
									continue;
								}

								final TaasValue inlinedValue = inlinedVertex.value;

								if( inlinedValue instanceof TReturn )
								{
									replacements
											.put(
													inlinedValue,
													TAAS
															.setLocal(
																	result,
																	( (TReturn)inlinedValue ).value ) );
								}
							}

							for( final Entry<TaasValue, TaasValue> replacement : replacements
									.entrySet() )
							{
								TaasToolkit.replace( inlinedMethod, replacement
										.getKey(), replacement.getValue() );
							}

							//
							// And finally, we replace the call expression with
							// the new resulting register.
							//

							TaasToolkit.replace( value, callProperty, result );

							//
							// In order to avoid a concurrent modification while
							// traversing the list we will now save the
							// information of what to do here.
							//

							target = new InlineTarget( vertex, inlinedMethod,
									false );
							break;
						}
					}
				}

			}
		}

		//
		// Inline all methods now.
		//

		if( null != target )
		{
			inline( method, target.vertex, target.method, target.replace );
			changed = true;
		}

		if( TaasCompiler.SHOW_ALL_TRANSFORMATIONS && changed )
		{
			TaasToolkit.debug( "InlineExpansion", method );
		}

		return changed;
	}
}
