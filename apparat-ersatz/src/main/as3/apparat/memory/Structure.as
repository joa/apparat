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
         * Extending the Structure class will let you use alchemy memory without needing to know field offset, index...
		 *
         * <p>TurboDieselSportInjection will expand all the operation to use alchemy memory.</p>
		 * <p>You can specify with the optional metadata Map, a type and a position within the memory.
		 * You have to know that if you don't specify a position there is no guarantee that a field will be before another
		 * </p>
		 * <p>By default in alchemy memory a Number is considered as a double, int as int, uint as int</p>
		 * <p>Type accepted within the metadata : <br/>
		 *  <pre>
		 *   byte, float, double, int, uint
		 *  </pre>
		 * </p>
		 * <p> position within the metadata can overlapp, so you can assign different type to a field (as union in C)
		 *  example: <br/>
		 *  <pre>
		 *   public final class Union extends Structure {
		 *     [Map(pos=0)]
		 *     public var x:Number; // considered as a double
		 *
		 *	   [Map(pos=0, type='byte')]
		 *	   public var bx:int;
         *
		 *	   [Map(pos=0, type='float')]
		 *	   public var fx:Number;
		 *	}
		 *  </pre>
		 *  Union have 3 fields at the same position but with 3 differents types, the size of the field is the maximum of all the size
		 * </p>
		 *
         * @author Patrick Le Clec'h
         * @see Structure
         * @see sizeOf
         *
         * @example
         * <pre>
         * public final class CPoint extends Structure {
         *    [Map(type='float', pos=0)]
         *    public var x:Number;
         *    [Map(type='float', pos=1)]
         *    public var y:Number;
		 *
		 *    public var z:Number; // z will be place after all the positionned field (x,y) and will be considered as double
		 *
		 *    public var visible:int; // visible will be place after all positionned field(x,y), but depending on the compiler before or after z field
		 *                            // since there is no type specified it will take the int type
         * }
         *
         * </pre>
         */
    public class Structure {
		 /**
		  * Advance the internal pointer of the size of the Structure.
          *
          */
        public function next():void{}

		/**
		  * Backward the internal pointer of the size of the Structure.
          *
          */
        public function prev():void{}

		/**
          * @return the value of the internal pointer.
          */
        public function internalPtr():int{return 0;}

		 /**
		  * Set the internal pointer to the value*sizeOf(Structure)
          *
          * @param ptr value of the internal pointer.
          */
        public function seekTo(ptr:int):void{}

		 /**
		  * Adjust the internal value by the delta*sizeOf(Structure)
          *
          * @param delta value to add or substract to the internal pointer.
          */
        public function seekBy(delta:int):void{}

		 /**
		  * Set the internal pointer to value in bytes
          *
          * @param ptr value of the internal pointer.
          */
        public function offsetTo(ptr:int):void{}

		 /**
		  * Adjust the internal value by delta in bytes
          *
          * @param delta value to add or substract to the internal pointer.
          */
        public function offsetBy(delta:int):void{}

		public function swap(ptr:*):void{}
    }
}
