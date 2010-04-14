package apparat.abc.analysis

import apparat.abc._
import apparat.bytecode.operations._

object AbcConstantPoolBuilder {
	def using(abc: Abc) = {
		val builder = new AbcConstantPoolBuilder()
		builder add abc
		builder.createPool
	}
}
class AbcConstantPoolBuilder extends AbcVisitor {
	var ints: List[Int] = Nil
	var uints: List[Long] = Nil
	var doubles: List[Double] = Nil
	var strings: List[Symbol] = Nil
	var namespaces: List[AbcNamespace] = Nil
	var nssets: List[AbcNSSet] = Nil
	var names: List[AbcName] = Nil

	def reset = {
		ints = Nil
		uints = Nil
		doubles = Nil
		strings = Nil
		namespaces = Nil
		nssets = Nil
		names = Nil
	}

	def optimize[@specialized B](list: List[B]) = {
		val noDuplicates = list.distinct
		val count = Map(noDuplicates zip (noDuplicates map { x => list count (_ == x) }): _*)
		noDuplicates sortWith { (a, b) => count(a) > count(b) }
	}

	def createPool = {
		import apparat.actors.Futures._
		val intFuture = future { (0 :: optimize(ints)).toArray }
		val uintFuture = future { (0L :: optimize(uints)).toArray }
		val doubleFuture = future { (Double.NaN :: optimize(doubles)).toArray }
		val stringFuture = future { (AbcConstantPool.EMPTY_STRING :: optimize(strings)).toArray }
		val namespaceFuture = future { (AbcConstantPool.EMPTY_NAMESPACE :: optimize(namespaces)).toArray }
		val nssetFuture = future { (AbcConstantPool.EMPTY_NSSET :: optimize(nssets)).toArray }
		val nameFuture = future {
			val noDuplicates = names.distinct
			val count = Map(noDuplicates zip (noDuplicates map { x => names count (_ == x) }): _*)
			(AbcConstantPool.EMPTY_NAME :: noDuplicates.sortWith((a, b) => a match {
				case AbcTypename(_, _) => false
				case other => b match {
					case AbcTypename(_, _) => true
					case _ => count(a) > count(b)
				}
			}).distinct).toArray
		}
		new AbcConstantPool(intFuture(), uintFuture(), doubleFuture(), stringFuture(),
			namespaceFuture(), nssetFuture(), nameFuture())
	}

	def add(abc: Abc): Unit = abc accept this

	def add(value: Int): Unit  = ints = value :: ints

	def add(value: Long): Unit  = uints = value :: uints

	//TODO got compile error when using Double.isNaN -- why? idea/scala bug?
	def add(value: Double): Unit  = if(!java.lang.Double.isNaN(value)) doubles = value :: doubles

	def add(value: Symbol): Unit  = if(value != AbcConstantPool.EMPTY_STRING) strings = value :: strings

	def add(value: AbcNamespace): Unit  = {
		add(value.name)
		if(value != AbcConstantPool.EMPTY_NAMESPACE) namespaces = value :: namespaces
	}

	def add(value: AbcNSSet): Unit  = {
		value.set foreach add
		if(value != AbcConstantPool.EMPTY_NSSET) nssets = value :: nssets
	}

	def add(value: AbcName): Unit = {
		if(value != AbcConstantPool.EMPTY_NAME) names = value :: names

		value match {
			case AbcQName(name, namespace) => {
				add(name)
				add(namespace)
			}
			case AbcQNameA(name, namespace) => {
				add(name)
				add(namespace)
			}
			case AbcRTQName(name) => add(name)
			case AbcRTQNameA(name) => add(name)
			case AbcRTQNameL | AbcRTQNameLA =>
			case AbcMultiname(name, nsset) => {
				add(name)
				add(nsset)
			}
			case AbcMultinameA(name, nsset) => {
				add(name)
				add(nsset)
			}
			case AbcMultinameL(nsset) => add(nsset)
			case AbcMultinameLA(nsset) => add(nsset)
			case AbcTypename(name, parameters) => {
				add(name)
				parameters foreach add
			}
		}
	}

	override def visit(value: AbcConstantPool): Unit = {}

	override def visit(value: AbcExceptionHandler): Unit = {
		add(value.typeName)
		add(value.varName)
	}

	override def visit(value: AbcInstance): Unit = {
		add(value.name)

		value.base match {
			case Some(base) => add(base)
			case None =>
		}

		value.protectedNs match {
			case Some(namespace) => add(namespace)
			case None =>
		}

		value.interfaces foreach add
	}

	override def visit(value: AbcMetadata): Unit = {
		add(value.name)

		for((key, value) <- value.attributes) {
			add(key)
			add(value)
		}
	}

	override def visit(value: AbcMethod): Unit = {
		add(value.returnType)
		add(value.name)
	}

