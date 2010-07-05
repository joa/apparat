package apparat.abc

import apparat.utils.{IndentingPrintWriter, Dumpable}

class AbcNominalType(var inst: AbcInstance) extends Dumpable {
	var klass: AbcClass = _//TODO replace with `class` and Option[AbcClass]

	def name = inst.name

	def protectedNs = inst.protectedNs

	def privateNs = inst.privateNs
	
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

	override def toString = "[AbcNominalType inst: " + inst + "]"
}
