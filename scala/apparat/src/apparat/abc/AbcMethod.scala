package apparat.abc

import apparat.bytecode.Bytecode

class AbcMethodParameter(val typeName: AbcName) {
	var name: Option[Symbol] = None
	var optional = false
	var optionalType: Option[Int] = None
	var optionalVal: Option[Any] = None

	def accept(visitor: AbcVisitor) = visitor visit this
}

class AbcMethod(val parameters: Array[AbcMethodParameter], val returnType: AbcName,
				val name: Symbol, val needsArguments: Boolean, val needsActivation: Boolean, val needsRest: Boolean,
				val hasOptionalParameters: Boolean, val ignoreRest: Boolean, val isNative: Boolean,
				val setsDXNS: Boolean, val hasParameterNames: Boolean) {
	var body: Option[AbcMethodBody] = None

	def accept(visitor: AbcVisitor) = {
		visitor visit this

		parameters foreach (_ accept visitor)

		body match {
			case Some(body) => body accept visitor
			case None =>
		}
	}

	override def toString = "[AbcMethod name: " + name.toString() + "]"
}

class AbcMethodBody(val maxStack: Int, val localCount: Int, val initScopeDepth: Int,
					val maxScopeDepth: Int, var code: Array[Byte], var exceptions: Array[AbcExceptionHandler],
					val traits: Array[AbcTrait], var bytecode: Option[Bytecode] = None)
{
	def accept(visitor: AbcVisitor) = {
		visitor visit this
		exceptions foreach (_ accept visitor)
		traits foreach (_ accept visitor)
	}
}

class AbcExceptionHandler(val from: Int, val to: Int, val target: Int, val typeName: AbcName, val varName: AbcName) {
	def accept(visitor: AbcVisitor) = visitor visit this
}