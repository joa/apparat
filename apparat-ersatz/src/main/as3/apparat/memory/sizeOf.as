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
     * The sizeOf function returns the size of a Structure type.
     *
     * A Structure consists of multiple arbitrary types. The sum of
     * the size of those basic types is the total size of the Structure.
     *
     * <table>
     * <tr><th>Type</th><th>Size</th></tr>
     * <tr><td>byte</td><td>1</td></tr>
     * <tr><td>float</td><td>4</td></tr>
     * <tr><td>double</td><td>8</td></tr>
     * <tr><td>int</td><td>4</td></tr>
     * <tr><td>uint</td><td>4</td></tr>
     * <tr><td>Number</td><td>8</td></tr>
     * </table>
     *
     * @author Patrick Le Clec'h
     * @see Structure
     * @return The size of the given Structure.
     *
     * @example
     * <pre>
     * class CPoint extends Structure {
     *    Map(type='float', pos=0)
     *    public var x:Number;
     *    Map(type='float', pos=1)
     *    public var y:Number;
     * }
     *
     * var len:int=sizeOf(CPoint); // will return 8
     * </pre>
     */
    public function sizeOf(klass: Class): int {
        return 0
    }
}
