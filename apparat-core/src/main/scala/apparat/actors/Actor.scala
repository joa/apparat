package apparat.actors

object Actor {
	val threadsEnabled = System.getProperty("apparat.threads", "true").toLowerCase == "true"
}