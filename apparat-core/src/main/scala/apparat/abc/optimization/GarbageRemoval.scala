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
package apparat.abc.optimization

import apparat.abc.Abc
import apparat.bytecode.optimization.{PeepholeOptimizations, BlockMerge}
import apparat.abc.analysis.{AbcConstantPoolBuilder, QuickAbcConstantPoolBuilder}

/**
 * @author Joa Ebert
 */
object GarbageRemoval extends (Abc => Unit) {
	override def apply(abc: Abc): Unit = {
		for(nominal <- abc.types) {
			if(nominal.inst.protectedNs.isDefined) {
				nominal.inst.protectedNs = None
			}
		}

		for {
			method <- abc.methods
			body <- method.body
		} {
			body.maxScopeDepth = body.maxScopeDepth - body.initScopeDepth
			body.initScopeDepth = 0
		}

		val wasAvailable = abc.bytecodeAvailable

		if(!wasAvailable) {
			abc.loadBytecode()
		}

		for {
			method <- abc.methods
			body <- method.body
			code <- body.bytecode
		} {
			PeepholeOptimizations(code)
			body.bytecode = Some(BlockMerge(code)._2)
		}

		abc.cpool = AbcConstantPoolBuilder using abc

		if(!wasAvailable) {
			abc.saveBytecode()
		}
	}
}