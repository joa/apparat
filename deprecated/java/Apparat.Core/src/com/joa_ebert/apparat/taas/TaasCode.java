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

package com.joa_ebert.apparat.taas;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.ControlFlowGraphException;
import com.joa_ebert.apparat.controlflow.EdgeKind;
import com.joa_ebert.apparat.controlflow.export.DOTExporter;

/**
 * 
 * @author Joa Ebert
 * 
 */
public final class TaasCode extends ControlFlowGraph<TaasVertex, TaasEdge>
{
	private TaasVertex lastInserted = null;
	private TaasEdge lastEdge = null;
	private EdgeKind nextKind = EdgeKind.Default;

	@Override
	public boolean add( final TaasEdge edge ) throws ControlFlowGraphException
	{
		nextKind = EdgeKind.Default;

		return super.add( edge );
	}

	public TaasVertex add( final TaasValue value ) throws TaasException
	{
		final TaasVertex vertex = new TaasVertex( value );

		try
		{
			add( vertex );

			lastEdge = new TaasEdge( lastInserted, vertex, nextKind );

			add( lastEdge );

			lastInserted = vertex;
		}
		catch( final ControlFlowGraphException ex )
		{
			throw new TaasException( ex );
		}

		return vertex;
	}

	void connectIfNeccessary( final TaasVertex to )
	{
		if( null == to )
		{
			return;
		}

		try
		{
			final List<TaasEdge> outgoingOf = outgoingOf( lastInserted );

			for( final TaasEdge e : outgoingOf )
			{
				if( e.endVertex == to && e.kind == nextKind )
				{
					return;
				}
			}

			lastEdge = new TaasEdge( lastInserted, to, nextKind );

			add( lastEdge );
		}
		catch( final ControlFlowGraphException exception )
		{
			throw new TaasException( exception );
		}
	}

	public String debug()
	{
		final StringBuilder builder = new StringBuilder( "Code:\n" );
		final StringWriter writer = new StringWriter();
		// final StringWriter writer2 = new StringWriter();
		final DOTExporter<TaasVertex, TaasEdge> exporter = new DOTExporter<TaasVertex, TaasEdge>(
				new TaasVertex.LabelProvider(), new TaasEdge.LabelProvider() );
		// final DOTExporter<BasicBlock<TaasVertex>,
		// Edge<BasicBlock<TaasVertex>>> exporter2 = new
		// DOTExporter<BasicBlock<TaasVertex>, Edge<BasicBlock<TaasVertex>>>(
		// new BasicBlock.LabelProvider<TaasVertex>() );

		exporter.export( new PrintWriter( writer ), this );

		// try
		// {
		// exporter2.export( new PrintWriter( writer2 ), this.toBlockGraph() );
		// }
		// catch( final ControlFlowGraphException e )
		// {
		// e.printStackTrace();
		// }
		//
		builder.append( writer );
		// builder.append( "\nBasic blocks:\n" );
		// builder.append( writer2 );
		//
		// return builder.toString();

		return builder.toString();
	}

	public void debug( final OutputStream output )
	{
		debug( new PrintWriter( output ) );
	}

	public void debug( final PrintWriter printWriter )
	{
		final DOTExporter<TaasVertex, TaasEdge> exporter = new DOTExporter<TaasVertex, TaasEdge>(
				new TaasVertex.LabelProvider(), new TaasEdge.LabelProvider() );

		exporter.export( printWriter, this );

		printWriter.flush();
	}

	TaasEdge getLastEdge()
	{
		return lastEdge;
	}

	TaasVertex getLastInserted()
	{
		return lastInserted;
	}

	EdgeKind getNextKind()
	{
		final EdgeKind result = nextKind;

		if( result != EdgeKind.Default )
		{
			nextKind = EdgeKind.Default;
		}

		return result;
	}

	void setLastEdge( final TaasEdge value )
	{
		lastEdge = value;
	}

	void setLastInserted( final TaasVertex value )
	{
		lastInserted = value;
	}

	void setNextKind( final EdgeKind value )
	{
		nextKind = value;
	}
}
