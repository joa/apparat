package apparat.abc

import apparat.utils.{Dumpable, IndentingPrintWriter}

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
		) extends Dumpable with HasTraits {
	init.anonymous = false
	
	def accept(visitor: AbcVisitor) = {
		visitor visit this
		traits foreach (_ accept visitor)
	}

	override def dump(writer: IndentingPrintWriter) = {
		writer <= "Instance:"
		writer withIndent {
			writer <= "Name: " + name
			base match {
				case Some(base) => writer <= "Base: " + base
				case None =>
			}
			writer <= "Is Sealed: " + isSealed
			writer <= "Is Final: " + isFinal
			writer <= "Is Interface " + isInterface
			writer <= "Nullable: " + (!nonNullable)
			protectedNs match {
				case Some(ns) => writer <= "Protected Namespace: " + ns
				case None =>
			}
			writer <= "Interfaces: "
			writer <<< interfaces
			init dump writer
			dumpTraits(writer)

		}
	}

	override def toString = "[AbcInstance name: " + name + "]"
}