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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.joa_ebert.apparat.abc.AbcEnvironment;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.EdgeKind;
import com.joa_ebert.apparat.controlflow.VertexKind;
import com.joa_ebert.apparat.taas.TaasConstant;
import com.joa_ebert.apparat.taas.TaasEdge;
import com.joa_ebert.apparat.taas.TaasException;
import com.joa_ebert.apparat.taas.TaasMethod;
import com.joa_ebert.apparat.taas.TaasPhi;
import com.joa_ebert.apparat.taas.TaasValue;
import com.joa_ebert.apparat.taas.TaasVertex;
import com.joa_ebert.apparat.taas.compiler.TaasCompiler;
import com.joa_ebert.apparat.taas.constants.TaasBoolean;
import com.joa_ebert.apparat.taas.constants.TaasInt;
import com.joa_ebert.apparat.taas.constants.TaasNumber;
import com.joa_ebert.apparat.taas.constants.TaasNumeric;
import com.joa_ebert.apparat.taas.constants.TaasUInt;
import com.joa_ebert.apparat.taas.expr.AbstractBinaryExpr;
import com.joa_ebert.apparat.taas.expr.AbstractUnaryExpr;
import com.joa_ebert.apparat.taas.expr.TAdd;
import com.joa_ebert.apparat.taas.expr.TBitAnd;
import com.joa_ebert.apparat.taas.expr.TBitNot;
import com.joa_ebert.apparat.taas.expr.TBitOr;
import com.joa_ebert.apparat.taas.expr.TBitXor;
import com.joa_ebert.apparat.taas.expr.TDecrement;
import com.joa_ebert.apparat.taas.expr.TDivide;
import com.joa_ebert.apparat.taas.expr.TEquals;
import com.joa_ebert.apparat.taas.expr.TGreaterEquals;
import com.joa_ebert.apparat.taas.expr.TGreaterThan;
import com.joa_ebert.apparat.taas.expr.TIf;
import com.joa_ebert.apparat.taas.expr.TIncrement;
import com.joa_ebert.apparat.taas.expr.TLessEquals;
import com.joa_ebert.apparat.taas.expr.TLessThan;
import com.joa_ebert.apparat.taas.expr.TModulo;
import com.joa_ebert.apparat.taas.expr.TMultiply;
import com.joa_ebert.apparat.taas.expr.TNegate;
import com.joa_ebert.apparat.taas.expr.TNot;
import com.joa_ebert.apparat.taas.expr.TShiftLeft;
import com.joa_ebert.apparat.taas.expr.TShiftRight;
import com.joa_ebert.apparat.taas.expr.TShiftRightUnsigned;
import com.joa_ebert.apparat.taas.expr.TStrictEquals;
import com.joa_ebert.apparat.taas.expr.TSubtract;
import com.joa_ebert.apparat.taas.toolkit.ITaasTool;
import com.joa_ebert.apparat.taas.toolkit.TaasToolkit;
import com.joa_ebert.apparat.taas.types.NumberType;
import com.joa_ebert.apparat.taas.types.TaasType;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class ConstantFolding implements ITaasTool
{
	private static final TaasBoolean TRUE = new TaasBoolean( true );
	private static final TaasBoolean FALSE = new TaasBoolean( false );

	private TaasConstant fold( final AbstractBinaryExpr binExp,
			final TaasBoolean lhs, final TaasBoolean rhs )
	{
		if( ( binExp instanceof TEquals ) || ( binExp instanceof TStrictEquals ) )
		{
			return new TaasBoolean( lhs.value == rhs.value );
		}

		return null;
	}

	private TaasConstant fold( final AbstractBinaryExpr binExp,
			final TaasNumeric lhs, final TaasNumeric rhs )
	{
		final TaasType type = TaasType.typeOf( lhs.getType(), rhs.getType() );

		final TaasNumeric lhs2 = (TaasNumeric)lhs.widen( type );
		final TaasNumeric rhs2 = (TaasNumeric)rhs.widen( type );

		if( binExp instanceof TAdd )
		{
			return lhs2.add( rhs2 );
		}
		else if( binExp instanceof TDivide )
		{
			if( lhs.getType() == NumberType.INSTANCE
					&& rhs.getType() == NumberType.INSTANCE )
			{
				return lhs2.divide( rhs2 );
			}
			else
			{
				final TaasNumeric lhs3 = (TaasNumeric)lhs
						.widen( NumberType.INSTANCE );

				final TaasNumeric rhs3 = (TaasNumeric)rhs
						.widen( NumberType.INSTANCE );

				final TaasNumeric result0 = lhs2.divide( rhs2 );
				final TaasNumber result1 = (TaasNumber)lhs3.divide( rhs3 );

				if( result0 instanceof TaasUInt )
				{
					final TaasUInt taasUInt = (TaasUInt)result0;

					if( taasUInt.value != result1.value )
					{
						return result1;
					}
					else
					{
						return result0;
					}
				}
				else if( result0 instanceof TaasInt )
				{
					final TaasInt taasInt = (TaasInt)result0;

					if( taasInt.value != result1.value )
					{
						return result1;
					}
					else
					{
						return result0;
					}
				}
				else
				{
					return result1;
				}
			}
		}
		else if( binExp instanceof TModulo )
		{
			return lhs2.modulo( rhs2 );
		}
		else if( binExp instanceof TMultiply )
		{
			return lhs2.multiply( rhs2 );
		}
		else if( binExp instanceof TSubtract )
		{
			return lhs2.subtract( rhs2 );
		}
		else if( binExp instanceof TBitAnd )
		{
			return lhs2.bitAnd( rhs2 );
		}
		else if( binExp instanceof TBitOr )
		{
			return lhs2.bitOr( rhs2 );
		}
		else if( binExp instanceof TBitXor )
		{
			return lhs2.bitXor( rhs2 );
		}
		else if( binExp instanceof TShiftLeft )
		{
			return lhs2.shiftLeft( rhs2 );
		}
		else if( binExp instanceof TShiftRight )
		{
			return lhs2.shiftRight( rhs2 );
		}
		else if( binExp instanceof TShiftRightUnsigned )
		{
			return lhs2.shiftRightUnsigned( rhs2 );
		}
		else if( binExp instanceof TEquals )
		{
			return lhs2.equals( rhs2 );
		}
		else if( binExp instanceof TGreaterEquals )
		{
			return lhs2.greaterEquals( rhs2 );
		}
		else if( binExp instanceof TGreaterThan )
		{
			return lhs2.greaterThan( rhs2 );
		}
		else if( binExp instanceof TLessEquals )
		{
			return lhs2.lessEquals( rhs2 );
		}
		else if( binExp instanceof TLessThan )
		{
			return lhs2.lessThan( rhs2 );
		}
		else if( binExp instanceof TStrictEquals )
		{
			return lhs2.strictEquals( rhs2 );
		}

		return null;
	}

	private TaasConstant fold( final AbstractUnaryExpr unaryExpr,
			final TaasBoolean rhs )
	{
		if( unaryExpr instanceof TNot )
		{
			return new TaasBoolean( !rhs.value );
		}

		return null;
	}

	private TaasConstant fold( final AbstractUnaryExpr unaryExpr,
			final TaasNumeric rhs )
	{
		if( unaryExpr instanceof TBitNot )
		{
			return rhs.bitNot();
		}
		else if( unaryExpr instanceof TDecrement )
		{
			return rhs.decrement();
		}
		else if( unaryExpr instanceof TIncrement )
		{
			return rhs.increment();
		}
		else if( unaryExpr instanceof TNegate )
		{
			return rhs.negate();
		}

		return null;
	}

	public boolean manipulate( final AbcEnvironment environment,
			final TaasMethod method )
	{
		boolean changed = TaasToolkit.phiCleanup( method );

		final List<TaasPhi> phiExprs = TaasToolkit.phisOf( method );

		for( final TaasPhi phiExpr : phiExprs )
		{
			if( phiExpr.values.size() == 1 )
			{
				TaasToolkit.replace( method, phiExpr,
						phiExpr.values.get( 0 ).value );
			}
		}

		try
		{
			final Iterator<TaasVertex> iter = method.code.vertexList()
					.listIterator();

			while( iter.hasNext() )
			{
				final TaasVertex vertex = iter.next();

				if( vertex.kind != VertexKind.Default )
				{
					continue;
				}

				final TaasValue value = vertex.value;

				final List<AbstractBinaryExpr> binExprs = new LinkedList<AbstractBinaryExpr>();

				TaasToolkit.searchAll( value, AbstractBinaryExpr.class,
						binExprs );

				for( final AbstractBinaryExpr binExpr : binExprs )
				{
					if( !binExpr.isConstant() )
					{
						continue;
					}

					TaasConstant constant = null;

					if( binExpr.lhs instanceof TaasNumeric
							&& binExpr.rhs instanceof TaasNumeric )
					{
						constant = fold( binExpr, (TaasNumeric)binExpr.lhs,
								(TaasNumeric)binExpr.rhs );
					}
					else if( binExpr.lhs instanceof TaasBoolean
							&& binExpr.rhs instanceof TaasBoolean )
					{
						constant = fold( binExpr, (TaasBoolean)binExpr.lhs,
								(TaasBoolean)binExpr.rhs );
					}

					if( null != constant )
					{
						TaasToolkit.replace( value, binExpr, constant );
						changed = true;
					}
				}

				final List<AbstractUnaryExpr> unaryExprs = new LinkedList<AbstractUnaryExpr>();

				TaasToolkit.searchAll( value, AbstractUnaryExpr.class,
						unaryExprs );

				for( final AbstractUnaryExpr unaryExpr : unaryExprs )
				{
					if( !unaryExpr.isConstant() )
					{
						continue;
					}

					TaasConstant constant = null;

					if( unaryExpr.rhs instanceof TaasNumeric )
					{
						constant = fold( unaryExpr, (TaasNumeric)unaryExpr.rhs );
					}
					else if( unaryExpr.rhs instanceof TaasBoolean )
					{
						constant = fold( unaryExpr, (TaasBoolean)unaryExpr.rhs );
					}

					if( null != constant )
					{
						TaasToolkit.replace( value, unaryExpr, constant );
						changed = true;
					}
				}

				if( !value.isConstant() )
				{
					continue;
				}

				if( value instanceof TaasPhi )
				{
					//
					// Fold phi nodes if the element of values is exactly one.
					// This means the phi is unnecessary.
					//

					final TaasPhi phiExpr = (TaasPhi)vertex.value;

					if( phiExpr.values.size() == 1 )
					{
						TaasToolkit.replace( method, phiExpr, phiExpr.values
								.get( 0 ).value );
					}
				}
				else if( value instanceof TIf )
				{
					//
					// Remove control flow expression if the condition is
					// constant. This will cut the edge for the unnecessary
					// flow. This will lead to dead code which can be eliminated
					// by a dead code elimination algorithm.
					//

					final TIf ifExpr = (TIf)vertex.value;

					switch( ifExpr.operator )
					{
						case True:
							if( ifExpr.lhs.equals( TRUE ) )
							{
								changed = remove( method, vertex,
										EdgeKind.False )
										|| changed;
							}
							else if( ifExpr.lhs == FALSE )
							{
								changed = remove( method, vertex, EdgeKind.True )
										|| changed;
							}
							break;

						case False:
							if( ifExpr.lhs.equals( FALSE ) )
							{
								changed = remove( method, vertex,
										EdgeKind.False )
										|| changed;
							}
							else if( ifExpr.lhs == TRUE )
							{
								changed = remove( method, vertex, EdgeKind.True )
										|| changed;
							}
							break;
					}
				}
			}
		}
		catch( final ControlFlowGraphException e )
		{
			throw new TaasException( e );
		}

		if( TaasCompiler.SHOW_ALL_TRANSFORMATIONS && changed )
		{
			TaasToolkit.debug( "ConstantFolding", method );
		}

		return changed;
	}

	private boolean remove( final TaasMethod method, final TaasVertex vertex,
			final EdgeKind kind ) throws ControlFlowGraphException
	{
		final List<TaasEdge> edges = method.code.outgoingOf( vertex );

		for( final TaasEdge edge : edges )
		{
			if( edge.kind == kind )
			{
				method.code.remove( edge );

				TaasToolkit.remove( method, vertex );

				return true;
			}
		}

		return false;
	}
}
