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

package com.joa_ebert.apparat.taas.toolkit;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.TaasCode;
import com.joa_ebert.apparat.taas.TaasEdge;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasLocal;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasPhi;
import com.joa_ebert.apparat.taas.TaasReference;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;

/**
 * 
 * @author Joa Ebert
 * 
 */
public class TaasToolkit
{
	private static <E> List<E> cloneList( final List<E> list )
	{
		final List<E> result = new LinkedList<E>();

		for( final E element : list )
		{
			result.add( element );
		}

		return result;
	}

	public static TaasLocal createRegister( final TaasMethod method )
	{
		final TaasLocal local = method.locals.create();

		return local;
	}

	private static void fixPhiEdge( final TaasMethod method,
			final TaasEdge oldEdge, final TaasEdge newEdge )
	{
		final List<TaasPhi> phiExprs = phisOf( method );

		for( final TaasPhi phiExpr : phiExprs )
		{
			for( final TaasPhi.Element element : phiExpr.values )
			{
				if( element.edge == oldEdge )
				{
					element.edge = newEdge;
				}
			}
		}
	}

	/**
	 * Inserts a new vertex before the old vertex.
	 * 
	 * @param method
	 * @param oldVertex
	 * @param newVertex
	 */
	public static void insertBefore( final TaasMethod method,
			final TaasVertex oldVertex, final TaasVertex newVertex )
	{
		try
		{
			final TaasCode code = method.code;

			final List<TaasEdge> incommingEdges = code.incommingOf( oldVertex );

			if( !code.contains( newVertex ) )
			{
				code.add( newVertex );
			}

			for( final TaasEdge edge : incommingEdges )
			{
				edge.endVertex = newVertex;
			}

			code.add( new TaasEdge( newVertex, oldVertex ) );
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}
	}

	public static boolean phiCleanup( final TaasMethod method )
	{
		boolean changed = false;

		final List<TaasPhi> phiExprs = phisOf( method );

		for( final TaasPhi phiExpr : phiExprs )
		{
			changed = phiCleanup( method, phiExpr ) || changed;
		}

		return changed;
	}

	private static boolean phiCleanup( final TaasMethod method,
			final TaasPhi phiExpr )
	{
		final List<TaasPhi.Element> removes = new LinkedList<TaasPhi.Element>();

		for( final TaasPhi.Element element : phiExpr.values )
		{
			if( !method.code.contains( element.edge ) )
			{
				removes.add( element );
			}
		}

		boolean changed = false;

		for( final TaasPhi.Element element : removes )
		{
			changed = phiExpr.remove( element.value ) || changed;
		}

		return changed;
	}

