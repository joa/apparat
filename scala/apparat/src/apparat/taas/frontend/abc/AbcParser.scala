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

import apparat.taas.ast._
import collection.mutable.ListBuffer
import apparat.abc._

/**
 * @author Joa Ebert
 */
class AbcParser(implicit ast: TaasAST, abc: Abc, unit: TaasUnit) {
	def parseAbc(): Unit = parseAbc(unit, abc)
	def parseAbc(abc: Abc): Unit = parseAbc(unit, abc)
	def parseAbc(unit: TaasUnit, abc: Abc): Unit = {
		abc.scripts foreach parseScript
	}

	def parseScript(script: AbcScript) = {
		script.traits foreach {
			case AbcTraitClass(name, _, nominalType, _) => {
				val pckg = packageOf(nominalType.inst.name.namespace.name)
				pckg.definitions += parseNominal(nominalType)
			}
			case anyMethod: AbcTraitAnyMethod => {
				val pckg = packageOf(anyMethod.name.namespace.name)
				pckg.definitions += parseMethod(anyMethod, true)
			}
			case anySlot: AbcTraitAnySlot => {
				val pckg = packageOf(anySlot.name.namespace.name)
				pckg.definitions += parseSlot(anySlot, true)
			}
		}
	}

	def parseNominal(nominal: AbcNominalType) = {
		val base = nominal.inst.base match {
			case Some(base) => Some(name2type(base))
			case None => None
		}

		var methods = ListBuffer.empty[TaasMethod]

		methods ++= nominal.inst.traits partialMap {
			case methodTrait: AbcTraitAnyMethod => {
				parseMethod(methodTrait, false)
			}
		}

		if(nominal.inst.isInterface) {
			TaasInterface(
				nominal.inst.name.name,
				nominal.inst.name.namespace,
				base,
				methods)
		} else {
			val fields = ListBuffer.empty[TaasField]

			methods ++= nominal.inst.traits partialMap {
				case methodTrait: AbcTraitAnyMethod => {
					parseMethod(methodTrait, false)
				}
			}

			methods ++= nominal.klass.traits partialMap {
				case methodTrait: AbcTraitAnyMethod => {
					parseMethod(methodTrait, true)
				}
			}

			fields ++= nominal.inst.traits partialMap {
				case slotTrait: AbcTraitAnySlot => {
					parseSlot(slotTrait, false)
				}
			}

			fields ++= nominal.klass.traits partialMap {
				case slotTrait: AbcTraitAnySlot => {
					parseSlot(slotTrait, true)
				}
			}

			TaasClass(nominal.inst.name.name,
				nominal.inst.name.namespace,
				nominal.inst.isFinal,
				!nominal.inst.isSealed,
				parseMethod(nominal.klass.init, true, true),
				parseMethod(nominal.inst.init, false, true),
				base,
				methods,
				fields)
		}
	}

	def parseMethod(methodTrait: AbcTraitAnyMethod, isStatic: Boolean): TaasMethod = {
		TaasMethod(
			methodTrait.name.name,
			methodTrait.name.namespace,
			methodTrait.method.returnType,
			parseParameters(methodTrait.method.parameters),
			isStatic,
			methodTrait.isFinal,
			methodTrait.method.isNative)
	}

	def parseMethod(method: AbcMethod, isStatic: Boolean, isFinal: Boolean): TaasMethod = {
		TaasMethod(
			method.name,
			TaasPublic,
			method.returnType,
			parseParameters(method.parameters),
			isStatic,
			isFinal,
			method.isNative)
	}

	def parseParameters(parameters: Array[AbcMethodParameter]) = ListBuffer((parameters map parseParameter): _*)

	def parseParameter(parameter: AbcMethodParameter) = TaasParameter(parameter.typeName, parameter.optionalVal)

	def parseSlot(slotTrait: AbcTraitAnySlot, isStatic: Boolean) = slotTrait match {
		case AbcTraitSlot(name, _, typeName, _, _, _) => TaasSlot(name.name, name.namespace, typeName, isStatic)
		case AbcTraitConst(name, _, typeName, _, _, _) => TaasConstant(name.name, name.namespace, typeName, isStatic)
	}

	private def packageOf(namespace: Symbol) = {
		unit.children find {
			case TaasPackage(name, _) if namespace == name => true
			case TaasPackage(_, _) => false
		} match {
			case Some(namespace) => namespace
			case None => {

				val result = TaasPackage(namespace, ListBuffer.empty)
				unit.children prepend result
				result
			}
		}
	}

	private implicit def ns2ns(namespace: AbcNamespace): TaasNamespace = {
		namespace.kind match {
			case AbcNamespaceKind.Package => TaasPublic
			case AbcNamespaceKind.Explicit => error("Explicit")
			case AbcNamespaceKind.Namespace => TaasExplicit(namespace.name)
			case AbcNamespaceKind.PackageInternal => TaasInternal
			case AbcNamespaceKind.Private => TaasPrivate
			case AbcNamespaceKind.Protected => TaasProtected
			case AbcNamespaceKind.StaticProtected => TaasProtected
		}
	}

	private implicit def name2type(name: AbcName): TaasType = {
		if(name == AbcConstantPool.EMPTY_NAME) {
			TaasAny
		} else {
			name match {
				case AbcQName(name, namespace) => {
					if(name == 'void && namespace.name.name.length == 0) {
						TaasVoid
					} else {
						AbcTypes fromQName (name, namespace)
					}
				}
				case AbcTypename(name, parameters) => {
						AbcTypes fromTypename (name, parameters)
				}
				case _ => error("Unexpected name: " + name)
			}
		}
	}
}