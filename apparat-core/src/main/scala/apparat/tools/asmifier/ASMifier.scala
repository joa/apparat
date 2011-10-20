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
package apparat.tools.asmifier

import apparat.tools.{ApparatConfiguration, ApparatTool, ApparatApplication}
import apparat.swf.{DoABC, SwfTags}
import apparat.actors.Futures._

import java.io.{FileWriter, File => JFile}
import apparat.utils.{IndentingPrintWriter, TagContainer}
import apparat.utils.IO._
import apparat.abc._
import apparat.log.{Debug => DebugLevel}
import apparat.bytecode.operations._
import apparat.bytecode.{Marker, Bytecode}

/**
 * @author Joa Ebert
 */
object ASMifier {
	def main(args: Array[String]): Unit = ApparatApplication(new ASMifierTool, args)

	class ASMifierTool extends ApparatTool {
		var input: JFile = _
		var output: Option[JFile] = None

		override def name = "ASMifier"

		override def help = """  -i [file]	Input file
  -o [dir]	Output directory (optional)"""

		override def configure(config: ApparatConfiguration): Unit = configure(ASMifierConfigurationFactory fromConfiguration config)

		def configure(config: ASMifierConfiguration): Unit = {
			input = config.input
			output = config.output
		}

		override def run() = {
			SwfTags.tagFactory = (kind: Int) => kind match {
				case SwfTags.DoABC => Some(new DoABC)
				case SwfTags.DoABC1 => Some(new DoABC)
				case _ => None
			}

			if(input.getName.toLowerCase endsWith ".abc") {
				exportAbcs((Abc fromFile input) :: Nil)
			} else {
				val cont = TagContainer fromFile input
				exportAbcs(
					(for{
						tag <- cont.tags
						abc <- Abc fromTag tag
					} yield { abc }).toList
				)
			}
		}

		private def exportAbcs(abcs: List[Abc]) = {
			if(abcs.size > 1) {
				abcs map { abc => future { exportAbc(abc) } } foreach { _() }
			} else {
				abcs foreach exportAbc
			}
		}

		private def exportAbc(abc: Abc) = {
			abc.loadBytecode()

			abc.types foreach exportNominal
			abc.scripts foreach exportScript
		}

		private def exportNominal(nominal: AbcNominalType) = if(!nominal.inst.isInterface) {
			using(writerFor(nominal.name)) {
				writer => {
					writer <= "package "+nominal.name.namespace.name.name+" {"
					writer <= ""
					writer withIndent {
						writer <= "class "+nominal.name.name.name+" {"
						writer <= ""
						writer withIndent {

							writer <= "//cinit"
							for {
								body <- nominal.klass.init.body
							} {
								exportMethodBody(writer, body)
							}
							writer <= ""

							for {
								t <- nominal.klass.traits
							} {
								exportTrait(writer, t, true)
								writer <= ""
							}


							val iinit = nominal.inst.init
							writer <= "function "+nominal.name.name.name+"("+
								(iinit.parameters.zipWithIndex map { p => "p"+p._2+": "+prettyPrint(p._1.typeName) } mkString ", ")+") {"
							writer withIndent {
								for {
									body <- iinit.body
								} {
									exportMethodBody(writer, body)
								}
							}
							writer <= "}"
							writer <= ""

							for {
								t <- nominal.inst.traits
							} {
								exportTrait(writer, t, false)
								writer <= ""
							}

						}
						writer <= "}"
					}
					writer <= "}"
				}
			}
		}

