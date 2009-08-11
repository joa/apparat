package com.joa_ebert.abc.bytecode.asbridge 
{
	/**
	 * Injects a set of raw bytes.
	 * 
	 * <p>The <code>__bytecode</code> method can not be compared with the
	 * <code>__asm</code> method since
	 * the <code>__bytecode</code> method requires from the developer to write
	 * the exact bytes required. This means an operation like PushInt becomes
	 * nearly impossible to inject using <code>__bytecode</code> since one
	 * would have to know the constant pool index of the integer in advance.</p>
	 * 
	 * @param bytes The bytes to inject.
	 * 
	 * @author Joa Ebert
	 */
	public function __bytecode( ... bytes ): void {}
}
