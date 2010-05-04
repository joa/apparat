package apparat.bytecode.analysis

import apparat.bytecode.Bytecode
import apparat.graph.immutable.BytecodeControlFlowGraphBuilder
import apparat.bytecode.operations.AbstractOp
import apparat.graph.{BlockVertex, BytecodeControlFlowGraph}

object StackAnalysis {
	def apply(bytecode: Bytecode) = apply(BytecodeControlFlowGraphBuilder(bytecode))

	def apply[V <: BlockVertex[AbstractOp]](cfg: BytecodeControlFlowGraph[V]) = {
		
	}
}