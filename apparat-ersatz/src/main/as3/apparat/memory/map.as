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
package apparat.memory {
    /**
     * The map function allows you to mimic a C <code>struct</code> using Alchemy memory.
     *
     * <p>TurboDieselSportInjection expands the map method and
     * creates a new internal pointer that will be use to navigate within the memory buffer.</p>
     *
     * @author Patrick Le Clec'h
     *
     * @see Structure
     * @see sizeOf
     * @param pointer The pointer in memory.
     * @param klass A Structure type.
     *
     * @example
     * <pre>
     * class CPoint extends Structure {
     *    [Map(type='float', pos=0)]
     *    public var x:Number;
     *    [Map(type='float', pos=1)]
     *    public var y:Number;
     * }
     *
     * var ram:ByteArray=new ByteArray();
     * ram.length = sizeOf(CPoint) * 10; //Allocate 10 points in Alchemy memory
     * Memory.select(ram);
     *
     * var pt:Point=map(0, Point); //Init pt as a memory access
     *
     * pt.y = 3.14;  //Set Alchemy memory at index 4 to be a float with the value 3.14
     * pt.next(); //Advance the internal pointer to the size of the Structure, here 8
     *
     * pt.x += Math.random() * 5; //Read and set the memory at index 8
     * </pre>
     */
    public function map(pointer: int, klass: Class): * {
        return null
    }
}
