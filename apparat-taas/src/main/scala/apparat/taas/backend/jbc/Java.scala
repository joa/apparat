/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.taas.backend.jbc

import apparat.taas.ast._
import org.objectweb.asm.{Opcodes => JOpcodes}
import collection.mutable.ListBuffer

/**
 * @author Joa Ebert
 */
protected[jbc] object Java {
	@inline
	def nameOf(qualifiedName: String): String = {
		val name = qualifiedName.replaceAll("\\.", "\\/")
		if(-1 == (name indexOf '/')) {
			"jitb/lang/"+name
		} else {
			name
		}
	}

	@inline
	def liftToplevel(qualifiedName: String): String = qualifiedName indexOf '.' match {
		case -1 => "jitb.lang."+ qualifiedName
		case _ => qualifiedName
	}

	@inline
	def nameOf(`type`: TaasType): String = `type` match {
		case TaasAnyType | TaasObjectType => "jitb/lang/Object"
		case TaasVoidType => error("Void has no name.")
		case TaasBooleanType => "java/lang/Boolean"
		case TaasDoubleType => "java/lang/Double"
		case TaasIntType => "java/lang/Integer"
		case TaasLongType => "java/lang/Long"
		case TaasStringType => "java/lang/String"
		case TaasFunctionType => "jitb/lang/closure/Function"
		case t: TaasNominalType => nameOf(t.nominal.qualifiedName)
	}

	@inline
	def typeOf(`type`: TaasType) = `type` match {
		case TaasAnyType | TaasObjectType => "Ljitb/lang/Object;"
		case TaasVoidType => "V"
		case TaasBooleanType => "Z"
		case TaasDoubleType => "D"
		case TaasIntType => "I"
		case TaasLongType => "J"
		case TaasStringType => "Ljava/lang/String;"
		case TaasFunctionType => "Ljitb/lang/closure/Function;"
		case t: TaasNominalType => "L"+nameOf(t.nominal.qualifiedName)+";"
	}

	@inline
	def visibilityOf(withNamespace: {def namespace: TaasNamespace}) = JOpcodes.ACC_PUBLIC/*withNamespace.namespace match {
		case TaasPublic => JOpcodes.ACC_PUBLIC
		case TaasInternal => JOpcodes.ACC_PUBLIC
		case TaasProtected => JOpcodes.ACC_PROTECTED
		case TaasPrivate => JOpcodes.ACC_PRIVATE
		case TaasExplicit(_) => JOpcodes.ACC_PUBLIC
	}*/

	@inline
	def ownerOf(definition: TaasDefinition) = definition.parent match {
		case Some(parent) => parent match {
			case p: TaasPackage => definition.qualifiedName
			case k: TaasClass => k.qualifiedName
			case i: TaasInterface => i.qualifiedName
			case f: TaasFunction => f.qualifiedName
			case _ => error("Unexpected parent "+parent+".")
		}
		case None => error("No parent for "+definition+".")
	}

	@inline
	def nameOfOwnerOf(definition: TaasDefinition) = nameOf(ownerOf(definition))

	@inline
	def methodDesc(returnType: String, parameters: ListBuffer[TaasParameter]): String = "("+(parameters map { _.`type` } map { typeOf } mkString "")+")"+returnType

	@inline
	def methodDesc(returnType: TaasType, parameters: ListBuffer[TaasParameter]): String = methodDesc(typeOf(returnType), parameters)

	@inline
	def methodDesc(method: TaasMethod): String = methodDesc(typeOf(method.`type`), method.parameters)

}
