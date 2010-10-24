package apparat.memory {
	import apparat.asm.AddInt;
	import apparat.asm.BitOr;
	import apparat.asm.DecLocalInt;
	import apparat.asm.Dup;
	import apparat.asm.GetByte;
	import apparat.asm.GetInt;
	import apparat.asm.GetShort;
	import apparat.asm.IfEqual;
	import apparat.asm.IncLocalInt;
	import apparat.asm.IncrementInt;
	import apparat.asm.Jump;
	import apparat.asm.LookupSwitch;
	import apparat.asm.SetByte;
	import apparat.asm.SetInt;
	import apparat.asm.SetLocal;
	import apparat.asm.SetShort;
	import apparat.asm.ShiftLeft;
	import apparat.asm.__as3;
	import apparat.asm.__asm;

	public function memset(dst:int, value:int, size:int):void {
		var rest:int = size & 7;
		size = size >> 3;
		__asm(
				__as3(value),
				__as3(value),
				__as3(8),
				ShiftLeft,
				BitOr,
				Dup,
				__as3(16),
				ShiftLeft,
				BitOr,
				SetLocal(value),
				"loop:",
				__as3(size),
				__as3(0),
				IfEqual("copyRest"),
				__as3(value),
				__as3(dst),
				SetInt,
				__as3(dst),
				__as3(4),
				AddInt,
				SetLocal(dst),
				__as3(value),
				__as3(dst),
				SetInt,
				__as3(dst),
				__as3(4),
				AddInt,
				SetLocal(dst),
				DecLocalInt(size),
				Jump("loop"),
				"copyRest:",
				__as3(rest),
				LookupSwitch("0", "0", "1", "2", "3", "4", "5", "6", "7"),
				"7:",
				__as3(value),
				__as3(dst),
				SetByte,
				__as3(value),
				IncLocalInt(dst),
				__as3(dst),
				SetShort,
				__as3(value),
				__as3(dst),
				IncrementInt,
				IncrementInt,
				SetInt,
				Jump("0"),
				"6:",
				__as3(value),
				__as3(dst),
				SetShort,
				__as3(value),
				__as3(dst),
				IncrementInt,
				IncrementInt,
				SetInt,
				Jump("0"),
				"5:",
				__as3(value),
				__as3(dst),
				SetByte,
				__as3(value),
				__as3(dst),
				IncrementInt,
				SetInt,
				Jump("0"),
				"4:",
				__as3(value),
				__as3(dst),
				SetInt,
				Jump("0"),
				"3:",
				__as3(value),
				__as3(dst),
				SetByte,
				__as3(value),
				__as3(dst),
				IncrementInt,
				SetShort,
				Jump("0"),
				"2:",
				__as3(value),
				__as3(dst),
				SetShort,
				Jump("0"),
				"1:",
				__as3(value),
				__as3(dst),
				SetByte,
				"0:"
				);
	}
}