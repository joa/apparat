package apparat.abc

import apparat.utils.{IndentingPrintWriter, Dumpable}

class AbcClass(val init: AbcMethod, val traits: Array[AbcTrait]) extends Dumpable with HasTraits {
	init.anonymous = false
	
	def accept(visitor: AbcVisitor) = {
		visitor visit this
		traits foreach (_ accept visitor)
	}

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Class: "
		writer withIndent {
			dumpTraits(writer)
			init dump writer
		}
	}
}