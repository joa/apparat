package apparat.abc

import apparat.bytecode.Bytecode
import apparat.utils.{IO, IndentingPrintWriter, Dumpable}

class AbcMethodParameter(val typeName: AbcName) extends Dumpable {
	var name: Option[Symbol] = None
	var optional = false
	var optionalType: Option[Int] = None
	var optionalVal: Option[Any] = None

	def accept(visitor: AbcVisitor) = visitor visit this

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Parameter: "
		writer withIndent {
			name match {
				case Some(name) => writer <= "Name: " + name.name
				case None =>
			}
			writer <= "Optional: " + optional
			optionalVal match {
				case Some(value) => writer <= "Default: " + value
				case None =>
			}
		}
	}
}

class AbcMethod(val parameters: Array[AbcMethodParameter], val returnType: AbcName,
				val name: Symbol, val needsArguments: Boolean, val needsActivation: Boolean, val needsRest: Boolean,
				val hasOptionalParameters: Boolean, val ignoreRest: Boolean, val isNative: Boolean,
				val setsDXNS: Boolean, val hasParameterNames: Boolean) extends Dumpable {
	var body: Option[AbcMethodBody] = None
	var anonymous = true
	
	def accept(visitor: AbcVisitor) = {
		visitor visit this

		parameters foreach (_ accept visitor)

		body match {
			case Some(body) => body accept visitor
			case None =>
		}
	}

	override def toString = "[AbcMethod name: " + name.toString() + "]"

	override def dump(writer: IndentingPrintWriter) = {
		writer <= (if(!anonymous) "Method:" else "Function:")
		writer withIndent {
			if(null != name.name) writer <= "Name: " + name.name
			writer <= "Return Type: " + returnType
			writer <= "Needs Arguments: " + needsArguments
			writer <= "Needs Rest: " + needsRest
			writer <= "Needs Activation: " + needsActivation
			writer <= "Has Optional Parameters: " + hasOptionalParameters
			writer <= "Ignore Rest: " + ignoreRest
			writer <= "Is Native: " + isNative
			writer <= "DXNS: " + setsDXNS
			writer <= "Has Parameter Names: " + hasParameterNames
			writer <= "Parameters:"
			writer withIndent {
				parameters foreach (_ dump writer)
			}
			body match {
				case Some(body) => body dump writer
				case None =>
			}
		}
	}
}

class AbcMethodBody(val maxStack: Int, val localCount: Int, val initScopeDepth: Int,
					val maxScopeDepth: Int, var code: Array[Byte], var exceptions: Array[AbcExceptionHandler],
					val traits: Array[AbcTrait], var bytecode: Option[Bytecode] = None) extends Dumpable with HasTraits
{
	def accept(visitor: AbcVisitor) = {
		visitor visit this
		exceptions foreach (_ accept visitor)
		traits foreach (_ accept visitor)
	}

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Method Body:"
		writer withIndent {
			writer <= "Max Stack: " + maxStack
			writer <= "Locals: " + localCount
			writer <= "InitScopeDepth: " + initScopeDepth
			writer <= "MaxScopeDepth: " + maxScopeDepth
			dumpTraits(writer)
			bytecode match {
				case Some(bytecode) => bytecode dump writer
				case None => {
					//TODO print exception handlers here
					writer <= "Code: "
					writer withIndent { IO dump (code, writer) }
				}
			}
		}
	}
}

class AbcExceptionHandler(val from: Int, val to: Int, val target: Int, val typeName: AbcName, val varName: AbcName) {
	def accept(visitor: AbcVisitor) = visitor visit this
}