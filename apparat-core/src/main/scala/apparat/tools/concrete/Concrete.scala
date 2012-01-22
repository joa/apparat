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
package apparat.tools.concrete

import apparat.utils.TagContainer
import apparat.abc._
import apparat.tools.{ApparatConfiguration, ApparatTool, ApparatApplication}
import java.io.{File => JFile}
import scala.collection.immutable.Stack
import scala.annotation.tailrec
import apparat.swf.{DoABC, SwfTags}

object Concrete {
	def main(args: Array[String]): Unit = ApparatApplication(new ConcreteTool, args)

	class ConcreteTool extends ApparatTool {
		var libraries = List.empty[JFile]

		var cache = Map.empty[AbcQName, AbcNominalType]

		var abcs = List.empty[Abc]

		override def name = "Concrete"

		override def help = "  -i [file0"+JFile.pathSeparatorChar+"file1"+JFile.pathSeparatorChar+"..."+JFile.pathSeparatorChar+"fileN]	Input file(s)"

		override def configure(config: ApparatConfiguration): Unit = configure(ConcreteConfigurationFactory fromConfiguration config)

		def configure(config: ConcreteConfiguration): Unit = {
			libraries = config.libraries
		}

		override def run() = {
			@tailrec def buildStack(nominalType: AbcNominalType, stack: Stack[AbcNominalType]): Stack[AbcNominalType] = {
				nominalType.inst.base match {
					case Some(base) => base match {
						case qname: AbcQName => buildStack(lookup(qname), stack push nominalType)
						case _ => error("Unexpected name " + base + ".")
					}
					case None => stack push nominalType
				}
			}

			SwfTags.tagFactory = (kind: Int) => kind match {
				case SwfTags.DoABC => Some(new DoABC)
				case SwfTags.DoABC1 => Some(new DoABC)
				case _ => None
			}

			abcs = libraries flatMap {
				library => {
					(TagContainer fromFile library).tags collect { case x: DoABC => x } map { Abc fromDoABC _ }
				}
			}

			for {
				abc <- abcs
				nominalType <- abc.types if nominalType.inst.base.isDefined && !nominalType.inst.isInterface
			} {

				val stack = buildStack(nominalType, Stack.empty[AbcNominalType])
				var abstracts = List.empty[Symbol]
				var concretes = List.empty[Symbol]

				for {
					currentType <- stack
					`trait` <- currentType.inst.traits
				} {
					`trait` match {
						// Protected namespaces may contain the class name (Issue 38)
						// We are now checking only for the Symbol.
						case anyMethod: AbcTraitAnyMethod if anyMethod.name.namespace.kind == AbcNamespaceKind.Protected || anyMethod.name.namespace.kind == AbcNamespaceKind.Package => {
							if(anyMethod.metadata.isDefined && (anyMethod.metadata.get exists { _.name == 'Abstract })) {
								if(concretes contains anyMethod) {
									log.error("Error in class %s: Method %s has already been marked abstract.",
										toPackage(currentType.inst.name), anyMethod.name.name)
								}

								if(nominalType != currentType) {
									abstracts = anyMethod.name.name :: abstracts
								}
							} else if(abstracts contains anyMethod.name.name) {
								concretes = anyMethod.name.name :: concretes
							}
						}
						case _ =>
					}
				}

				for(error <- abstracts diff concretes) {
					log.error("Class %s must implement abstract method %s.",
						toPackage(nominalType.inst.name), error.name)
				}
			}
		}

		@inline private def lookup(qname: AbcQName): AbcNominalType = {
			def search(qname: AbcQName): AbcNominalType = {
				for(abc <- abcs) {
					val nominal = abc.types find (_.inst.name == qname)

					if(nominal.isDefined) {
						return nominal.get
					}
				}

				error("Could not find class "+toPackage(qname))
			}

			val result = cache.get(qname)

			if(result.isDefined) {
				result.get
			} else {
				val nominal = search(qname)
				cache = cache + (qname -> nominal)
				nominal
			}
		}

		private def toPackage(qname: AbcQName) = {
			val nsString = qname.namespace.name.name

			if(nsString.isEmpty) {
				qname.name.name
			} else {
				nsString+"."+qname.name.name
			}
		}
	}
}
