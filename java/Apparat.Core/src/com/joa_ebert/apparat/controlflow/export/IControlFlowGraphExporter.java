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

package com.joa_ebert.apparat.controlflow.export;

import java.io.OutputStream;
import java.io.PrintWriter;

import com.joa_ebert.apparat.controlflow.ControlFlowGraph;
import com.joa_ebert.apparat.controlflow.Edge;
import com.joa_ebert.apparat.controlflow.Vertex;

/**
 * 
 * @author Joa Ebert
 * 
 */
public interface IControlFlowGraphExporter<V extends Vertex, E extends Edge<V>>
{
	void export( final OutputStream output, final ControlFlowGraph<V, E> graph );

	void export( final PrintWriter writer, final ControlFlowGraph<V, E> graph );
}
