package apparat.abc

class AbcMethodParameter(val typeName: AbcName) {
	var name: Option[String] = None
	var optional = false
	var optionalType: Option[Int] = None
	var optionalVal: Option[AnyVal] = None
}

class AbcMethod(val parameters: Array[AbcMethodParameter], val returnType: AbcName,
	val name: String, val needsArguments: Boolean, val needsActivation: Boolean, val needsRest: Boolean,
	val hasOptionalParameters: Boolean, val setsDXNS: Boolean, val hasParameterNames: Boolean) {
	var body: Option[AbcMethodBody] = None
	
	override def toString = "[AbcMethod name: " + name.toString() + "]"
}

class AbcMethodBody() {
	
}