		private def exportScript(script: AbcScript) = {
			for(t <- script.traits) t match {
				case anyMethod: AbcTraitAnyMethod => {
					using(writerFor(anyMethod.name)) {
						writer => {
							val method = anyMethod.method
							writer <= "package "+anyMethod.name.namespace.name.name+" {"
							writer <= ""
							writer withIndent {
								writer <= "function "+methodTypeOf(anyMethod)+
										anyMethod.name.name.name+"("+
										(method.parameters.zipWithIndex map { p => "p"+p._2+": "+prettyPrint(p._1.typeName) } mkString ", ")+
										"): "+prettyPrint(method.returnType)+" {"
								writer withIndent {
									if(method.body.isDefined) {
										exportMethodBody(writer, method.body.get)
									} else {
										writer <= "//empty methodbody"
									}
								}
								writer <= "}"
							}
							writer <= "}"
						}
					}
				}
				case _ =>
			}
		}

		private def exportTrait(writer: IndentingPrintWriter, t: AbcTrait, static: Boolean) = t match {
			case anyMethod: AbcTraitAnyMethod => {
				val method = anyMethod.method
				writer <= (if(static) "static " else "")+"function "+methodTypeOf(anyMethod)+
						anyMethod.name.name.name+"("+
						(method.parameters.zipWithIndex map { p => "p"+p._2+": "+prettyPrint(p._1.typeName) } mkString ", ")+
						"): "+prettyPrint(method.returnType)+" {"
				writer withIndent {
					if(method.body.isDefined) {
						exportMethodBody(writer, method.body.get)
					} else {
						writer <= "//empty methodbody"
					}
				}
				writer <= "}"
			}
			case _ =>
		}


		private def exportMethodBody(writer: IndentingPrintWriter, methodBody: AbcMethodBody) = {
			if(methodBody.exceptions.nonEmpty) {
				writer <= "//exception handlers are not supported yet"
			} else {
				if(methodBody.bytecode.isDefined) {
					writer <= "//locals: "+methodBody.localCount
					exportBytecode(writer, methodBody.bytecode.get)
				} else {
					writer <= "//bytecode is not defined"
				}
			}
		}

		private def exportBytecode(writer: IndentingPrintWriter, bytecode: Bytecode) = {
			val last = bytecode.ops.last
			val markers = bytecode.markers
			writer <= "__asm("
			writer withIndent {
				for(op <- bytecode.ops) {
					markers(op) match {
						case Some(marker) => writer <= "\""+marker.toString+":\", "
						case None =>
					}

					writer <= toAS3(op)+(if(op == last) "" else ", ")
				}
			}
			writer <= ");"
		}

		private def writerFor(qname: AbcQName) = new IndentingPrintWriter(new FileWriter(output match {
			case Some(dir) => new JFile(dir, asFilename(qname))
			case None => new JFile(asFilename(qname))
		}))

		private def asFilename(qname: AbcQName) = {
			namespaceWithDot(qname.namespace)+qname.name.name+".asm"
		}

		private def namespaceWithDot(namespace: AbcNamespace) = if(null == namespace || null == namespace.name || null == namespace.name.name) { "*" } else {
			if(namespace.name.name.length > 0) { namespace.name.name+"." } else { "" } }

		private def methodTypeOf(anyMethod: AbcTraitAnyMethod) = anyMethod match {
			case _: AbcTraitGetter => "get"
			case _: AbcTraitSetter => "set"
			case _ => ""
		}
		
		private def prettyPrint(name: AbcName): String = name match {
			case null => "*"
			case AbcQName(name, namespace) => if(null == namespace || null == namespace.name || null == namespace.name.name) "*" else { namespaceWithDot(namespace)+name.name }
			case AbcQNameA(name, namespace) => if(null == namespace || null == namespace.name || null == namespace.name.name) "*" else { namespaceWithDot(namespace)+name.name }
			case AbcRTQName(_) => "*"
			case AbcRTQNameA(_) => "*"
			case AbcRTQNameL => "*"
			case AbcRTQNameLA => "*"
			case AbcMultiname(_, _) => "*"
			case AbcMultinameA(_, _) => "*"
			case AbcMultinameL(_) => "*"
			case AbcMultinameLA(_) => "*"
			case AbcTypename(name: AbcQName, parameters: Array[AbcName]) => prettyPrint(name)+".<"+(parameters map prettyPrint mkString ", ")+">"
		}

