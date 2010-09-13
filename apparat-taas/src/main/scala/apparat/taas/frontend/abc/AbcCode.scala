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
import apparat.abc._
import apparat.graph.Edge
import apparat.taas.graph.{TaasEntry, TaasExit, TaasBlock, TaasGraph, TaasGraphLinearizer}
import apparat.taas.optimization._
import apparat.log.{SimpleLog, Debug => DebugLogLevel}

/**
 * @author Joa Ebert
 */
protected[abc] object AbcCode {
	val DEBUG = "true" == System.getProperty("apparat.debug", "false")
}

protected[abc] class AbcCode(ast: TaasAST, abc: Abc, method: AbcMethod,
							 scope: Option[AbcNominalType], isStatic: Boolean) extends TaasCode with SimpleLog {
	implicit private val implicitAST = ast

	lazy val graph = computeGraph()

	val scopeType = scope match {
		case Some(scope) => AbcTypes name2type scope.inst.name
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
		
		for(i <- 0 until method.parameters.length) {
			method.parameters(i).typeName match {
				case qname: AbcQName => registers(i + 1) typeAs (AbcTypes name2type qname)
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

			val taasGraph = new TaasGraph(result, TaasEntry, TaasExit)
			var modified = false

			if(AbcCode.DEBUG) {
				log.debug("Bytecode:")
				bytecode.dump()
				log.debug("Code after initial parse step:")
				new TaasGraphLinearizer(taasGraph).dump(log, DebugLogLevel)
			}

			do {
				modified = false
				modified |= CopyPropagation(taasGraph)
				modified |= ConstantFolding(taasGraph)
				modified |= DeadCodeElimination(method.parameters.length, taasGraph)

				/*if(modified) {
					new TaasGraphLinearizer(taasGraph).list foreach println
					log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
				}*/
			} while(modified)

			if(AbcCode.DEBUG) {
				log.debug("Code after CP/CF/DCE:")
				new TaasGraphLinearizer(taasGraph).dump(log, DebugLogLevel)
			}
			
			taasGraph
		} catch {
			case e => {
				e.printStackTrace()
				bytecode.dump()
				log.debug("-------------------------------------------------")
				new TaasGraph(new Graph[TaasBlock](), TaasEntry, TaasExit)
			}
		}
	}

	private def mapVertices[V <: BlockVertex[AbstractOp]](g: BytecodeControlFlowGraph[V], annotations: Map[V, (Int, Int)])(implicit registers: List[TReg]) = {
		var result = HashMap.empty[V, TaasBlock]

		for(vertex <- g.topsort) {
			result += vertex -> (
				if(g isEntry vertex) TaasEntry
				else if(g isExit vertex) TaasExit
				else mapVertex(vertex, annotations(vertex))
			)
		}

		result
	}
	
	private def mapVertex[V <: BlockVertex[AbstractOp]](vertex: V, stack: (Int, Int))(implicit registers: List[TReg]) = {
		var r = List.empty[TExpr]
		var operandStack = stack._1
		var scopeStack = stack._2

		@inline def pp(expr: TExpr) = {
			if(log.debugEnabled) {
				log.debug("%s", expr)
			}
			r = expr :: r
		}
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

		if(method.needsArguments) {
			//
			// TODO check if method requires "arguments", if yes create an Array object
			// that lives in register parameters+1, which is filled with the arguments.
			//
			// We may do this only in the first successor of the TaasEntry for this method.
			//
			error("TODO method.needsArguments")
		}

		for(op <- vertex.block) {
			val operandStackBefore = operandStack
			val scopeStackBefore = scopeStack

			op match {
				case Add() | AddInt() => pp(binop(TOp_+))
				case ApplyType(numArguments) => TODO(op)
				case AsType(typeName) => TODO(op)
				case AsTypeLate() => TODO(op)
				case BitAnd() => pp(binop(TOp_&))
				case BitNot() => pp(unop(TOp_~))
				case BitOr() => pp(binop(TOp_|))
				case BitXor() => pp(binop(TOp_^))
				case Breakpoint() | BreakpointLine() =>
				case Call(numArguments) => TODO(op)
				case CallMethod(index, numArguments) => TODO(op)
				case CallProperty(property, numArguments) => {
					val args = arguments(numArguments)
					val obj = pop()
					println(obj.`type`)
					println(AbcTypes.name2type(property))//fixme!!
					println(obj.`type` == AbcTypes.name2type(property))//fixme!!
					if(obj.`type` == AbcTypes.name2type(property) && args.length == 1) {
						pp(T2(TCoerce(obj.`type`), args.head, nextOperand))
					} else {
						val method = AbcSolver.property(obj.`type`, property, numArguments) match {
							case Some(method: TaasMethod) => method
							case _ => error("Could not find property "+property+" on "+obj.`type`)
						}
						pp(TCall(obj, method, args, Some(nextOperand)))
					}
				}
				case CallPropLex(property, numArguments) => TODO(op)
				case CallPropVoid(property, numArguments) => {
					val args = arguments(numArguments)
					val obj = pop()
					val method = AbcSolver.property(obj.`type`, property, numArguments) match {
						case Some(method: TaasMethod) => method
						case _ => error("Could not find property "+property+" on "+obj.`type`)
					}
					pp(TCall(obj, method, args, None))
				}
				case CallStatic(method, numArguments) => TODO(op)
				case CallSuper(property, numArguments) => TODO(op)
				case CallSuperVoid(property, numArguments) => TODO(op)
				case CheckFilter() => TODO(op)
				case Coerce(typeName) => typeName match {
					case qname: AbcQName => pp(unop(TCoerce(AbcTypes name2type qname)))
					case other => error("Unexpected Coerce("+other+").")
				}
				case CoerceAny() => pp(unop(TCoerce(TaasAnyType)))
				case CoerceBoolean() => pp(unop(TCoerce(TaasBooleanType)))
				case CoerceDouble() => pp(unop(TCoerce(TaasDoubleType)))
				case CoerceInt() => pp(unop(TCoerce(TaasIntType)))
				case CoerceObject() => pp(unop(TCoerce(TaasObjectType)))
				case CoerceString() => pp(unop(TCoerce(TaasStringType)))
				case CoerceUInt() => pp(unop(TCoerce(TaasLongType)))
				case Construct(numArguments) => {
					val args = arguments(numArguments)
					pp(TConstruct(pop(), args, nextOperand))
				}
				case ConstructProp(property, numArguments) => {
					//TODO verify that property equals obj
					val args = arguments(numArguments)
					pp(TConstruct(pop(), args, nextOperand))
				}
				case ConstructSuper(numArguments) => {
					val args = arguments(numArguments)
					pp(TSuper(pop(), args))
				}
				case ConvertBoolean() => pp(unop(TConvert(TaasBooleanType)))
				case ConvertDouble() => pp(unop(TConvert(TaasDoubleType)))
				case ConvertInt() => pp(unop(TConvert(TaasIntType)))
				case ConvertObject() => pp(unop(TConvert(TaasObjectType)))
				case ConvertString() => pp(unop(TConvert(TaasStringType)))
				case ConvertUInt() => pp(unop(TConvert(TaasLongType)))
				case Debug(kind, name, register, extra) =>
				case DebugFile(file) =>
				case DebugLine(line) =>
				case DecLocal(index) => pp(T3(TOp_-, register(index), TInt(1), register(index)))
				case DecLocalInt(index) => pp(T3(TOp_-, register(index), TInt(1), register(index)))
				case Decrement() | DecrementInt() => {
					val lhs = pop()
					val result = operand(operandStack)
					operandStack += 1
					pp(T3(TOp_-, lhs, TInt(1), result))
				}
				case DeleteProperty(property) => TODO(op)
				case Divide() => {
					val rhs = pop()
					val lhs = pop()
					val result = operand(operandStack)
					operandStack += 1
					pp(T2(TConvert(TaasDoubleType), lhs, lhs))
					pp(T2(TConvert(TaasDoubleType), rhs, rhs))
					pp(T3(TOp_/, lhs, rhs, result))
				}
				case Dup() => {
					val reg = pop()
					pp(push(reg))
					pp(push(reg))
				}
				case DefaultXMLNamespace(uri) => TODO(op)
				case DefaultXMLNamespaceLate() => TODO(op)
				case Equals() => TODO(op)
				case EscapeXMLAttribute() => TODO(op)
				case EscapeXMLElement() => TODO(op)
				case FindProperty(property) => {//we are always strict...
					property match {
						case qname: AbcQName => {
							scopeType match {
								case t: TaasNominalType =>
									pp(push(TLexical(AbcSolver.property(t.nominal, property) getOrElse {
										(AbcTypes name2type qname) match {
											case n: TaasNominalType => n.nominal
											case other => error("TaasNominalType expected, got "+other+".")
										}})))
								case _ => pp(push(TLexical((AbcTypes name2type qname) match {
											case n: TaasNominalType => n.nominal
											case other => error("TaasNominalType expected, got "+other+".")
										})))
							}
						}
						case _ => error("QName expected.")
					}
				}
				case FindPropStrict(property) => {
					property match {
						case qname: AbcQName => {
							scopeType match {
								case t: TaasNominalType =>
									pp(push(TLexical(AbcSolver.property(t.nominal, property) getOrElse {
										(AbcTypes name2type qname) match {
											case n: TaasNominalType => n.nominal
											case other => error("TaasNominalType expected, got "+other+".")
										}})))
								case _ => pp(push(TLexical((AbcTypes name2type qname) match {
											case n: TaasNominalType => n.nominal
											case other => error("TaasNominalType expected, got "+other+".")
										})))
							}
						}
						case _ => error("QName expected.")
					}
				}
				case GetDescendants(property) => TODO(op)
				case GetGlobalScope() => TODO(op)
				case GetGlobalSlot(slot) => TODO(op)
				case GetLex(typeName) => pp(push(TLexical(AbcSolver getLexical (scopeType, isStatic, typeName) getOrElse error("Could not solve "+typeName+" on "+scopeType+"."))))
				case GetLocal(index: Int) => pp(push(register(index)))
				case GetProperty(property) => {
					property match {
						case multinameL: AbcMultinameL => {
							log.warning("Assuming Array index in GetProperty(%s).", property)
							val index = pop()
							val obj = pop()

							pp(TCall(obj, TGetIndex, index :: Nil, Some(nextOperand)))
						}
						case multiname: AbcMultiname => {
							log.warning("Dynamic variable lookup in GetProperty(%s).", property)
							val obj = pop()
							pp(TCall(obj, TGetProperty, TString(multiname.name) :: Nil, Some(nextOperand)))
						}
						case _ => {
							val obj = pop()
							
							AbcSolver.getProperty(obj.`type`, property) match {
								case Some(method: TaasMethod) => {
									if(method.parameters.length == 0) {
										pp(TCall(obj, method, List.empty, Some(nextOperand)))
									} else {
										pp(push(TClosure(method)))
									}
								}
								case Some(field: TaasField) => pp(TLoad(obj, field, nextOperand))
								case _ => error("Could not find property "+property+" on "+obj.`type`)
							}
						}
					}
				}
				case GetScopeObject(index) => TODO(op)
				case GetSlot(slot) => TODO(op)
				case GetSuper(property) => TODO(op)
				case GreaterEquals() | GreaterThan() => TODO(op)
				case HasNext() => TODO(op)
				case HasNext2(objectRegister, indexRegister) => TODO(op)
				case IfEqual(marker) => pp(if2(TOp_==))
				case IfFalse(marker) => pp(if1(TOp_false))
				case IfGreaterEqual(marker) => pp(if2(TOp_>=))
				case IfGreaterThan(marker) => pp(if2(TOp_>))
				case IfLessEqual(marker) => pp(if2(TOp_<=))
				case IfLessThan(marker) => pp(if2(TOp_<))
				case IfNotEqual(marker) => pp(if2(TOp_!=))
				case IfNotGreaterEqual(marker) => pp(if2(TOp_!>=))
				case IfNotGreaterThan(marker) => pp(if2(TOp_!>))
				case IfNotLessEqual(marker) => pp(if2(TOp_!<=))
				case IfNotLessThan(marker) => pp(if2(TOp_!<))
				case IfStrictEqual(marker) => pp(if2(TOp_===))
				case IfStrictNotEqual(marker) => pp(if2(TOp_!==))
				case IfTrue(marker) => pp(if1(TOp_true))
				case In() => TODO(op)
				case IncLocal(index) => pp(T3(TOp_+, register(index), TInt(1), register(index)))
				case IncLocalInt(index) => pp(T3(TOp_+, register(index), TInt(1), register(index)))
				case Increment() | IncrementInt() => {
					val lhs = pop()
					val result = operand(operandStack)
					operandStack += 1
					pp(T3(TOp_+, lhs, TInt(1), result))
				}
				case InitProperty(property) => {
					val arg = pop()
					val obj = pop()
					AbcSolver.setProperty(obj.`type`, property) match {
						case Some(method: TaasMethod) => pp(TCall(obj, method, arg :: Nil, None))
						case Some(field: TaasField) => pp(TStore(obj, field, arg))
						case _ => error("Could not find property "+property+" on "+obj.`type`)
					}
				}
				case InstanceOf() => TODO(op)
				case IsType(typeName) => TODO(op)
				case IsTypeLate() => TODO(op)
				case Jump(marker) =>
				case Kill(register) =>
				case Label() =>
				case LessEquals() | LessThan() => TODO(op)
				case LookupSwitch(defaultCase, cases) => TODO(op)
				case ShiftLeft() => pp(binop(TOp_<<))
				case Modulo() => pp(binop(TOp_%))
				case Multiply() | MultiplyInt() => pp(binop(TOp_*))
				case Negate() | NegateInt() => TODO(op)
				case NewActivation() => TODO(op)
				case NewArray(numArguments) => {
					//
					// TODO need different strategy
					//
					// push 800
					// newArray(1)
					//
					// Results in register based:
					//
					// r0 = 800
					// r0 = newArray(1)
					//
					// We cannot expand to this:
					//
					// r0 = 800
					// r0 = new Array()
					// r0[0] = r0
					//
					// Hack as:
					//
					// r0 = 800
					// r1 = new Array()
					// r1[0] = r0
					// r0 = r1
					//
					val elements = for(i <- 0 until numArguments) yield pop()
					val result = operandStack
					val arrayRegister = operand(result+numArguments)
					operandStack += 1
					pp(TConstruct(TClass(AbcTypes.fromQName('Array, AbcNamespace(AbcNamespaceKind.Package, Symbol("")))), Nil, arrayRegister))
					elements.zipWithIndex foreach { x => pp(TCall(arrayRegister, TSetIndex, TInt(x._2) :: x._1 :: Nil, None)) }
					pp(T2(TOp_Nothing, arrayRegister, operand(result)))
				}

				case NewCatch(exceptionHandler) => TODO(op)
				case NewClass(nominalType) => TODO(op)
				case NewFunction(function) => TODO(op)
				case NewObject(numArguments) => TODO(op)
				case NextName() => TODO(op)
				case NextValue() => TODO(op)
				case Nop() => TODO(op)
				case Not() => TODO(op)
				case Pop() => pop()
				case PopScope() => TODO(op)
				case PushByte(value) => pp(push(TInt(value)))
				case PushDouble(value) => pp(push(TDouble(value)))
				case PushFalse() => pp(push(TBool(false)))
				case PushInt(value) => pp(push(TInt(value)))
				case PushNamespace(value) => TODO(op)
				case PushNaN() => pp(push(TDouble(Double.NaN)))
				case PushNull() => pp(push(TNull))
				case PushScope() => pop()
				case PushShort(value) => pp(push(TInt(value)))
				case PushString(value) => pp(push(TString(value)))
				case PushTrue() => pp(push(TBool(true)))
				case PushUInt(value) => pp(push(TLong(value)))
				case PushUndefined() | PushWith() => TODO(op)
				case ReturnValue() => pp(TReturn(pop()))
				case ReturnVoid() => pp(TReturn(TVoid))
				case ShiftRight() => pp(binop(TOp_>>))
				case SetLocal(index) => pp(T2(TOp_Nothing, pop(), register(index)))
				case SetGlobalSlot(slot) => TODO(op)
				case SetProperty(property) => {
					val arg = pop()
					property match {
						case multinameL: AbcMultinameL => {
							log.warning("Assuming Array index in SetProperty(%s).", property)
							val index = pop()
							val obj = pop()

							pp(TCall(obj, TSetIndex, index :: arg :: Nil, None))
						}
						case multiname: AbcMultiname => {
							log.warning("Dynamic variable lookup in SetProperty(%s).", property)
							val obj = pop()
							pp(TCall(obj, TSetProperty, TString(multiname.name) :: arg :: Nil, None))
						}
						case _ => val obj = pop()
							AbcSolver.setProperty(obj.`type`, property) match {
								case Some(method: TaasMethod) => pp(TCall(obj, method, arg :: Nil, None))
								case Some(field: TaasField) => pp(TStore(obj, field, arg))
								case _ => error("Could not find property "+property+" on "+obj.`type`)
							}
					}
				}
				case SetSlot(slot) => TODO(op)
				case SetSuper(property) => TODO(op)
				case StrictEquals() => TODO(op)
				case Subtract() | SubtractInt() => pp(binop(TOp_-))
				case Swap() => TODO(op)
				case Throw() | TypeOf() | ShiftRightUnsigned() => TODO(op)
				case SetByte() | SetShort() | SetInt() | SetFloat() | SetDouble() => TODO(op)
				case GetByte() | GetShort() | GetInt() | GetFloat() | GetDouble() => TODO(op)
				case Sign1() | Sign8() | Sign16() => TODO(op)
			}

			if((operandStackBefore + op.operandDelta) != operandStack) {
				log.error("Wrong operand stack delta.")
				log.error("Expected: %d -> %d", operandStackBefore, (operandStackBefore + op.operandDelta))
				log.error("Result: %d -> %d", operandStackBefore, operandStack)
				log.error("List of values on stack:")

				for(i <- 0 until (operandStackBefore + op.operandDelta)) {
					log.error("%s", pop())
				}

				error("Wrong operand-stack delta for "+op+". Got "+operandStackBefore+" -> "+operandStack+", expected "+operandStackBefore+" -> "+(operandStackBefore + op.operandDelta))
			}
		}

		new TaasBlock(r.reverse)
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