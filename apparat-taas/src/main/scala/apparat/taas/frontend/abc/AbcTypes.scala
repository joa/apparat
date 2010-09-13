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

import apparat.abc._
import apparat.taas.ast._
import collection.mutable.HashMap

/**
 * @author Joa Ebert
 */
protected[abc] object AbcTypes {
	private var cache = HashMap.empty[Symbol, TaasType]
	private val EMPTY = Symbol("")

	def fromQName(name: Symbol, namespace: AbcNamespace)(implicit ast: TaasAST): TaasType = {
		namespace match {
			case
				AbcNamespace(5, Symbol("BitmapData.as$233")) |
				AbcNamespace(5, Symbol("DynamicPropertyOutput.as$208")) =>
				cache get name match {
					case Some(x) => x
					case None =>
						val result = AbcType(ast, name, AbcNamespace(22, Symbol("")))
						cache += name -> result
						result
				}
			case _ =>
				val qname = if(namespace.name == EMPTY) { name } else {
					Symbol(namespace.name.name+"."+name.name)
				}
				cache get qname match {
					case Some(x) => x
					case None =>
						val result = AbcType(ast, name, namespace)
						cache += qname -> result
						result
				}
		}
	}

	def fromQName(qname: AbcQName)(implicit ast: TaasAST): TaasType = fromQName(qname.name, qname.namespace)

	def fromTypename(name: AbcQName, parameters: Array[AbcName])(implicit ast: TaasAST): TaasType = {
		val qname = if(name.namespace.name == EMPTY) { name.name } else {
			Symbol(name.namespace.name.name+"."+name.name.name)
		}
		cache get qname match {
			case Some(x) => x
			case None =>
				val result = AbcParameterizedType(ast, name, parameters)
				cache += qname -> result
				result
		}
	}

	def fromTypename(typename: AbcTypename)(implicit ast: TaasAST): TaasType = fromTypename(typename.name, typename.parameters)

	def name2type(name: AbcName)(implicit ast: TaasAST): TaasType = {
		if(name == AbcConstantPool.EMPTY_NAME) TaasAnyType
		else {
			name match {
				case AbcQName(name, namespace) => {
					if(name == 'void && namespace.name.name.length == 0) TaasVoidType
					else if(name == 'int && namespace.name.name.length == 0) TaasIntType
					else if(name == 'uint && namespace.name.name.length == 0) TaasLongType
					else if(name == 'Number && namespace.name.name.length == 0) TaasDoubleType
					else if(name == 'String && namespace.name.name.length == 0) TaasStringType
					else if(name == 'Boolean && namespace.name.name.length == 0) TaasBooleanType
					else if(name == 'Function && namespace.name.name.length == 0) TaasFunctionType
					else if(name == 'Object && namespace.name.name.length == 0) TaasObjectType
					else AbcTypes fromQName (name, namespace)
				}
				case AbcTypename(name, parameters) => AbcTypes fromTypename (name, parameters)
				case AbcMultiname(name, nsset) => nsset.set.length match {
					case 1 => AbcTypes fromQName (name, nsset.set(0))
					case n => AbcTypes fromQName (name, nsset.set(1))
				}
				case _ => error("Unexpected name: " + name)
			}
		}
	}
}

protected[abc] case class AbcType(ast: TaasAST, name: Symbol, namespace: AbcNamespace) extends TaasNominalType {
	/*override def equals(that: Any) = that match {
		case AbcType(_, thatName, thatNamespace) => name == thatName && namespace == thatNamespace
		case thatNominal: TaasNominalType => thatNominal.nominal == nominal 
		case _ => false
	}*/

	if(namespace.name.name.length == 0) name match {
		case 'int => error("Use TaasIntType instead of nominal type for int.")
		case 'uint => error("Use TaasLongType instead of nominal type for uint.")
		case 'Number => error("Use TaasDoubleType instead of nominal type for Number.")
		case 'String => error("Use TaasStringType instead of nominal type for String.")
		case 'Boolean => error("Use TaasBooleanType instead of nominal type for Boolean.")
		case 'Function => error("Use TaasFunctionType instead of nominal type for Function.")
		case 'Object => error("Use TaasObjectType instead of nominal type for Object.")
		case _ =>
	}

	lazy val nominal: TaasNominal = {
		def search(ast: TaasAST): TaasNominal = {
			for(unit <- ast.units;
				pckg <- unit.packages if pckg.name == namespace.name) {
				pckg.definitions find (_.name == name) match {
					case Some(definition) => definition match {
						case nominal: TaasNominal => return nominal
						case _ => error("Expected nominal type, got "+definition+".")
					}
					case None => false
				}
			}

			error("Missing definition " + name + " in " + namespace)
		}

		search(ast)
	}
}

protected[abc] case class AbcParameterizedType(ast: TaasAST, name: AbcQName, params: Array[AbcName]) extends TaasParameterizedType {
	lazy val nominal: TaasNominal = AbcTypes.fromQName(name)(ast) match {
		case n: TaasNominalType => n.nominal
		case other => error("TaasNominalType expected, got "+other+".")
	}
	
	lazy val parameters: List[TaasType] = params map {
		case qname: AbcQName => AbcTypes.name2type(qname)(ast)
		case typename: AbcTypename => AbcTypes.name2type(typename)(ast)
		case other => error("Unexpected name: " + other)
	} toList
}