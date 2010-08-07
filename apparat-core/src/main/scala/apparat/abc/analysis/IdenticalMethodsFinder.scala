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
package apparat.abc.analysis

import apparat.abc._
import apparat.log.SimpleLog

/**
 * @author Joa Ebert
 */
object IdenticalMethodsFinder extends (Abc => Map[AbcMethod, AbcMethod]) with SimpleLog {
	override def apply(abc: Abc) = {
		if(abc.bytecodeAvailable) {
			log.warning("Only raw bytecode will be checked for identity.")
		}

		val methods = abc.methods
		val n = methods.length

		var i = 0
		var j = 0

		//
		// The mapping source -> target.
		//
		// A source is a method which should be replaced by
		// a given target.
		//

		var map = Map.empty[AbcMethod, AbcMethod]

		while(i < n) {
			if(!(map contains methods(i))) {
				j = i + 1

				while(j < n) {
					if(methodsEqual(methods(i), methods(j))) {
						map += methods(j) -> methods(i)
					}

					j += 1
				}
			}

			i += 1
		}

		map
	}

	private def methodsEqual(a: AbcMethod, b: AbcMethod): Boolean = {
		if(a.anonymous || b.anonymous) {
			false
		} else if(a == null || b == null) {
			a == b
		} else {
			if(a.returnType == b.returnType && a.parameters.length == b.parameters.length &&
				a.name == b.name && a.needsArguments == b.needsArguments &&
				a.needsActivation == b.needsActivation && a.needsRest == b.needsRest &&
				a.hasOptionalParameters == b.hasOptionalParameters && a.ignoreRest == b.ignoreRest &&
				a.isNative == b.isNative && a.setsDXNS == b.setsDXNS && a.hasParameterNames == b.hasParameterNames) {

				val n = a.parameters.length
				var i = 0

				while(i < n) {
					if(!paramsEqual(a.parameters(i), b.parameters(i))) {
						return false
					}

					i += 1
				}

				bodiesEqual(a.body, b.body)
			} else {
				false
			}
		}
	}

	private def paramsEqual(a: AbcMethodParameter, b: AbcMethodParameter): Boolean = {
		if(a == null || b == null) {
			a == b
		} else {
			a.typeName == b.typeName && a.name == b.name && a.optional == b.optional &&
			a.optionalType == b.optionalType && a.optionalVal == b.optionalVal
		}
	}

	private def bodiesEqual(a: Option[AbcMethodBody], b: Option[AbcMethodBody]): Boolean = {
		if(a == null || b == null) {
			a == b
		} else {
			if(a.isDefined && b.isDefined) {
				val bodyA = a.get
				val bodyB = b.get

				if(bodyA.maxStack == bodyB.maxStack && bodyA.localCount == bodyB.localCount &&
					bodyA.initScopeDepth == bodyB.initScopeDepth &&
					bodyA.maxScopeDepth == bodyB.maxScopeDepth && bodyA.traits.length == bodyB.traits.length &&
					bodyA.exceptions.length == bodyB.exceptions.length && bodyA.code.length == bodyB.code.length) {
					val n = bodyA.code.length
					val ca = bodyA.code
					val cb = bodyB.code
					var i = 0

					while(i < n) {
						if(ca(i) != cb(i)) {
							return false
						}

						i += 1
					}

					//
					// No need to check for traits here. The bytecode is absolutely identical.
					// Also different catch handlers would not make any sense in that case.
					//

					true
				} else {
					false
				}
			} else {
				a.isDefined == b.isDefined
			}
		}
	}
}