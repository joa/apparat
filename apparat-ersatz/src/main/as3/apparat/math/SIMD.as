package apparat.math {
  import apparat.asm.*
  import apparat.inline.Macro

  /**
   * The SIMD macro is a collection of single instructions
   * for multiple data.
   *
   * <p>All instructions are performed four times with respect
   * to the size of the datatype. Methods prefixed with <em>d</em>
   * are double based (8b) and methods prefixed with <em>f</em>
   * are float based (4b).</p>
   *
   * <p>To illustrate this, here is an example of <code>dmov</code>
   * using the <code>Memory</code> class:</p>
   *
   * <p><pre>
   * Memory.writeDouble(Memory.readDouble(source), target)
   * Memory.writeDouble(Memory.readDouble(source + 8), target + 8)
   * Memory.writeDouble(Memory.readDouble(source + 16), target + 16)
   * Memory.writeDouble(Memory.readDouble(source + 24), target + 24)
   * </pre></p>
   *
   * <p>This code is implemented in SIMD as well with some special
   * optimizations applied. Although SIMD is a macro it will never
   * modify any incoming parameters besides in the <code>*get</code>
   * instructions.
   * You also do not have to fear register pressure since all
   * instructions are implemented without adding any extra registers.</p>
   *
   * @see apparat.memory.Memory
   */
  public class SIMD extends Macro {
    //////////////////////////////////////////////////
    // DOUBLE
    //////////////////////////////////////////////////
    
    /**
     * Performs the operation <code>target = source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function dmov(target:  int, source: int): void {
      __asm(
        GetLocal(source),
        GetDouble,
        GetLocal(target),
        SetDouble,

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetDouble,

        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetDouble,

        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = value</code>.
     *
     * @param target Target address in domain memory.
     * @param value The value to set.
     */
    public static function dset(target: int, value: Number): void {
      __asm(
        GetLocal(value),
        GetLocal(target),
        SetDouble,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetDouble,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetDouble,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = target + source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function dadd(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetDouble,
        GetLocal(source),
        GetDouble,
        Add,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        Add,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        Add,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        Add,
        Swap,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = target - source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function dsub(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetDouble,
        GetLocal(source),
        GetDouble,
        Subtract,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        Subtract,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        Subtract,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        Subtract,
        Swap,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = target * source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function dmul(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetDouble,
        GetLocal(source),
        GetDouble,
        Multiply,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        Multiply,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        Multiply,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        Multiply,
        Swap,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = target / source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function ddiv(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetDouble,
        GetLocal(source),
        GetDouble,
        Divide,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        Divide,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        Divide,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        Divide,
        Swap,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = target % source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function dmod(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetDouble,
        GetLocal(source),
        GetDouble,
        Modulo,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        Modulo,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        Modulo,
        Swap,
        SetDouble,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetDouble,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        Modulo,
        Swap,
        SetDouble
      )
    }

    /**
     * Assigns the double values at target address to the given result registers.
     * 
     * @param source The source address in domain memory.
     * @param r0 The register which will receive the first value.
     * @param r1 The register which will receive the second value.
     * @param r2 The register which will receive the third value.
     * @param r3 The register which will receive the fourth value.
     */
    public static function dget(source: int, 
                                r0: Number, r1: Number, r2: Number, r3: Number): void {
      __asm(
        GetLocal(source),
        GetDouble,
        SetLocal(r0),
        
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        SetLocal(r1),
        
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        SetLocal(r2),
        
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        SetLocal(r3)
      )
    }
    
    //////////////////////////////////////////////////
    // FLOAT
    //////////////////////////////////////////////////
    
    /**
     * Performs the operation <code>target = source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function fmov(target:  int, source: int): void {
      __asm(
        GetLocal(source),
        GetFloat,
        GetLocal(target),
        SetFloat,

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetFloat,

        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetFloat,
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetFloat,

        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetFloat,
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = value</code>.
     *
     * @param target Target address in domain memory.
     * @param value The value to set.
     */
    public static function fset(target: int, value: Number): void {
      __asm(
        GetLocal(value),
        GetLocal(target),
        SetFloat,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetFloat,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetFloat,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = target + source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function fadd(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetFloat,
        GetLocal(source),
        GetFloat,
        Add,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        Add,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetFloat,
        Add,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetFloat,
        Add,
        Swap,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = target - source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function fsub(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetFloat,
        GetLocal(source),
        GetFloat,
        Subtract,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        Subtract,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetFloat,
        Subtract,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetFloat,
        Subtract,
        Swap,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = target * source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function fmul(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetFloat,
        GetLocal(source),
        GetFloat,
        Multiply,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        Multiply,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetFloat,
        Multiply,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetFloat,
        Multiply,
        Swap,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = target / source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function fdiv(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetFloat,
        GetLocal(source),
        GetFloat,
        Divide,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        Divide,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetFloat,
        Divide,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetFloat,
        Divide,
        Swap,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = target % source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function fmod(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetFloat,
        GetLocal(source),
        GetFloat,
        Modulo,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        Modulo,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x10),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetFloat,
        Modulo,
        Swap,
        SetFloat,

        GetLocal(target),
        PushByte(0x18),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetFloat,
        Modulo,
        Swap,
        SetFloat
      )
    }

    /**
     * Assigns the double values at target address to the given result registers.
     * 
     * @param source The source address in domain memory.
     * @param r0 The register which will receive the first value.
     * @param r1 The register which will receive the second value.
     * @param r2 The register which will receive the third value.
     * @param r3 The register which will receive the fourth value.
     */
    public static function fget(source: int, 
                                r0: Number, r1: Number, r2: Number, r3: Number): void {
      __asm(
        GetLocal(source),
        GetFloat,
        SetLocal(r0),
        
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        SetLocal(r1),
        
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetFloat,
        SetLocal(r2),
        
        GetLocal(source),
        PushByte(0x18),
        
        AddInt,
        GetFloat,
        SetLocal(r3)
      )
    }
  }
}