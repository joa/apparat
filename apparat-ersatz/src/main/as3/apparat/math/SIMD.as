/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.math {
  import apparat.asm.*
  import apparat.inline.Macro

  /**
   * The SIMD macro is a collection of single instructions
   * for multiple data.
   *
   * <p>All instructions are performed four times with respect
   * to the size of the datatype. Methods prefixed with <em>d</em>
   * are double based (8b), methods prefixed with <em>f</em>
   * are float based (4b) and methods prefixed with <em>i</em> are
   * integer based (4b).</p>
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
   * instructions and <code>initMath</code>.
   *
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

    /**
     * Performs the operation <code>target = Math.sin(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.dsin(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function dsin(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetDouble,
        CallProperty(__as3(Math.sin), 1),
        GetLocal(target),
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.sin), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.sin), 1),
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.sin), 1),
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = Math.asin(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.dasin(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function dasin(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetDouble,
        CallProperty(__as3(Math.asin), 1),
        GetLocal(target),
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.asin), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.asin), 1),
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.asin), 1),
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = Math.cos(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.dcos(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function dcos(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetDouble,
        CallProperty(__as3(Math.cos), 1),
        GetLocal(target),
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.cos), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.cos), 1),
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.cos), 1),
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = Math.acos(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.dacos(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function dacos(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetDouble,
        CallProperty(__as3(Math.acos), 1),
        GetLocal(target),
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.acos), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.acos), 1),
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.acos), 1),
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = Math.sqrt(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.dsqrt(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function dsqrt(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetDouble,
        CallProperty(__as3(Math.sqrt), 1),
        GetLocal(target),
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.sqrt), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.sqrt), 1),
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetDouble,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        CallProperty(__as3(Math.sqrt), 1),
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetDouble
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
        PushByte(0x04),
        AddInt,
        GetFloat,
        GetLocal(target),
        PushByte(0x04),
        AddInt,
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
        PushByte(0x0c),
        AddInt,
        GetFloat,
        GetLocal(target),
        PushByte(0x0c),
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
        PushByte(0x04),
        AddInt,
        SetFloat,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetFloat,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x0c),
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
        PushByte(0x04),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
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
        PushByte(0x0c),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x0c),
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
        PushByte(0x04),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
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
        PushByte(0x0c),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x0c),
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
        PushByte(0x04),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
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
        PushByte(0x0c),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x0c),
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
        PushByte(0x04),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
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
        PushByte(0x0c),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x0c),
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
        PushByte(0x04),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
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
        PushByte(0x0c),
        AddInt,
        Dup,
        GetFloat,
        GetLocal(source),
        PushByte(0x0c),
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
        PushByte(0x04),
        AddInt,
        GetFloat,
        SetLocal(r1),

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        SetLocal(r2),

        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetFloat,
        SetLocal(r3)
      )
    }

    /**
     * Performs the operation <code>target = Math.sin(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.fsin(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function fsin(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetFloat,
        CallProperty(__as3(Math.sin), 1),
        GetLocal(target),
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.sin), 1),
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.sin), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.sin), 1),
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = Math.asin(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.fasin(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function fasin(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetFloat,
        CallProperty(__as3(Math.asin), 1),
        GetLocal(target),
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.asin), 1),
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.asin), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.asin), 1),
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = Math.cos(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.fcos(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function fcos(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetFloat,
        CallProperty(__as3(Math.cos), 1),
        GetLocal(target),
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.cos), 1),
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.cos), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.cos), 1),
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = Math.acos(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.facos(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function facos(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetFloat,
        CallProperty(__as3(Math.acos), 1),
        GetLocal(target),
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.acos), 1),
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.acos), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.acos), 1),
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = Math.sqrt(source)</code>.
     *
     * <p>You can initialize the local math object with the following code:</p>
     * <p><pre>
     * var math: Class
     * SIMD.initMath(math)
     * SIMD.fsqrt(0, 0, math)
     * </pre></p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     * @param math The local Math object.
     */
    public static function fsqrt(target: int, source: int, math: *): void {
      __asm(
        GetLocal(math),
        GetLocal(source),
        GetFloat,
        CallProperty(__as3(Math.sqrt), 1),
        GetLocal(target),
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.sqrt), 1),
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.sqrt), 1),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetFloat,

        GetLocal(math),
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetFloat,
        CallProperty(__as3(Math.sqrt), 1),
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetFloat
      )
    }

    //////////////////////////////////////////////////
    // INTEGER
    //////////////////////////////////////////////////

    /**
     * Performs the operation <code>target = source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function imov(target:  int, source: int): void {
      __asm(
        GetLocal(source),
        GetInt,
        GetLocal(target),
        SetInt,

        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetInt,

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetInt,

        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = value</code>.
     *
     * @param target Target address in domain memory.
     * @param value The value to set.
     */
    public static function iset(target: int, value: int): void {
      __asm(
        GetLocal(value),
        GetLocal(target),
        SetInt,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetInt,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetInt,

        GetLocal(value),
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target + source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function iadd(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        GetLocal(source),
        GetInt,
        AddInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        AddInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        AddInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        AddInt,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target - source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function isub(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        GetLocal(source),
        GetInt,
        SubtractInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        SubtractInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        SubtractInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        SubtractInt,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target * source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function imul(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        GetLocal(source),
        GetInt,
        MultiplyInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        MultiplyInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        MultiplyInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        MultiplyInt,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target / source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function idiv(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        ConvertDouble,
        GetLocal(source),
        GetInt,
        ConvertDouble,
        Divide,
        ConvertInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        ConvertDouble,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        ConvertDouble,
        Divide,
        ConvertInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        ConvertDouble,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        ConvertDouble,
        Divide,
        ConvertInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        ConvertDouble,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        ConvertDouble,
        Divide,
        ConvertInt,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target % source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function imod(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        ConvertDouble,
        GetLocal(source),
        GetInt,
        ConvertDouble,
        Modulo,
        ConvertInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        ConvertDouble,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        ConvertDouble,
        Modulo,
        ConvertInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        ConvertDouble,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        ConvertDouble,
        Modulo,
        ConvertInt,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        ConvertDouble,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        ConvertDouble,
        Modulo,
        ConvertInt,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target << source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function ishl(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        GetLocal(source),
        GetInt,
        ShiftLeft,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        ShiftLeft,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        ShiftLeft,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        ShiftLeft,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target >> source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function ishr(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        GetLocal(source),
        GetInt,
        ShiftRight,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        ShiftRight,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        ShiftRight,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        ShiftRight,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target & source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function iand(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        GetLocal(source),
        GetInt,
        BitAnd,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        BitAnd,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        BitAnd,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        BitAnd,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target | source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function ior(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        GetLocal(source),
        GetInt,
        BitOr,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        BitOr,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        BitOr,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        BitOr,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = target ^ source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function ixor(target: int, source: int): void {
      __asm(
        GetLocal(target),
        Dup,
        GetInt,
        GetLocal(source),
        GetInt,
        BitXor,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x04),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        BitXor,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x08),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        BitXor,
        Swap,
        SetInt,

        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        Dup,
        GetInt,
        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        BitXor,
        Swap,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = ~source</code>.
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function inot(target: int, source: int): void {
      __asm(
        GetLocal(source),
        GetInt,
        BitNot,
        GetLocal(target),
        SetInt,

        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        BitNot,
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetInt,

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        BitNot,
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetInt,

        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        BitNot,
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetInt
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
    public static function iget(source: int, r0: int, r1: int, r2: int, r3: int): void {
      __asm(
        GetLocal(source),
        GetInt,
        SetLocal(r0),

        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        SetLocal(r1),

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        SetLocal(r2),

        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        SetLocal(r3)
      )
    }

    //////////////////////////////////////////////////
    // GENERIC
    //////////////////////////////////////////////////

    /**
     * Initializes the local Math object.
     *
     * <p>Once you have initialized the Math object it can
     * be used with any method as often as you want. The
     * following code would be valid for instance.</p>
     *
     * <p><pre>
     * var math: Class
     * var r0: Number, r1: Number, r2: Number, r3: Number
     *
     * SIMD.initMath(math)
     *
     * SIMD.fset(0, Math.PI)
     * SIMD.fcos(0, 0, math)
     * SIMD.facos(0, 0, math)
     * SIMD.fsqrt(0, 0, math)
     * SIMD.fget(0, r0, r1, r2, r3)
     *
     * trace(r0, r1, r2, r3)
     * </pre></p>
     *
     * @param math A register to store the Math object.
     */
    public static function initMath(math: *): void {
      __asm(
        __as3(Math),
        SetLocal(math)
      )
    }

    /**
     * Performs the operation <code>target = float(source)</code>.
     *
     * <p>Note that the source is treated like an integer and converted
     * to float.</p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function i2f(target: int, source: int): void {
      __asm(
        GetLocal(source),
        GetInt,
        ConvertDouble,
        GetLocal(target),
        SetFloat,

        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        ConvertDouble,
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetFloat,

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        ConvertDouble,
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetFloat,

        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        ConvertDouble,
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetFloat
      )
    }

    /**
     * Performs the operation <code>target = int(source)</code>.
     *
     * <p>Note that the source is treated like a float and converted
     * to int.</p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function f2i(target: int, source: int): void {
      __asm(
        GetLocal(source),
        GetFloat,
        ConvertInt,
        GetLocal(target),
        SetInt,

        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetFloat,
        ConvertInt,
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetInt,

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetFloat,
        ConvertInt,
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetInt,

        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetFloat,
        ConvertInt,
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetInt
      )
    }

    /**
     * Performs the operation <code>target = int(source)</code>.
     *
     * <p>Note that the source is treated like an integer and converted
     * to double. Remember also that the target registers are double based
     * and 8b long whereas the source registers are integer based and only
     * 4b long.</p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function i2d(target: int, source: int): void {
      __asm(
        GetLocal(source),
        GetInt,
        ConvertDouble,
        GetLocal(target),
        SetDouble,

        GetLocal(source),
        PushByte(0x04),
        AddInt,
        GetInt,
        ConvertDouble,
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetDouble,

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetInt,
        ConvertDouble,
        GetLocal(target),
        PushByte(0x10),
        AddInt,
        SetDouble,

        GetLocal(source),
        PushByte(0x0c),
        AddInt,
        GetInt,
        ConvertDouble,
        GetLocal(target),
        PushByte(0x18),
        AddInt,
        SetDouble
      )
    }

    /**
     * Performs the operation <code>target = int(source)</code>.
     *
     * <p>Note that the source is treated like a double and converted
     * to int. Remember also that the target registers are integer based
     * and 4b long whereas the source registers are double based and
     * 8b long.</p>
     *
     * @param target Target address in domain memory.
     * @param source Source address in domain memory.
     */
    public static function d2i(target: int, source: int): void {
      __asm(
        GetLocal(source),
        GetDouble,
        ConvertInt,
        GetLocal(target),
        SetInt,

        GetLocal(source),
        PushByte(0x08),
        AddInt,
        GetDouble,
        ConvertInt,
        GetLocal(target),
        PushByte(0x04),
        AddInt,
        SetInt,

        GetLocal(source),
        PushByte(0x10),
        AddInt,
        GetDouble,
        ConvertInt,
        GetLocal(target),
        PushByte(0x08),
        AddInt,
        SetInt,

        GetLocal(source),
        PushByte(0x18),
        AddInt,
        GetDouble,
        ConvertInt,
        GetLocal(target),
        PushByte(0x0c),
        AddInt,
        SetInt
      )
    }
  }
}
