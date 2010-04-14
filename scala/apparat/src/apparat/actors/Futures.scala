package apparat.actors

import scala.actors.{Future => SFuture, Futures => SFutures}

object Futures {
	private val enabled = Actor.threadsEnabled

	def future[T](body: => T): SFuture[T] = {
		if(enabled) {
			SFutures.future(body)
		} else {
			val result: T = body
			SFutures.future { result }
		}
	}
}