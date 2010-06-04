/*
 * This file is part of Apparat.
 *
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.taas.frontend.abc

import apparat.actors.Futures._
import apparat.taas.ast._
import apparat.graph.{BlockVertex, BytecodeControlFlowGraph}
import collection.immutable.HashMap
import apparat.graph.immutable.{Graph, BytecodeControlFlowGraphBuilder}
import apparat.bytecode.operations._
import apparat.abc.{Abc, AbcMethod, AbcNominalType, AbcQName}
import apparat.graph.Edge
import apparat.taas.graph.{TaasEntry, TaasExit, TaasBlock, TaasGraph, TaasGraphLinearizer}

/**
 * @author Joa Ebert
 */
protected[abc] class AbcCode(ast: TaasAST, abc: Abc, method: AbcMethod, scope: Option[AbcNominalType], isStatic: Boolean) extends TaasCode {
	implicit private val implicitAST = ast

	lazy val graph = {
		val f = future { computeGraph() }
		f()
	}

	val scopeType = scope match {
		case Some(scope) => AbcTypes.fromQName(scope.inst.name)
		case None => TaasAnyType
	}

	var numRegisters = 0

	private def computeGraph(): TaasGraph = {
		require(method.body.isDefined, "MethodBody has to be defined.")
		require(method.body.get.bytecode.isDefined, "Bytecode has to be defined.")

		val body = method.body.get
		val bytecode = body.bytecode.get
		val g = BytecodeControlFlowGraphBuilder(bytecode)
		implicit val registers = List.tabulate(body.localCount + body.maxStack) { TReg }

		registers(0) typeAs scopeType
		
		for(i <- 1 to method.parameters.length) {
			method.parameters(i).typeName match {
				case qname: AbcQName => registers(i) typeAs AbcTypes.fromQName(qname)
				case other => error("Expected QName, got "+other+".")
			}
		}

		numRegisters = body.localCount

		try {
			val mapping = mapVertices(g, annotate(g))
			var result = new Graph[TaasBlock]()

			result = result ++ mapping.valuesIterator.toTraversable

			for(edge <- g.edgesIterator) {
				result += Edge.transpose(edge, mapping(edge.startVertex), mapping(edge.endVertex))
			}

			result.dotExport to Console.out
			println("-------------------------------------------------")
			new TaasGraphLinearizer(new TaasGraph(result, TaasEntry, TaasExit)).list foreach println
			println("=================================================")
			new TaasGraph(result, TaasEntry, TaasExit)
		} catch {
			case e => {
				e.printStackTrace
				bytecode.dump()
				println("-------------------------------------------------")
				new TaasGraph(new Graph[TaasBlock](), TaasEntry, TaasExit)
			}
		}
	}

	private def mapVertices[V <: BlockVertex[AbstractOp]](g: BytecodeControlFlowGraph[V], annotations: Map[V, (Int, Int)])(implicit registers: List[TReg]) = {
		var result = HashMap.empty[V, TaasBlock]

		for(vertex <- g.verticesIterator) {
			result += vertex -> (
				if(g isEntry vertex) TaasEntry
				else if(g isExit vertex) TaasExit
				else mapVertex(vertex, annotations(vertex))
			)
		}

		result
	}
	
	private def mapVertex[V <: BlockVertex[AbstractOp]](vertex: V, stack: (Int, Int))(implicit registers: List[TReg]) = {
		var operandStack = stack._1
		var scopeStack = stack._2

		@inline def register(i: Int): TReg = registers(i)
		@inline def operand(i: Int): TReg = registers(i + numRegisters)
		@inline def push(value: TValue): TExpr = { val o = operandStack; operandStack += 1; T2(TOp_Nothing, value, operand(o)) }
		@inline def nextOperand: TReg = { val result = operandStack; operandStack += 1; operand(result) }
		@inline def pop(): TReg = { operandStack -= 1; operand(operandStack) }
		@inline def binop(op: TaasBinop): TExpr = {
			val rhs = pop()
			val lhs = pop()
			val result = operand(operandStack)
			operandStack += 1
			T3(op, lhs, rhs, result)
		}
		@inline def unop(op: TaasUnop): TExpr = {
			val rhs = pop()
			val result = operand(operandStack)
			operandStack += 1
			T2(op, rhs, result)
		}
		@inline def if1(op: TaasUnop) = TIf1(op, pop())
		@inline def if2(op: TaasBinop) = {
			val rhs = pop()
			val lhs = pop()
			TIf2(op, lhs, rhs)
		}
		@inline def arguments(n: Int): List[TReg] = List.fill(n) { pop() }.reverse
		@inline def ignored = TNop()

		new TaasBlock(vertex.block map {
			op => {
				val operandStackBefore = operandStack
				val scopeStackBefore = scopeStack
				
				val result = op match {
					case Add() | AddInt() => binop(TOp_+)
					case ApplyType(numArguments) => TODO(op)
					case AsType(typeName) => TODO(op)
					case AsTypeLate() => TODO(op)
					case BitAnd() => binop(TOp_&)
					case BitNot() => unop(TOp_~)
					case BitOr() => binop(TOp_|)
					case BitXor() => binop(TOp_^)
					case Breakpoint() | BreakpointLine() => ignored
					case Call(numArguments) => TODO(op)
					case CallMethod(index, numArguments) => TODO(op)
					case CallProperty(property, numArguments) => TODO(op)
					case CallPropLex(property, numArguments) => TODO(op)
					case CallPropVoid(property, numArguments) => {
						val args = arguments(numArguments)
						val obj = pop()
						val method = AbcSolver.property(obj.`type`, property, numArguments) match {
							case Some(method: TaasMethod) => method
							case _ => error("Could not find property "+property+" on "+obj.`type`)
						}
						TCall(obj, method, args, None)
					}
					case CallStatic(method, numArguments) => TODO(op)
					case CallSuper(property, numArguments) => TODO(op)
					case CallSuperVoid(property, numArguments) => TODO(op)
					case CheckFilter() => TODO(op)
					case Coerce(typeName) => typeName match {
						case qname: AbcQName => unop(TCoerce(AbcTypes fromQName qname))
						case other => error("Unexpected Coerce("+other+").")
					}
					case CoerceAny() => unop(TCoerce(TaasAnyType))
					case CoerceBoolean() => unop(TCoerce(TaasBooleanType))
					case CoerceDouble() => unop(TCoerce(TaasDoubleType))
					case CoerceInt() => unop(TCoerce(TaasIntType))
					case CoerceObject() => unop(TCoerce(TaasObjectType))
					case CoerceString() => unop(TCoerce(TaasStringType))
					case CoerceUInt() => unop(TCoerce(TaasLongType))
					case Construct(numArguments) => {
						val args = arguments(numArguments)
						TConstruct(pop(), args)
					}
					case ConstructProp(property, numArguments) => TODO(op)
					case ConstructSuper(numArguments) => {
						val args = arguments(numArguments)
						TSuper(pop(), args)
					}
					case ConvertBoolean() => unop(TConvert(TaasBooleanType))
					case ConvertDouble() => unop(TConvert(TaasDoubleType))
					case ConvertInt() => unop(TConvert(TaasIntType))
					case ConvertObject() => unop(TConvert(TaasObjectType))
					case ConvertString() => unop(TConvert(TaasStringType))
					case ConvertUInt() => unop(TConvert(TaasLongType))
					case Debug(kind, name, register, extra) => TODO(op)
					case DebugFile(file) => ignored
					case DebugLine(line) => ignored
					case DecLocal(index) => T3(TOp_-, register(index), TInt(1), register(index))
					case DecLocalInt(index) => T3(TOp_-, register(index), TInt(1), register(index))
					case Decrement() | DecrementInt() => {
						val lhs = pop()
						val result = operand(operandStack)
						operandStack += 1
						T3(TOp_-, lhs, TInt(1), result)
					}
					case DeleteProperty(property) => TODO(op)
					case Divide() => binop(TOp_/)
					case Dup() => TODO(op)
					case DefaultXMLNamespace(uri) => TODO(op)
					case DefaultXMLNamespaceLate() => TODO(op)
					case Equals() => TODO(op)
					case EscapeXMLAttribute() => TODO(op)
					case EscapeXMLElement() => TODO(op)
					case FindProperty(property) => TODO(op)
					case FindPropStrict(property) => property match {
						case qname: AbcQName => push(TLexical((AbcTypes fromQName qname).nominal))
						case _ => error("QName expected.")
					}
					case GetDescendants(property) => TODO(op)
					case GetGlobalScope() => TODO(op)
					case GetGlobalSlot(slot) => TODO(op)
					case GetLex(typeName) => push(TLexical(AbcSolver getLexical (scopeType, isStatic, typeName) getOrElse error("Could not solve "+typeName+" on "+scopeType+".")))
					case GetLocal(index: Int) => push(register(index))
					case GetProperty(property) => {
						val obj = pop()
						AbcSolver.getProperty(obj.`type`, property) match {
							case Some(method: TaasMethod) => TCall(obj, method, List.empty, Some(nextOperand))
							case Some(field: TaasField) => TLoad(obj, field, nextOperand)
							case _ => error("Could not find property "+property+" on "+obj.`type`)
						}
					}
					case GetScopeObject(index) => TODO(op)
					case GetSlot(slot) => TODO(op)
					case GetSuper(property) => TODO(op)
					case GreaterEquals() | GreaterThan() => TODO(op)
					case HasNext() => TODO(op)
					case HasNext2(objectRegister, indexRegister) => TODO(op)
					case IfEqual(marker) => if2(TOp_==)
					case IfFalse(marker) => if1(TOp_false)
					case IfGreaterEqual(marker) => if2(TOp_>=)
					case IfGreaterThan(marker) => if2(TOp_>)
					case IfLessEqual(marker) => if2(TOp_<=)
					case IfLessThan(marker) => if2(TOp_<)
					case IfNotEqual(marker) => if2(TOp_!=)
					case IfNotGreaterEqual(marker) => if2(TOp_!>=)
					case IfNotGreaterThan(marker) => if2(TOp_!>)
					case IfNotLessEqual(marker) => if2(TOp_!<=)
					case IfNotLessThan(marker) => if2(TOp_!<)
					case IfStrictEqual(marker) => if2(TOp_===)
					case IfStrictNotEqual(marker) => if2(TOp_!==)
					case IfTrue(marker) => if1(TOp_true)
					case In() => TODO(op)
					case IncLocal(index) => T3(TOp_+, register(index), TInt(1), register(index))
					case IncLocalInt(index) => T3(TOp_+, register(index), TInt(1), register(index))
					case Increment() | IncrementInt() => {
						val lhs = pop()
						val result = operand(operandStack)
						operandStack += 1
						T3(TOp_+, lhs, TInt(1), result)
					}
					case InitProperty(property) => TODO(op)
					case InstanceOf() => TODO(op)
					case IsType(typeName) => TODO(op)
					case IsTypeLate() => TODO(op)
					case Jump(marker) => ignored
					case Kill(register) => ignored
					case Label() => ignored
					case LessEquals() | LessThan() => TODO(op)
					case LookupSwitch(defaultCase, cases) => TODO(op)
					case ShiftLeft() => TODO(op)
					case Modulo() => binop(TOp_%)
					case Multiply() | MultiplyInt() => binop(TOp_*)
					case Negate() | NegateInt() => TODO(op)
					case NewActivation() => TODO(op)
					case NewArray(numArguments) => TODO(op)
					case NewCatch(exceptionHandler) => TODO(op)
					case NewClass(nominalType) => TODO(op)
					case NewFunction(function) => TODO(op)
					case NewObject(numArguments) => TODO(op)
					case NextName() | NextValue() | Nop() | Not() | Pop() | PopScope() => TODO(op)
					case PushByte(value) => push(TInt(value))
					case PushDouble(value) => push(TDouble(value))
					case PushFalse() => push(TBool(false))
					case PushInt(value) => push(TInt(value))
					case PushNamespace(value) => TODO(op)
					case PushNaN() => push(TDouble(Double.NaN))
					case PushNull() => TODO(op)
					case PushScope() => { pop(); ignored }
					case PushShort(value) => push(TInt(value))
					case PushString(value) => push(TString(value))
					case PushTrue() => push(TBool(true))
					case PushUInt(value) => push(TLong(value))
					case PushUndefined() | PushWith() => TODO(op)
					case ReturnValue() => TReturn(pop())
					case ReturnVoid() => TReturn(TVoid)
					case ShiftRight() => TODO(op)
					case SetLocal(index) => T2(TOp_Nothing, pop(), register(index))
					case SetGlobalSlot(slot) => TODO(op)
					case SetProperty(property) => {
						val arg = pop()
						val obj = pop()
						AbcSolver.setProperty(obj.`type`, property) match {
							case Some(method: TaasMethod) => TCall(obj, method, arg :: Nil, None)
							case Some(field: TaasField) => TStore(obj, field, arg)
							case _ => error("Could not find property "+property+" on "+obj.`type`)
						}
					}
					case SetSlot(slot) => TODO(op)
					case SetSuper(property) => TODO(op)
					case StrictEquals() => TODO(op)
					case Subtract() | SubtractInt() => binop(TOp_-)
					case Swap() => TODO(op)
					case Throw() | TypeOf() | ShiftRightUnsigned() => TODO(op)
					case SetByte() | SetShort() | SetInt() | SetFloat() | SetDouble() => TODO(op)
					case GetByte() | GetShort() | GetInt() | GetFloat() | GetDouble() => TODO(op)
					case Sign1() | Sign8() | Sign16() => TODO(op)
				}

				if((operandStackBefore + op.operandDelta) != operandStack) {
					error("Wrong operand-stack delta for "+op+". Got "+operandStackBefore+" -> "+operandStack+", expected "+operandStackBefore+" -> "+(operandStackBefore + op.operandDelta))
				}
				
				result
			}
		} filterNot { _ == TNop() })
	}

	private def TODO(op: AbstractOp) = error("TODO "+op)

	/**
	 * Creates and returns a mapping between a basic block
	 * and its initial stack depth.
	 */
	private def annotate[V <: BlockVertex[AbstractOp]](g: BytecodeControlFlowGraph[V]) = {
		var result = HashMap.empty[V, (Int, Int)]
		var visited = g vertexMap { v => false }

		def loop(vertex: V, initOperand: Int, initScope: Int): Unit = {
			if(!visited(vertex)) {
				result += vertex -> (initOperand, initScope)
				visited = visited.updated(vertex, true)

				var operand = initOperand
				var scope = initScope

				for(op <- vertex.block) {
					operand += op.operandDelta
					scope += op.scopeDelta
				}

				for(successor <- g outgoingOf vertex) {
					loop(successor.endVertex, operand, scope)
				}
			}
		}

		loop(g.entryVertex, 0, 0)
		result
	}

	override def toString = "AbcCode"
}