		private def toAS3(op: AbstractOp): String = op match {
			case Add() => "Add"
			case AddInt() => "AddInt"
			case ApplyType(_) => op.toString
			case AsType(typeName) => "AsType("+toAS3(typeName)+")"
			case AsTypeLate() => "AsTypeLate"
			case BitAnd() => "BitAnd"
			case BitNot() => "BitNot"
			case BitOr() => "BitOr"
			case BitXor() => "BitXor"
			case Breakpoint() => "Breakpoint"
			case BreakpointLine() => "BreakpointLine"
			case Call(_) => op.toString
			case CallMethod(_, _) => op.toString
			case CallProperty(property, numArguments) => "CallProperty("+toAS3(property)+", "+numArguments+")"
			case CallPropLex(property, numArguments) => "CallPropLex("+toAS3(property)+", "+numArguments+")"
			case CallPropVoid(property, numArguments) => "CallPropVoid("+toAS3(property)+", "+numArguments+")"
			case CallStatic(method, numArguments) => "CallStatic(methodIndex, "+numArguments+")"
			case CallSuper(property, numArguments) => "CallSuper("+toAS3(property)+", "+numArguments+")"
			case CallSuperVoid(property, numArguments) => "CallSuperVoid("+toAS3(property)+", "+numArguments+")"
			case CheckFilter() => "CheckFilter"
			case Coerce(typeName) => "Coerce("+toAS3(typeName)+")"
			case CoerceAny() => "CoerceAny"
			case CoerceBoolean() => "CoerceBoolean"
			case CoerceDouble() => "CoerceDouble"
			case CoerceInt() => "CoerceInt"
			case CoerceObject() => "CoerceObject"
			case CoerceString() => "CoerceString"
			case CoerceUInt() => "CoerceUInt"
			case Construct(_) => op.toString
			case ConstructProp(property, numArguments) => "ConstructProp("+toAS3(property)+", "+numArguments+")"
			case ConstructSuper(_) => op.toString
			case ConvertBoolean() => "ConvertBoolean"
			case ConvertDouble() => "ConvertDouble"
			case ConvertInt() => "ConvertInt"
			case ConvertObject() => "ConvertObject"
			case ConvertString() => "ConvertString"
			case ConvertUInt() => "ConvertUInt"
			case Debug(kind, name, register, extra) => "Debug(%d, %s, %d, %d)".format(kind, toAS3(name), register, extra)
			case DebugFile(file) => "DebugFile("+toAS3(file)+")"
			case DebugLine(_) => op.toString
			case DecLocal(_) => op.toString
			case DecLocalInt(_) => op.toString
			case Decrement() => "Decrement"
			case DecrementInt() => "DecrementInt"
			case DeleteProperty(property) => "DeleteProperty("+toAS3(property)+")"
			case Divide() => "Divide"
			case Dup() => "Dup"
			case DefaultXMLNamespace(uri) => "DefaultXMLNamespace("+toAS3(uri)+")"
			case DefaultXMLNamespaceLate() => "DefaultXMLNamespaceLate"
			case Equals() => "Equals"
			case EscapeXMLAttribute() => "EscapeXMLAttribute"
			case EscapeXMLElement() => "EscapeXMLAttribute"
			case FindProperty(property) => "FindProperty("+toAS3(property)+")"
			case FindPropStrict(property) => "FindPropStrict("+toAS3(property)+")"
			case GetDescendants(property) => "GetDescendants("+toAS3(property)+")"
			case GetGlobalScope() => "GetGlobalScope"
			case GetGlobalSlot(_) => op.toString
			case GetLex(typeName) => "GetLex("+toAS3(typeName)+")"
			case GetLocal(_) => op.toString
			case GetProperty(property) => "GetProperty("+toAS3(property)+")"
			case GetScopeObject(_) => op.toString
			case GetSlot(_) => op.toString
			case GetSuper(property) => "GetSuper("+toAS3(property)+")"
			case GreaterEquals() => "GreaterEquals"
			case GreaterThan() => "GreaterThan"
			case HasNext() => "HasNext"
			case HasNext2(_, _) => op.toString
			case IfEqual(marker) => "IfEqual("+toAS3(marker)+")"
			case IfFalse(marker) => "IfFalse("+toAS3(marker)+")"
			case IfGreaterEqual(marker) => "IfGreaterEqual("+toAS3(marker)+")"
			case IfGreaterThan(marker) => "IfGreaterThan("+toAS3(marker)+")"
			case IfLessEqual(marker) => "IfLessEqual("+toAS3(marker)+")"
			case IfLessThan(marker) => "IfLessThan("+toAS3(marker)+")"
			case IfNotEqual(marker) => "IfNotEqual("+toAS3(marker)+")"
			case IfNotGreaterEqual(marker) => "IfNotGreaterEqual("+toAS3(marker)+")"
			case IfNotGreaterThan(marker) => "IfNotGreaterThan("+toAS3(marker)+")"
			case IfNotLessEqual(marker) => "IfNotLessEqual("+toAS3(marker)+")"
			case IfNotLessThan(marker) => "IfNotLessThan("+toAS3(marker)+")"
			case IfStrictEqual(marker) => "IfStrictEqual("+toAS3(marker)+")"
			case IfStrictNotEqual(marker) => "IfStrictNotEqual("+toAS3(marker)+")"
			case IfTrue(marker) => "IfTrue("+toAS3(marker)+")"
			case In() => "In"
			case IncLocal(_) => op.toString
			case IncLocalInt(_) => op.toString
			case Increment() => "Increment"
			case IncrementInt() => "IncrementInt"
			case InitProperty(property) => "InitProperty("+toAS3(property)+")"
			case InstanceOf() => "InstanceOf"
			case IsType(typeName) => "IsType("+toAS3(typeName)+")"
			case IsTypeLate() => "IsTypeLate"
			case Jump(marker) => "Jump("+toAS3(marker)+")"
			case Kill(_) => op.toString
			case Label() => "Label"
			case LessEquals() => "LessEquals"
			case LessThan() => "LessThan"
			case LookupSwitch(defaultCase, cases) => "LookupSwitch(%s, %s)".format(toAS3(defaultCase), cases map toAS3 mkString ", ")
			case ShiftLeft() => "ShiftLeft"
			case Modulo() => "Modulo"
			case Multiply() => "Multiply"
			case MultiplyInt() => "MultiplyInt"
			case Negate() => "Negate"
			case NegateInt() => "NegateInt"
			case NewActivation() => "NewActivation"
			case NewArray(_) => op.toString
			case NewCatch(_) => "NewCatch(exceptionHandler)"
			case NewClass(_) => "NewClass(class)"
			case NewFunction(_) => "NewFunction(functionIndex)"
			case NewObject(_) => op.toString
			case NextName() => "NextName"
			case NextValue() => "NextValue"
			case Nop() => "Nop"
			case Not() => "Not"
			case Pop() => "Pop"
			case PopScope() => "PopScope"
			case PushByte(_) => op.toString
			case PushDouble(value) => op.toString
			case PushFalse() => "PushFalse"
			case PushInt(value) => op.toString
			case PushNamespace(value) => "PushNamespace("+toAS3(value)+")"
			case PushNaN() => "PushNaN"
			case PushNull() => "PushNull"
			case PushScope() => "PushScope"
			case PushShort(_) => op.toString
			case PushString(value) => "PushString("+toAS3(value)+")"
			case PushTrue() => "PushTrue"
			case PushUInt(value) => "PushUInt("+value.toString+")"
			case PushUndefined() => "PushUndefined"
			case PushWith() => "PushWith"
			case ReturnValue() => "ReturnValue"
			case ReturnVoid() => "ReturnVoid"
			case ShiftRight() => "ShiftRight"
			case SetLocal(_) => op.toString
			case SetGlobalSlot(_) => op.toString
			case SetProperty(property) => "SetProperty("+toAS3(property)+")"
			case SetSlot(_) => op.toString
			case SetSuper(property) => "SetSuper("+toAS3(property)+")"
			case StrictEquals() => "StrictEquals"
			case Subtract() => "Subtract"
			case SubtractInt() => "SubtractInt"
			case Swap() => "Swap"
			case Throw() => "Throw"
			case TypeOf() => "TypeOf"
			case ShiftRightUnsigned() => "ShiftRightUnsigned"
			case SetByte() => "SetByte"
			case SetShort() => "SetShort"
			case SetInt() => "SetInt"
			case SetFloat() => "SetFloat"
			case SetDouble() => "SetDouble"
			case GetByte() => "GetByte"
			case GetShort() => "GetShort"
			case GetInt() => "GetInt"
			case GetFloat() => "GetFloat"
			case GetDouble() => "GetDouble"
			case Sign1() => "Sign1"
			case Sign8() => "Sign8"
			case Sign16() => "Sign16"
			case _ => op.toString
		}

