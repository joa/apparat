package apparat.bytecode.analysis

import apparat.bytecode.Bytecode
import apparat.graph.immutable.BytecodeControlFlowGraphBuilder
import apparat.bytecode.operations.AbstractOp
import apparat.graph.{BlockVertex, BytecodeControlFlowGraph}

object StackAnalysis {
	def apply(bytecode: Bytecode): (Int, Int) = apply(BytecodeControlFlowGraphBuilder(bytecode))

	def apply[V <: BlockVertex[AbstractOp]](cfg: BytecodeControlFlowGraph[V]): (Int, Int) = {
		var visited = cfg vertexMap { v => false }
		var maxOperand = 0
		var maxScope = 0

		def loop(vertex: V, initOperand: Int, initScope: Int): Unit = {
			if(!visited(vertex)) {
				var operand = initOperand
				var scope = initScope

				for(op <- vertex.block) {
					operand += op.operandDelta
					scope += op.scopeDelta

					if(operand > maxOperand) {
						maxOperand = operand
					}

					if(scope > maxScope) {
						maxScope = scope
					}
				}

				visited = visited.updated(vertex, true)
				
				for(successor <- cfg outgoingOf vertex) {
					loop(successor.endVertex, operand, scope)
				}
			}
		}

		loop(cfg.entryVertex, 0, 0)

		(maxOperand, maxScope)
	}
}