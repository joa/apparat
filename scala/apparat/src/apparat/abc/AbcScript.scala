package apparat.abc

class AbcScript(val init: AbcMethod, val traits: Array[AbcTrait]) {
	def accept(visitor: AbcVisitor) = visitor visit this
}