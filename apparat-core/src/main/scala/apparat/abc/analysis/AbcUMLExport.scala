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
package apparat.abc.analysis

import apparat.graph.{Edge, GraphLike, DOTExport}
import apparat.utils.IndentingPrintWriter
import apparat.utils.IO._
import java.io.{File => JFile, PrintStream => JPrintStream, PrintWriter => JPrintWriter, FileOutputStream => JFileOutputStream}
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import apparat.abc._

/**
 * @author Joa Ebert
 */
class AbcUMLExport(abcs: List[Abc]) {
	private lazy val indices = {
		var map: HashMap[AbcName, Int] = HashMap.empty
		var index = 0
		
		for(abc <- abcs; `type` <- abc.types) {
			map get `type`.inst.name match {
				case Some(_) =>
				case None => {
					map += `type`.inst.name -> index
					index += 1
				}
			}
		}

		map
	}

	private lazy val packages = {
		var map: HashMap[Symbol, List[AbcNominalType]] = HashMap.empty

		for(abc <- abcs; `type` <- abc.types) {
			val `package` = `type`.inst.name.namespace.name
			map(`package`) = `type` :: map.getOrElse(`package`, List.empty)
		}

		map
	}

	private lazy val topLevel = Symbol("TopLevel")

	def to(pathname: String): Unit = to(new JFile(pathname))

	def to(file: JFile): Unit = using(new JFileOutputStream(file)){
		stream => to(new IndentingPrintWriter(new JPrintWriter(stream)))
	}

	def to(stream: JPrintStream): Unit = to(new JPrintWriter(stream))

	def to(writer: JPrintWriter): Unit = {
		val indentingPrintWriter = new IndentingPrintWriter(writer)
		to(indentingPrintWriter)
		indentingPrintWriter.flush()
	}

	def to(writer: IndentingPrintWriter): Unit = {
		val fontName = "Bitstream Vera Sans Mono"
		val fontSize = 8

		writer <= "digraph G {"
		writer withIndent {
			writer <= "fontname=\""+fontName+"\";"
			writer <= "fontsize="+fontSize+";"
			writer <= "ranksep=2;"
			writer <= "ratio=auto;"

			writer <= "graph["
			writer withIndent {
				writer <= "rankdir= \"TB\","
				writer <= "splines= true,"
				writer <= "overlap= false"
			}
			writer <= "];"

			writer <= "node["
			writer withIndent {
				writer <= "fontname=\""+fontName+"\","
				writer <= "fontsize="+fontSize+","
				writer <= "shape=\"record\""
			}
			writer <= "];"

			writer <= "edge["
			writer withIndent {
				writer <= "fontname=\"" + fontName + "\","
				writer <= "fontsize=" + fontSize + ","
				writer <= "arrowhead=\"empty\""
			}
			writer <= "];"

			printPackages(writer)
			printInheritance(writer)

			writer <= "label=\"Powered by Apparat - http://apparat.googlecode.com/\";"
		}
		writer <= "}"
	}

	private def printPackages(implicit writer: IndentingPrintWriter) = {
		var i = 0

		for((name, types) <- packages.elements) {
			val `package` = name.name.length match {
				case 0 => topLevel
				case _ => name
			}

			writer <= "subgraph clusterP"+i+"{"
			writer withIndent {
				printTypes(types)
				writer <= "label=\""+`package`.name+"\";"
			}
			writer <= "}"

			i += 1
		}
	}

	private def printInheritance(implicit writer: IndentingPrintWriter) = {
		writer <= "edge[style=\"solid\"];"

		for(abc <- abcs; `type` <- abc.types if `type`.inst.base.isDefined) {
			indices get `type`.inst.base.get match {
				case Some(index) => writer <= indices(`type`.inst.name)+" -> "+index+";"
				case None =>
			}
		}

		writer <= "edge[style=\"dashed\"];"

		for(abc <- abcs; `type` <- abc.types; interface <- `type`.inst.interfaces) {
			indices get interface match {
				case Some(index) => writer <= indices(`type`.inst.name)+" -> "+index+";"
				case None =>
			}
		}
	}
	
	private def printTypes(types: List[AbcNominalType])(implicit writer: IndentingPrintWriter) = {
		for(`type` <- types) {
			val buffer = new StringBuilder()
			var properties = ListBuffer.empty[String]
			var methods = ListBuffer.empty[String]
			val inst = `type`.inst

			exportTraits(inst.traits, properties, methods, "")
			exportTraits(`type`.klass.traits, properties, methods, "$ ")

			if(inst.isInterface) {
				 buffer append "\\<\\<interface\\>\\>\\n"
			}

			buffer append inst.name.name.name

			if(inst.isFinal) {
				buffer append "\\n\\{final\\}"
			}

			if(!inst.isInterface)
			{
				buffer append "|"

				for(property <- properties.sorted) {
					buffer append property
					buffer append "\\l"
				}
			}

			buffer append "|"

			for(method <- methods.sorted) {
				buffer append method
				buffer append "\\l"
			}

			writer <= indices(inst.name)+" [label=\"{"+buffer.toString()+"}\"];"
		}
	}

