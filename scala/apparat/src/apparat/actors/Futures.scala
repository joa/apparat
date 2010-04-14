package apparat.actors

import scala.actors.{Futures => SFutures}

object Futures {
	private val enabled = Actor.threadsEnabled

	def future[T](body: => T): () => T = {
		if(enabled) {
			SFutures.future(body)
		} else {
			val result: T = body
			() => { result }
		}
	}
}