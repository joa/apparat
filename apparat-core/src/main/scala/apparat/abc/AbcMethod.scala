package apparat.abc

import apparat.bytecode.Bytecode
import apparat.utils.{IO, IndentingPrintWriter, Dumpable}

class AbcMethodParameter(var typeName: AbcName) extends Dumpable {
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

class AbcMethod(var parameters: Array[AbcMethodParameter], var returnType: AbcName,
				var name: Symbol, var needsArguments: Boolean, var needsActivation: Boolean, var needsRest: Boolean,
				var hasOptionalParameters: Boolean, var ignoreRest: Boolean, var isNative: Boolean,
				var setsDXNS: Boolean, var hasParameterNames: Boolean) extends Dumpable {
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

class AbcMethodBody(var maxStack: Int, var localCount: Int, var initScopeDepth: Int,
					var maxScopeDepth: Int, var code: Array[Byte], var exceptions: Array[AbcExceptionHandler],
					var traits: Array[AbcTrait], var bytecode: Option[Bytecode] = None) extends Dumpable with HasTraits
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

class AbcExceptionHandler(var from: Int, var to: Int, var target: Int, var typeName: AbcName, var varName: AbcName) {
	def accept(visitor: AbcVisitor) = visitor visit this
}