	override def visit(value: AbcMethodBody): Unit = {
		value.bytecode match {
			case Some(bytecode) => {
				bytecode.ops foreach {
					case Add() | AddInt() => 
					case ApplyType(_) => 
					case AsType(typeName) => add(typeName)
					case AsTypeLate() => 
					case BitAnd() | BitNot() | BitOr() | BitXor() => 
					case Breakpoint() | BreakpointLine() => 
					case Call(_) => 
					case CallMethod(_, _) => 
					case CallProperty(property, _) => add(property)
					case CallPropLex(property, _) => add(property)
					case CallPropVoid(property, _) => add(property)
					case CallStatic(_, _) => 
					case CallSuper(property, _) => add(property)
					case CallSuperVoid(property, _) => add(property)
					case CheckFilter() => 
					case Coerce(typeName) => add(typeName)
					case CoerceAny() | CoerceBoolean() | CoerceDouble() | CoerceInt() | CoerceObject() | CoerceString() | CoerceUInt() => 
					case Construct(_) => 
					case ConstructProp(property, _) => add(property)
					case ConstructSuper(_) => 
					case ConvertBoolean() | ConvertDouble() | ConvertInt() | ConvertObject() | ConvertString() | ConvertUInt() => 
					case Debug(_, name, _, _) => add(name)
					case DebugFile(file) => add(file)
					case DebugLine(_) => 
					case DecLocal(_) => 
					case DecLocalInt(_) => 
					case Decrement() | DecrementInt() => 
					case DeleteProperty(property) => add(property)
					case Divide() => 
					case Dup() => 
					case DefaultXMLNamespace(uri) => add(uri)
					case DefaultXMLNamespaceLate() => 
					case Equals() => 
					case EscapeXMLAttribute() => 
					case EscapeXMLElement() => 
					case FindProperty(property) => add(property)
					case FindPropStrict(property) => add(property)
					case GetDescendants(property) => add(property)
					case GetGlobalScope() => 
					case GetGlobalSlot(_) => 
					case GetLex(typeName) => add(typeName)
					case GetLocal(_) => 
					case GetProperty(property) => add(property)
					case GetScopeObject(_) => 
					case GetSlot(_) => 
					case GetSuper(property) => add(property)
					case GreaterEquals() | GreaterThan() => 
					case HasNext() => 
					case HasNext2(_, _) => 
					case IfEqual(_) => 
					case IfFalse(_) => 
					case IfGreaterEqual(_) => 
					case IfGreaterThan(_) => 
					case IfLessEqual(_) => 
					case IfLessThan(_) => 
					case IfNotEqual(_) => 
					case IfNotGreaterEqual(_) => 
					case IfNotGreaterThan(_) => 
					case IfNotLessEqual(_) => 
					case IfNotLessThan(_) => 
					case IfStrictEqual(_) => 
					case IfStrictNotEqual(_) => 
					case IfTrue(_) => 
					case In() => 
					case IncLocal(_) => 
					case IncLocalInt(_) => 
					case Increment() | IncrementInt() => 
					case InitProperty(property) => add(property)
					case InstanceOf() => 
					case IsType(typeName) => add(typeName)
					case IsTypeLate() => 
					case Jump(_) => 
					case Kill(_) => 
					case Label() => 
					case LessEquals() | LessThan() => 
					case LookupSwitch(_, _) => 
					case ShiftLeft() | Modulo() | Multiply() | MultiplyInt() | Negate() | NegateInt() => 
					case NewActivation() => 
					case NewArray(_) => 
					case NewCatch(_) => 
					case NewClass(_) => 
					case NewFunction(_) => 
					case NewObject(_) => 
					case NextName() | NextValue() | Nop() | Not() | Pop() | PopScope() => 
					case PushByte(_) => 
					case PushDouble(value) => add(value)
					case PushFalse() => 
					case PushInt(value) => add(value)
					case PushNamespace(value) => add(value)
					case PushNaN() | PushNull() | PushScope() => 
					case PushShort(_) => 
					case PushString(value) => add(value)
					case PushTrue() => 
					case PushUInt(value) => add(value)
					case PushUndefined() | PushWith() => 
					case ReturnValue() | ReturnVoid() => 
					case ShiftRight() => 
					case SetLocal(_) => 
					case SetGlobalSlot(_) => 
					case SetProperty(property) => add(property)
					case SetSlot(_) => 
					case SetSuper(property) => add(property)
					case StrictEquals() | Subtract() | SubtractInt() | Swap() => 
					case Throw() | TypeOf() | ShiftRightUnsigned() => 
					case SetByte() | SetShort() | SetInt() | SetFloat() | SetDouble() => 
					case GetByte() | GetShort() | GetInt() | GetFloat() | GetDouble() => 
					case Sign1() | Sign8() | Sign16() => 
				}
			}
			case None =>
		}
	}

	override def visit(value: AbcMethodParameter): Unit = {
		add(value.typeName)

		value.name match {
			case Some(name) => add(name)
			case None =>
		}

		if(value.optional) {
			error("TODO")
			//TODO add optional
		}
	}

	override def visit(value: AbcTrait): Unit = {
		add(value.name)

		value match {
			case slot: AbcTraitAnySlot => {
				add(slot.typeName)
				slot.value match {
					case Some(value) => error("TODO")//TODO add optional
					case None =>
				}
			}
			case _ => 
		}
	}
}