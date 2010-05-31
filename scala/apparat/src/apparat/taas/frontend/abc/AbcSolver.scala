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
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.taas.frontend.abc

import apparat.taas.ast._
import apparat.abc.{AbcNamespace, AbcNominalType, AbcQName, AbcName}

/**
 * @author Joa Ebert
 */
object AbcSolver {
	def getProperty(`type`: TaasType, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = {
		`type` match {
			case nominalType: TaasNominalType => getProperty(nominalType.nominal, name)
			case _ => error("Nominal type expected.")
		}
	}

	def getProperty(`type`: TaasNominalType, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = getProperty(`type`.nominal, name)

	def getProperty(nominal: TaasNominal, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = nominal match {
		case TaasInterface(_, _, base, methods, _) => {
			name match {
				case AbcQName(symbol, _) => methods find { _.name == symbol } match {
					case Some(result) => Some(result)
					case None => base match {
						case Some(base) => getProperty(base, name)
						case None => None
					}
				}
				case _ => error("QName expected.")
			}
		}
		case TaasClass(_, _, _, _, _, _, base, methods, fields, _) => {
			name match {
				case AbcQName(symbol, _) => methods find { _.name == symbol } match {
					case Some(result) => Some(result)
					case None => fields find { _.name == symbol } match {
						case Some(result) => Some(result)
						case None => base match {
							case Some(base) => getProperty(base, name)
							case None => None
						}
					}
				}
				case _ => error("QName expected.")
			}
		}

		case TaasFunction(_, _, method) => Some(method)
	}

	def getLexical(scope: TaasType, static: Boolean, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = {
		scope match {
			case nominalType: TaasNominalType => getLexical(nominalType.nominal, static, name)
			case _ => error("Nominal type expected.")
		}
	}

	def getLexical(scope: TaasNominalType, static: Boolean, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = {
		getLexical(scope.nominal, static, name)
	}

	def getLexical(scope: TaasNominal, static: Boolean, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = name match {
		case qname: AbcQName => {
			qname.namespace match {
				case AbcNamespace(_, Symbol("")) => scope match {
					case TaasClass(_, _, _, _, _, _, base, methods, fields, _) => {
						methods find { method => method.name == qname.name && method.isStatic == static } match {
							case Some(result) => Some(result)
							case None => fields find { field => field.name == qname.name && field.isStatic == static } match {
								case Some(result) => Some(result)
								case None => base match {
									case Some(base) => getLexical(base, static, name)
									case None => None
								}
							}
						}
					}
					case _ => error("TaasClass expected.")
				}
				case _ => Some((AbcTypes fromQName qname).nominal) 
			}
		}
		case _ => error("QName expected.")
	}
}