		private def toAS3(name: AbcName): String = name match {
			case null => "*"
			case AbcQName(name, namespace) => "AbcQName("+toAS3(name)+", "+toAS3(namespace)+")"
			case AbcQNameA(name, namespace) => "AbcQNameA("+toAS3(name)+", "+toAS3(namespace)+")"
			case AbcRTQName(name) => "AbcRTQName("+toAS3(name)+")"
			case AbcRTQNameA(name) => "AbcRTQNameA("+toAS3(name)+")"
			case AbcRTQNameL => "AbcRTQNameL"
			case AbcRTQNameLA => "AbcRTQNameLA"
			case AbcMultiname(name, nsset) => "AbcMultiname("+toAS3(name)+", "+toAS3(nsset)+")"
			case AbcMultinameA(name, nsset) => "AbcMultinameA("+toAS3(name)+", "+toAS3(nsset)+")"
			case AbcMultinameL(nsset) => "AbcMultinameL("+toAS3(nsset)+")"
			case AbcMultinameLA(nsset) => "AbcMultinameLA("+toAS3(nsset)+")"
			case AbcTypename(name: AbcQName, parameters: Array[AbcName]) => "AbcTypename(%s, %s)".format(toAS3(name), parameters map toAS3 mkString ", ")
		}

		private def toAS3(symbol: Symbol): String = if(null == symbol.name) "null" else "\""+escape(symbol.name)+"\""
		private def escape(value: String): String = value

		private def toAS3(marker: Marker): String = "\""+marker.toString+"\""

		private def toAS3(namespace: AbcNamespace): String = "AbcNamespace("+kindOf(namespace)+", "+toAS3(namespace.name)+")"

		private def toAS3(namespaceSet: AbcNSSet): String = "AbcNamespaceSet("+(namespaceSet.set map toAS3 mkString ", ")+")"

		private def kindOf(namespace: AbcNamespace): String = "NamespaceKind."+(namespace.kind match {
			case x if x == AbcNamespaceKind.Namespace => "NAMESPACE"
			case x if x == AbcNamespaceKind.Package => "PACKAGE"
			case x if x == AbcNamespaceKind.PackageInternal => "PACKAGEINTERNAL"
			case x if x == AbcNamespaceKind.Protected => "PROTECTED"
			case x if x == AbcNamespaceKind.Explicit => "EXPLICIT"
			case x if x == AbcNamespaceKind.StaticProtected=> "STATICPROTECTED"
			case x if x == AbcNamespaceKind.Private => "PRIVATE"
		})
	}
}