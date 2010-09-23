package jitb.net {
	import flash.errors.IllegalOperationError
	import flash.events.EventDispatcher
	import flash.utils.ByteArray

	public final class ContinuousFileLoader extends EventDispatcher {
		public function ContinuousFileLoader(file: String) { throw new IllegalOperationError() }
		public function get data(): ByteArray { throw new IllegalOperationError() }
		public function start(): void { throw new IllegalOperationError() }
		public function stop(): void { throw new IllegalOperationError() }
	}
}