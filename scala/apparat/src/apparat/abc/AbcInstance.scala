package apparat.abc

class AbcInstance(
		val name: AbcName,
		val base: Option[AbcName],
		val isSealed: Boolean,
		val isFinal: Boolean,
		val isInterface: Boolean,
		val protectedNs: Option[AbcNamespace],
		val interfaces: Array[AbcName],
		val init: AbcMethod,
		val traits: Array[AbcTrait]
		)