package apparat.abc

import scala.collection.immutable.Map

class AbcMetadata(var name: Symbol, var attributes: Map[Symbol, Symbol]) {
	def accept(visitor: AbcVisitor) = visitor visit this
	
	override def toString = "[" + name + "(" + (for ((k, v) <- attributes) yield k + "=" + v).mkString(",") + ")]"

	override def equals(that: Any) = that match {
		case thatMetadata: AbcMetadata if name == thatMetadata.name && attributes == thatMetadata.attributes => true
		case _ => false
	}
}
