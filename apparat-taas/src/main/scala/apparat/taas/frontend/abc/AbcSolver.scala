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
import apparat.log.SimpleLog

/**
 * @author Joa Ebert
 */
object AbcSolver extends SimpleLog {
	def getProperty(`type`: TaasType, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = {
		`type` match {
			case nominalType: TaasNominalType => property(nominalType.nominal, name, 0)
			case _ => error("Nominal type expected, got "+`type`+" when searching "+name+".")
		}
	}

	def getProperty(`type`: TaasNominalType, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = property(`type`.nominal, name, 0)

	def setProperty(`type`: TaasType, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = {
		`type` match {
			case nominalType: TaasNominalType => property(nominalType.nominal, name, 1)
			case _ => error("Nominal type expected, got "+`type`+".")
		}
	}

	def setProperty(`type`: TaasNominalType, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = property(`type`.nominal, name, 1)

	def property(`type`: TaasType, name: AbcName, numParameters: Int)(implicit ast: TaasAST): Option[TaasDefinition] = {
		`type` match {
			case nominalType: TaasNominalType => property(nominalType.nominal, name, numParameters)
			case _ => error("Nominal type expected, got "+`type`+" while searching "+name+" with "+numParameters+" parameter(s).")
		}
	}

	def property(`type`: TaasNominalType, name: AbcName, numParameters: Int)(implicit ast: TaasAST): Option[TaasDefinition] = property(`type`.nominal, name, numParameters)

	def property(nominal: TaasNominal, name: AbcName, numParameters: Int)(implicit ast: TaasAST): Option[TaasDefinition] = {
		nominal match {
			case TaasInterface(_, _, base, methods, _) => {
				name match {
					case AbcQName(symbol, _) => methods find { m => m.name == symbol && m.parameters.length == numParameters } match {
						case Some(result) => Some(result)
						case None => {
							methods count { m => m.name == symbol } match {
								case 1 => methods find { m => m.name == symbol }
								case _ => base match {
									case Some(base) => property(base, name, numParameters)
									case None => None
								}
							}
						}
					}
					case _ => error("QName expected, got "+name+".")
				}
			}
			case TaasClass(_, _, _, _, _, _, base, methods, fields, _) => {
				name match {
					case AbcQName(symbol, _) => methods find { m => m.name == symbol && m.parameters.length == numParameters } match {
						case r @ Some(_) => r
						case None => {
							methods count { m => m.name == symbol } match {
								case 1 => methods find { m => m.name == symbol }
								case _ => fields find { _.name == symbol } match {
									case Some(result) => Some(result)
									case None => base match {
										case Some(base) => property(base, name, numParameters)
										case None => None
									}
								}
							}
						}
					}
					case _ => error("QName expected, got "+name+".")
				}
			}

			case TaasFunction(_, _, method) => Some(method)
		}
	}

	def property(nominal: TaasNominal, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = {
		def getProperty(`type`: TaasType, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = {
			`type` match {
				case nominalType: TaasNominalType => property(nominalType.nominal, name)
				case TaasObjectType => None
				case _ => error("Unexpected type, got "+`type`+".")
			}
		}
		
		nominal match {
			case TaasInterface(_, _, base, methods, _) => {
				name match {
					case AbcQName(symbol, _) => methods find { m => m.name == symbol } match {
						case Some(result) => Some(nominal)
						case None => base match {
							case Some(base) => getProperty(base, name)
							case None => None
						}
					}
					case _ => error("QName expected, got "+name+".")
				}
			}
			case TaasClass(_, _, _, _, _, _, base, methods, fields, _) => {
				name match {
					case AbcQName(symbol, _) => methods find { m => m.name == symbol } match {
						case Some(result) => Some(nominal)
						case None => fields find { _.name == symbol } match {
							case Some(result) => Some(nominal)
							case None => base match {
								case Some(base) => getProperty(base, name)
								case None => None
							}
						}
					}
					case _ => error("QName expected, got "+name+".")
				}
			}

			case TaasFunction(_, _, method) => Some(method)
		}
	}

	def getLexical(scope: TaasType, static: Boolean, name: AbcName)(implicit ast: TaasAST): Option[TaasDefinition] = {
		scope match {
			case nominalType: TaasNominalType => getLexical(nominalType.nominal, static, name)
			case TaasObjectType => name match {
				case qname: AbcQName => Some(AbcTypes.fromQName(qname).nominal)
				case _ => error("QName expected, got "+name+".")
			}
			case other => error("Nominal type expected, got "+other+".")
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