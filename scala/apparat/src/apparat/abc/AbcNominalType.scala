package apparat.abc

class AbcNominalType(val inst: AbcInstance) {
	var klass: AbcClass = _//TODO replace with `class` and Option[AbcClass]
	
	def accept(visitor: AbcVisitor) = {
		visitor visit this

		inst accept visitor

		if(null != klass) {
			klass accept visitor
		}
	}
}
