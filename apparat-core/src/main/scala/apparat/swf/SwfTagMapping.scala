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
 */
package apparat.swf

import apparat.actors.Futures._

/**
 * @author Joa Ebert
 */
trait SwfTagMapping {
	def tags: List[SwfTag]
	def tags_=(value: List[SwfTag]): Unit

	def mapTags(f: PartialFunction[SwfTag, SwfTag]): Unit = {
		val mappers = for(tag <- tags) yield {
			if(f.isDefinedAt(tag)) { future { f(tag) } } else { () => tag }
		}
		tags = mappers map { _() }
	}
	
	def foreachTag(f: PartialFunction[SwfTag, Unit]): Unit = {
		val mappers = for(tag <- tags) yield {
			if(f.isDefinedAt(tag)) { Some(future { f(tag) }) } else { None }
		}
		mappers foreach { _ map { _() } }
	}
}