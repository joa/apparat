package apparat.abc

import apparat.utils.{IndentingPrintWriter, Dumpable}

class AbcScript(var init: AbcMethod, var traits: Array[AbcTrait]) extends Dumpable with HasTraits {
	init.anonymous = false
	
	def accept(visitor: AbcVisitor) = visitor visit this

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Script:"
		writer withIndent {
			writer <= "Init:"
			writer withIndent { init dump writer }
			dumpTraits(writer)
		}
	}

}