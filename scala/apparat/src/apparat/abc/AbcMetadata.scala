package apparat.abc

import scala.collection.immutable.Map

class AbcMetadata(val name: String, val attributes: Map[String, String]) {
	override def toString = "[" + name + "(" + (for ((k, v) <- attributes) yield k + "=" + v).mkString(",") + ")]"
}
