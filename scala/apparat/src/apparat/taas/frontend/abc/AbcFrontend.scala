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
package apparat.taas.frontend.abc

import apparat.taas.frontend.TaasFrontend
import apparat.taas.ast.TaasVisibility._
import apparat.taas.ast._
import collection.mutable.ListBuffer
import apparat.abc._
import util.DynamicVariable

/**
 * @author Joa Ebert
 */
class AbcFrontend(main: Abc, libraries: List[Abc]) extends TaasFrontend {
	private val ast: TaasAST = TaasAST(ListBuffer.empty)
	private var abc: Abc = _
	private var unit: TaasUnit = _

	override def getAST = {
		val target = TaasTarget(ListBuffer.empty)
		ast.children append target

		parseABC(target, main)

		for(library <- libraries) {
			val lib = TaasLibrary(ListBuffer.empty)
			ast.children append lib
			parseABC(lib, library)
		}

		ast.init()
	}

	private def parseABC(unit: TaasUnit, abc: Abc) = {
		this.abc = abc
		this.unit = unit
		
		try {
			abc.scripts foreach parseScript
		} finally {
			this.abc = null
			this.unit = null
		}
	}

	private def parseScript(script: AbcScript) = {
		script.traits foreach {
			case AbcTraitClass(name, index, nominalType, metadata) => {
				val namespace = getNS(nominalType.inst.name.namespace.name)
				namespace.definitions += parseNominal(nominalType)
			}
		}
	}

	private def parseNominal(nominal: AbcNominalType) = {
		if(nominal.inst.isInterface) {
			TaasInterface(nominal.inst.name.name, Public, ListBuffer.empty)
		} else {
			val instMethods = nominal.inst.traits partialMap {
				case methodTrait: AbcTraitAnyMethod => {
					parseMethod(methodTrait, false)
				}
			}

			val classMethods = nominal.klass.traits partialMap {
				case methodTrait: AbcTraitAnyMethod => {
					parseMethod(methodTrait, true)
				}
			}

			var methods = ListBuffer.empty[TaasMethod]
			methods ++= instMethods
			methods ++= classMethods

			TaasClass(
				nominal.inst.name.name,
				Public,
				nominal.inst.isFinal,
				!nominal.inst.isSealed,
				parseMethod(nominal.klass.init, true, true),
				parseMethod(nominal.inst.init, false, true),
				methods,
				ListBuffer.empty)
		}
	}

	private def parseMethod(methodTrait: AbcTraitAnyMethod, isStatic: Boolean): TaasMethod = {
		TaasMethod(methodTrait.name.name, Public, isStatic, methodTrait.isFinal, methodTrait.method.isNative)
	}

	private def parseMethod(method: AbcMethod, isStatic: Boolean, isFinal: Boolean): TaasMethod = {
		TaasMethod(method.name, Public, isStatic, isFinal, method.isNative)
	}

	private def getNS(namespace: Symbol) = {
		unit.children find {
			case TaasNamespace(name, _) if namespace == name => true
			case TaasNamespace(_, _) => false
		} match {
			case Some(namespace) => namespace
			case None => {
				val result = TaasNamespace(namespace, ListBuffer.empty)
				unit.children prepend result
				result
			}
		}
	}
}