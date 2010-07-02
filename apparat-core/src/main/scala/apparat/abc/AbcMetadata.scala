package apparat.abc

import scala.collection.immutable.Map

class AbcMetadata(var name: Symbol, var attributes: Map[Symbol, Symbol]) {
	def accept(visitor: AbcVisitor) = visitor visit this
	
	override def toString = "[" + name + "(" + (for ((k, v) <- attributes) yield k + "=" + v).mkString(",") + ")]"
}
