package apparat.bytecode.operations

import apparat.abc._

object Op {
	def byteOf(op: AbstractOp): Byte = op match {
		case _ => error("Unknown operation " + op)
	}
}

sealed abstract class AbstractOp {
	def canThrow = false
	def controlsFlow = false

	def pushOperands: Int = 0
	def popOperands: Int = 0
	def pushScopes: Int = 0
	def popScopes: Int = 0

	def operandDelta = pushOperands - popOperands
	def scopeDelta = pushScopes - popScopes

	override def equals(that: Any) = that match {
		case abstractOp: AbstractOp => abstractOp eq this
		case _ => false
	}
}

trait OpThatCanThrow extends AbstractOp { final override def canThrow = true }

trait OpThatControlsFlow extends AbstractOp { final override def controlsFlow = true }

trait OpWithRegister {
	self: AbstractOp =>
	def register: Int
}

trait OpWithSlot {
	self: AbstractOp =>
	def slot: Int
}

trait OpWithMethod {
	self: AbstractOp =>
	def method: AbcMethod
}

trait OpWithArguments extends AbstractOp {
	def numArguments: Int
	override def popOperands = super.popOperands + numArguments
}

trait OpWithProperty extends AbstractOp {
	def property: AbcName
	override def popOperands = super.popOperands + (property match {
		case AbcRTQNameL | AbcRTQNameLA => 2
		case AbcRTQName(_) | AbcRTQNameA(_) => 1
		case _ => 0
	})
}

sealed abstract class AbstractOpWithOperands(numPush: Int, numPop: Int) extends AbstractOp {
	override def pushOperands: Int = numPush
	override def popOperands: Int = numPop
}

sealed abstract class AbstractOpWithScopes(numPush: Int, numPop: Int) extends AbstractOp {
	override def pushScopes: Int = numPush
	override def popScopes: Int = numPop
}

sealed abstract class AbstractBinaryOp extends AbstractOpWithOperands(1, 2)
sealed abstract class AbstractUnaryOp extends AbstractOpWithOperands(1, 1)

case class Add extends AbstractBinaryOp
case class AddDouble extends AbstractBinaryOp
case class AddInt extends AbstractBinaryOp
case class ApplyType(val numArguments: Int) extends AbstractUnaryOp with OpWithArguments with OpThatCanThrow
case class AsType(typeName: AbcName) extends AbstractUnaryOp {
	typeName match {
		case AbcRTQName(_) | AbcRTQNameA(_) | AbcRTQNameL | AbcRTQNameLA => error("Type must not be a runtime name.")
		case _ => {}
	}
}
case class AsTypeLate() extends AbstractOpWithOperands(1, 2)
case class BitAnd() extends AbstractBinaryOp
case class BitNot() extends AbstractUnaryOp
case class BitOr() extends AbstractBinaryOp
case class BitXor() extends AbstractBinaryOp
case class Breakpoint() extends AbstractOp
case class BreakpointLine() extends AbstractOp
case class Call(val numArguments: Int) extends AbstractOpWithOperands(1, 2) with OpWithArguments with OpThatCanThrow
case class CallMethod(val numArguments: Int, val method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
case class CallProperty(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallPropLex(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallPropVoid(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallStatic(val numArguments: Int, val method: AbcMethod) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithMethod with OpThatCanThrow
case class CallSuper(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(1, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow
case class CallSuperVoid(val numArguments: Int, val property: AbcName) extends AbstractOpWithOperands(0, 1) with OpWithArguments with OpWithProperty with OpThatCanThrow

