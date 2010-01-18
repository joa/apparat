package apparat.abc

class AbcInstance(
		val name: AbcQName,
		val base: Option[AbcName],
		val isSealed: Boolean,
		val isFinal: Boolean,
		val isInterface: Boolean,
		val nonNullable: Boolean,
		val protectedNs: Option[AbcNamespace],
		val interfaces: Array[AbcName],
		val init: AbcMethod,
		val traits: Array[AbcTrait]
		) {
	def accept(visitor: AbcVisitor) = {
		visitor visit this
		traits foreach (_ accept visitor)
	}
}