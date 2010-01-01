package apparat.bytecode

import collection.mutable.ListBuffer
import operations._
import java.io.ByteArrayInputStream
import apparat.abc.{AbcMethodBody, AbcMethod, AbcInputStream, AbcConstantPool}
import annotation.tailrec

object Bytecode {
	def fromMethod(method: AbcMethod) = method.body match {
		case Some(body) => fromBody(body)
		case None => None
	}

	def fromBody(body: AbcMethodBody) = {
		val input = new AbcInputStream(new ByteArrayInputStream(body.code))
		val cpool = null.asInstanceOf[AbcConstantPool]

		def u08 = input.readU08()
		def u30 = input.readU30()
		def name = cpool.names(u30)
		def string = cpool.strings(u30)
		def numArguments = u30
		def register = u30
		def property = name
		def nextOp: AbstractOp = input.readU08() match {
			case Op.`throw` => Throw()
			case Op.add => Add()
			case Op.add_i => AddInt()
			case Op.add_p => error("add_p")
			case Op.applytype => ApplyType(numArguments)
			case Op.astype => AsType(name)
			case Op.astypelate => AsTypeLate()
			case Op.bitand => BitAnd()
			case Op.bitnot => BitNot()
			case Op.bitor => BitOr()
			case Op.bitxor => BitXor()
			case Op.bkpt => Breakpoint()
			case Op.bkptline => BreakpointLine()
			case Op.call => Call(numArguments)
			case Op.callmethod => CallMethod(numArguments,u30)
			case Op.callproperty => CallProperty(numArguments, property)
			case Op.callproplex => CallPropLex(numArguments, property)
			case Op.callpropvoid => CallPropVoid(numArguments, property)
			case Op.callstatic => CallStatic(numArguments, abc methods u30)
			case Op.callsuper => CallSuper(numArguments, property)
			case Op.callsupervoid => CallSuperVoid(numArguments, property)
			case Op.checkfilter => CheckFilter()
			case Op.coerce => Coerce(name)
			case Op.coerce_a => CoerceAny()
			case Op.coerce_b => error("coerce_b")
			case Op.coerce_d => error("coerce_d")
			case Op.coerce_i => error("coerce_i")
			case Op.coerce_o => error("coerce_o")
			case Op.coerce_s => CoerceString()
			case Op.coerce_u => error("coerce_u")
			case Op.construct => Construct(numArguments)
			case Op.constructprop => ConstructProp(numArguments, property)
			case Op.constructsuper => ConstructSuper(numArguments)
			case Op.convert_b => ConvertBoolean()
			case Op.convert_d => ConvertDouble()
			case Op.convert_i => ConvertInt()
			case Op.convert_m => error("convert_m")
			case Op.convert_m_p => error("convet_m_p")
			case Op.convert_o => ConvertObject()
			case Op.convert_u => ConvertUInt()
			case Op.debug => Debug(u08, string, u08, u30)
			case Op.debugfile => DebugFile(string)
			case Op.debugline => DebugLine(u30)
			case Op.declocal => DecLocal(register)
			case Op.declocal_i => DecLocalInt(register)
			case Op.declocal_p => error("declocal_p")
			case Op.decrement => Decrement()
			case Op.decrement_i => DecrementInt()
			case Op.decrement_p => error("decrement_p")
			case Op.deldescendants => error("deldescendants")
			case Op.deleteproperty => DeleteProperty(property)
			case Op.divide => Divide()
			case Op.divide_p => error("divide_p")
			case Op.dup => Dup()
			case Op.dxns => DefaultXMLNamespace(string)
			case Op.dxnslate => DefaultXMLNamespaceLate()
			case Op.equals => Equals()
			case Op.esc_xattr => EscapeXMLAttribute()
			case Op.esc_xelem => EscapeXMLElement()
			case Op.finddef => error("finddef")
			case Op.findproperty => FindProperty(property)
			case Op.findpropstrict => FindPropStrict(property)
			case Op.getdescendants => GetDescendants(property)
			case Op.getglobalscope => GetGlobalScope()
			case Op.getglobalslot => GetGlobalSlot()
			case Op.getlex => GetLex(name)
			case Op.getlocal => GetLocal(register)
			case Op.getlocal0 => GetLocal(0)
			case Op.getlocal1 => GetLocal(1)
			case Op.getlocal2 => GetLocal(2)
			case Op.getlocal3 => GetLocal(3)
			case Op.getproperty => GetProperty(property)
			case Op.getscopeobject => GetScopeObject(u08)
			case Op.getslot => GetSlot()
			case Op.getsuper => GetSuper(property)
			case Op.greaterequals => GreaterEquals()
			case Op.greaterthan => GreaterThan()

			case Op.pushbyte => PushByte(u08 match  {
				case x if (x & 0x80) != 0 => (x & 0x7f) - 0x80
				case y => y
			})
			case Op.pushshort => PushShort(input.readU16() match {
				case x if (x & 0x8000) != 0 => (x & 0x7fff) - 0x8000
				case y => y
			})

			case x => error("Unknown opcode " + x + ".")
		}

		@tailrec def build(list: ListBuffer[AbstractOp]): ListBuffer[AbstractOp] = {
			input.available match {
				case 0 => list
				case _ => build(list += nextOp)
			}
		}

		try {
			build(new ListBuffer[AbstractOp]())
		}
		finally {
			try { input.close() } catch { case _ => {} }
		}
	}
}

class Bytecode {
	private val code = new ListBuffer[AbstractOp]()
}