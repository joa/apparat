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

/**
 * @author Joa Ebert
 */
protected[abc] object AbcTypes {
	def fromQName(name: Symbol, namespace: AbcNamespace)(implicit ast: TaasAST): AbcType = new AbcType(ast, name, namespace)
	def fromQName(qname: AbcQName)(implicit ast: TaasAST): AbcType = fromQName(qname.name, qname.namespace)

	def fromTypename(name: AbcQName, parameters: Array[AbcName])(implicit ast: TaasAST): AbcParameterizedType = new AbcParameterizedType(ast, name, parameters)
	def fromTypename(typename: AbcTypename)(implicit ast: TaasAST): AbcParameterizedType = fromTypename(typename.name, typename.parameters)
}

protected[abc] class AbcType(ast: TaasAST, name: Symbol, namespace: AbcNamespace) extends TaasNominalType {
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

protected[abc] class AbcParameterizedType(ast: TaasAST, name: AbcQName, params: Array[AbcName]) extends TaasParameterizedType {
	lazy val nominal: TaasNominal = AbcTypes.fromQName(name)(ast).nominal
	lazy val parameters: List[TaasNominalType] = params map {
		case AbcQName(name, namespace) => AbcTypes.fromQName(name, namespace)(ast)
		case AbcTypename(name, parameters) => AbcTypes.fromTypename(name, parameters)(ast)
		case other => error("Unexpected name: " + other)
	} toList
}