	public static List<TaasPhi> phisOf( final TaasMethod method )
	{
		final List<TaasPhi> result = new LinkedList<TaasPhi>();

		final ControlFlowGraph<TaasVertex, TaasEdge> graph = method.code;

		for( final TaasVertex vertex : graph.vertexList() )
		{
			if( vertex.kind != VertexKind.Default )
			{
				continue;
			}

			final TaasValue value = vertex.value;

			if( null == value )
			{
				continue;
			}

			if( value instanceof TaasPhi )
			{
				result.add( (TaasPhi)value );
			}
			else
			{
				final Field[] fields = value.getClass().getFields();

				for( final Field field : fields )
				{
					if( field.isAnnotationPresent( TaasReference.class ) )
					{
						try
						{
							final Object referencedObject = field.get( value );

							if( referencedObject instanceof TaasPhi )
							{
								result.add( (TaasPhi)referencedObject );
							}
							else if( referencedObject instanceof TaasValue[] )
							{
								final TaasValue[] referenced = (TaasValue[])referencedObject;

								if( null != referenced )
								{
									for( final TaasValue referencedValue : referenced )
									{
										if( referencedValue instanceof TaasPhi )
										{
											result
													.add( (TaasPhi)referencedValue );
										}
									}
								}
							}
						}
						catch( final Exception e )
						{
							throw new TaasException( e );
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Whether or not <code>value</code> references <code>search</code>.
	 * 
	 * @param value
	 *            The value that might have a reference to <code>search</code>.
	 * @param search
	 *            The value to search for.
	 * 
	 * @return <code>true</code> if a reference exists; <code>false</code>
	 *         otherwise.
	 */
	public static boolean references( final TaasValue value,
			final TaasValue search )
	{
		if( null == value )
		{
			return false;
		}

		if( value instanceof TaasPhi )
		{
			final TaasPhi phi = (TaasPhi)value;

			for( final TaasPhi.Element element : phi.values )
			{
				if( element.value != null && element.value.equals( search ) )
				{
					return true;
				}

				if( references( element.value, search ) )
				{
					return true;
				}
			}

			return false;
		}
		else
		{
			final Field[] fields = value.getClass().getFields();

			for( final Field field : fields )
			{
				if( field.isAnnotationPresent( TaasReference.class ) )
				{
					try
					{
						final Object referencedObject = field.get( value );

						if( referencedObject instanceof TaasValue )
						{
							final TaasValue referenced = (TaasValue)referencedObject;

							if( null != referenced
									&& referenced.equals( search ) )
							{
								return true;
							}

							if( references( referenced, search ) )
							{
								return true;
							}
						}
						else if( referencedObject instanceof TaasValue[] )
						{
							final TaasValue[] referenced = (TaasValue[])referencedObject;

							if( null != referenced )
							{
								for( final TaasValue referencedValue : referenced )
								{
									if( null != referencedValue
											&& referencedValue.equals( search ) )
									{
										return true;
									}

									if( references( referencedValue, search ) )
									{
										return true;
									}
								}
							}
						}
					}
					catch( final Exception e )
					{
						throw new TaasException( e );
					}
				}
			}

			return false;
		}
	}

	/**
	 * Removes a vertex from the control flow graph without removing the
	 * references to the containing TaasValue.
	 * 
	 * @param method
	 *            The method of the graph.
	 * @param vertex
	 *            The vertex to remove.
	 * 
	 * @throws ControlFlowGraphException
	 *             Thrown if an CFG error occurs.
	 */
	public static void remove( final TaasMethod method, final TaasVertex vertex )
			throws ControlFlowGraphException
	{
		if( !method.code.contains( vertex ) )
		{
			return;
		}

		final ControlFlowGraph<TaasVertex, TaasEdge> graph = method.code;
		final List<TaasEdge> incommingEdges = graph.incommingOf( vertex );
		final List<TaasEdge> outgoingEdges = cloneList( graph
				.outgoingOf( vertex ) );

		graph.remove( vertex );

		for( final TaasEdge incommingEdge : incommingEdges )
		{
			for( final TaasEdge outgoingEdge : outgoingEdges )
			{
				if( !graph.containsEdge( incommingEdge.startVertex,
						outgoingEdge.endVertex ) )
				{
					final TaasEdge newEdge = new TaasEdge(
							incommingEdge.startVertex, outgoingEdge.endVertex,
							incommingEdge.kind );

					graph.add( newEdge );

					fixPhiEdge( method, incommingEdge, newEdge );
					fixPhiEdge( method, outgoingEdge, newEdge );
				}
			}
		}
	}

	/**
	 * Replaces all references to the TaasValue and the TaasValue itself.
	 * 
	 * @param method
	 *            The method of the value.
	 * @param search
	 *            The TaasValue to replace.
	 * @param replacement
	 *            The replacement of the TaasValue.
	 */
	public static void replace( final TaasMethod method,
			final TaasValue search, final TaasValue replacement )
	{
		final ControlFlowGraph<TaasVertex, TaasEdge> graph = method.code;

		for( final TaasVertex vertex : graph.vertexList() )
		{
			if( vertex.kind != VertexKind.Default )
			{
				continue;
			}

			if( vertex.value != null && vertex.value.equals( search ) )
			{
				vertex.value = replacement;
			}
			else
			{
				final TaasValue value = vertex.value;

				if( value instanceof TaasPhi )
				{
					final TaasPhi phi = (TaasPhi)value;

					for( final TaasPhi.Element element : phi.values )
					{
						if( element.value != null
								&& element.value.equals( search ) )
						{
							element.value = replacement;
						}
					}

					phi.updateType();
				}
				else
				{
					final Field[] fields = value.getClass().getFields();

					for( final Field field : fields )
					{
						if( field.isAnnotationPresent( TaasReference.class ) )
						{
							try
							{
								final Object referencedObject = field
										.get( value );

								if( referencedObject instanceof TaasValue )
								{
									final TaasValue referenced = (TaasValue)referencedObject;

									if( null != referenced
											&& referenced.equals( search ) )
									{
										field.set( value, replacement );
										value.updateType();
									}
								}
								else if( referencedObject instanceof TaasValue[] )
								{
									final TaasValue[] referenced = (TaasValue[])referencedObject;

									if( null != referenced )
									{
										int n = referenced.length;

										while( --n > -1 )
										{
											final TaasValue referencedValue = referenced[ n ];
											if( null != referencedValue
													&& referencedValue
															.equals( search ) )
											{
												referenced[ n ] = replacement;
												value.updateType();
											}
										}
									}
								}
							}
							catch( final Exception e )
							{
								throw new TaasException( e );
							}
						}
					}
				}
			}
		}
	}

	public static void replace( final TaasValue value, final TaasValue search,
			final TaasValue replacement )
	{
		if( null == value )
		{
			return;
		}

		if( value instanceof TaasPhi )
		{
			final TaasPhi phi = (TaasPhi)value;

			for( final TaasPhi.Element element : phi.values )
			{
				if( element.value != null && element.value.equals( search ) )
				{
					element.value = replacement;
					value.updateType();
				}
				else if( references( element.value, search ) )
				{
					replace( element.value, search, replacement );
				}
			}
		}
		else
		{
			final Field[] fields = value.getClass().getFields();

			for( final Field field : fields )
			{
				if( field.isAnnotationPresent( TaasReference.class ) )
				{
					try
					{
						final Object referencedObject = field.get( value );

						if( referencedObject instanceof TaasValue )
						{
							final TaasValue referenced = (TaasValue)referencedObject;

							if( null != referenced
									&& referenced.equals( search ) )
							{
								field.set( value, replacement );
								value.updateType();
							}
							else if( references( referenced, search ) )
							{
								replace( referenced, search, replacement );
							}
						}
						else if( referencedObject instanceof TaasValue[] )
						{
							final TaasValue[] referenced = (TaasValue[])referencedObject;

							if( null != referenced )
							{
								int n = referenced.length;

								while( --n > -1 )
								{
									final TaasValue referencedValue = referenced[ n ];
									if( null != referencedValue
											&& referencedValue.equals( search ) )
									{
										referenced[ n ] = replacement;
										value.updateType();
									}
									else if( references( referencedValue,
											search ) )
									{
										replace( referencedValue, search,
												replacement );
									}
								}
							}
						}
					}
					catch( final Exception e )
					{
						throw new TaasException( e );
					}
				}
			}
		}
	}
}
