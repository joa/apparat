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
 * User: Patrick Le Clec'h
 * Date: 8 mai. 2010
 * Time: 17:40:35
 */

package apparat.embedding.ant.tdsi

import apparat.embedding.ant.{OutParameter, ApparatTask}
import apparat.tools.tdsi.TurboDieselSportInjection

sealed class TDSITask extends ApparatTask(TurboDieselSportInjection, "tdsi") with OutParameter {
	def setAlchemy(value: String) = setArgument("a", value)

	def setInline(value: String) = setArgument("e", value)

	def setMacro(value: String) = setArgument("m", value)
}