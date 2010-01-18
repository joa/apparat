package apparat.abc

class AbcClass(val init: AbcMethod, val traits: Array[AbcTrait]) {
	def accept(visitor: AbcVisitor) = {
		visitor visit this
		traits foreach (_ accept visitor)
	}
}