package {
	import flash.display.Sprite;
	import flash.events.Event;

	/**
	 * @author Joa Ebert
	 */
	public final class Matryoshka extends Sprite {
		public function Matryoshka() {
			super()

			if(null == stage) {
				addEventListener(Event.ADDED_TO_STAGE, onAddedToStage)
			} else {
				init()
			}
		}

		private function onAddedToStage(event: Event): void {
			removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage)
			init()
		}

		private function init(): void {

		}


		/**
		 * @inheritDoc
		 */
		override public function toString(): String {
			return '[Matryoshka]'
		}
	}
}