	private def exportTraits(traits: Array[AbcTrait], properties: ListBuffer[String], methods: ListBuffer[String], prefix: String) = {
		var visited = HashMap.empty[AbcName, Boolean]

		//name.name.name!
		for(`trait` <- traits) {
			`trait` match {
				case AbcTraitConst(name, _, typeName, valueType, value, _) => {
					properties += prefix +
						getVisibility(name) + name.name.name + ": " + getType(typeName) + (value match {
							case Some(value) => " = " + getValue(valueType.get, value)
							case None => ""
						}) + " \\{read-only\\}"
				}

				case AbcTraitSlot(name, _, typeName, valueType, value, _) => {
					properties += prefix +
						getVisibility(name) + name.name.name + ": " + getType(typeName) + (value match {
							case Some(value) => " = " + getValue(valueType.get, value)
							case None => ""
						})
				}

				case AbcTraitMethod(name, _, method, isFinal, isOverride, _) => {
					methods += prefix +
						getVisibility(name) + name.name.name + "(" +
						getParameters(method) + "): " + getType(method.returnType) +
						(isOverride match {
							case true => " \\{redefines "+name.name.name+"\\}"
							case false => ""
						}) +
						(isFinal match {
							case true => " \\{final\\}"
							case false => ""
						})
				}

				case AbcTraitGetter(name, _, method, isFinal, isOverride, _) => if(!(visited contains name)) {
					val hasSetter = traits exists {
						case AbcTraitSetter(setterName, _, _, _, _, _) if name == setterName => true
						case _ => false
					}

					visited += name -> true

					properties += prefix +
						getVisibility(name) + name.name.name + ": " + getType(method.returnType) +
						(isOverride match {
							case true => " \\{redefines "+name.name.name+"\\}"
							case false => ""
						}) +
						(isFinal match {
							case true => " \\{final\\}"
							case false => ""
						}) +
						(hasSetter match {
							case true => ""
							case false => " \\{read-only\\}"
						})
				}

				case AbcTraitSetter(name, _, method, isFinal, isOverride, _) => if(!(visited contains name)) {
					val hasGetter = traits exists {
						case AbcTraitGetter(getterName, _, _, _, _, _) if name == getterName => true
						case _ => false
					}

					visited += name -> true
					
					properties += prefix +
						getVisibility(name) + name.name.name + ": " + getType(method.returnType) +
						(isOverride match {
							case true => " \\{redefines "+name.name.name+"\\}"
							case false => ""
						}) +
						(isFinal match {
							case true => " \\{final\\}"
							case false => ""
						}) +
						(hasGetter match {
							case true => ""
							case false => " \\{write-only\\}"
						})
				}

				case _ =>
			}
		}
	}

	private def getVisibility(name: AbcQName) = name.namespace.kind match {
		case AbcNamespaceKind.PackageInternal => "~ "
		case AbcNamespaceKind.Package => "+ "
		case AbcNamespaceKind.Private => "- "
		case AbcNamespaceKind.Protected => "# "
		case _ => ""
	}
	
	private def getType(name: AbcName): String = name match {
		case AbcQName(name, _) => name.name
		case AbcTypename(name, parameters) => name.name.name + ".\\<"+getType(parameters(0))+"\\>"
		case _ => "*"
	}

	private def getValue(valueType: Int, value: Any) = valueType match {
		case AbcConstantType.Double => value.asInstanceOf[Double].toString
		case AbcConstantType.False => "false"
		case AbcConstantType.Int => value.asInstanceOf[Int].toString
		case AbcConstantType.Null => "null"
		case AbcConstantType.True => "true"
		case AbcConstantType.UInt => value.asInstanceOf[Long].toString
		case AbcConstantType.Undefined => "undefined"
		case AbcConstantType.Utf8 => "\\\""+escape(value.asInstanceOf[Symbol].name)+"\\\""
		case _ => "\\<unknown\\>"
	}

	private def getParameters(method: AbcMethod) = {
		val buffer = new StringBuilder

		val n = method.parameters.length
		val m = n - 1
		var i = 0

		while(i < n) {
			val parameter = method.parameters(i)

			parameter.name match {
				case Some(Symbol(name)) if name.length != 0 => name
				case _ => buffer append "p"+i
			}

			buffer append ": "
			buffer append getType(parameter.typeName)

			if(parameter.optional) {
				buffer append " = "+getValue(parameter.optionalType.get, parameter.optionalVal.get)
			}

			if(i != m) {
				buffer append ", "
			}

			i += 1
		}

		buffer.toString
	}

	private def escape(value: String) = value.replaceAll("\\\\", "\\\\\\\\").replaceAll("\n", "\\\\\\\\n").replaceAll("\"", "\\\\\"")
}