package apparat.abc

import apparat.utils.{IndentingPrintWriter, Dumpable}

class AbcNominalType(val inst: AbcInstance) extends Dumpable {
	var klass: AbcClass = _//TODO replace with `class` and Option[AbcClass]
	
	def accept(visitor: AbcVisitor) = {
		visitor visit this

		inst accept visitor

		if(null != klass) {
			klass accept visitor
		}
	}

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Type:"
		writer withIndent {
			if(null != klass) klass dump writer
			inst dump writer
		}
	}
}
