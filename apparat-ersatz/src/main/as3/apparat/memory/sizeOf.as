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
 * Author : Patrick Le Clec'h
 */

package apparat.memory {
       /**
         * The sizeOf function return the size of a class of type Structure
         * <pre>
         *  size of basic type:
         *    byte   => 1
		 *    float  => 4
		 *    double => 8
		 *    int    => 4
		 *    uint   => 4
         *    Number => 8
         * </pre>
         * @author Patrick Le Clec'h
         * @see Structure
         * @see sizeOf
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
    public function sizeOf(clazz:Class):int {return 0;}
}
