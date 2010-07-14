package {
	import apparat.lzma.LZMADecoder;

	import flash.display.Loader;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.system.ApplicationDomain;
	import flash.system.LoaderContext;
	import flash.utils.ByteArray;

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
			try {
				var decoder: LZMADecoder = new LZMADecoder()
				var properties: Vector.<int> = new Vector.<int>(5, true)
				var input: ByteArray = MatryoshkaContent.byteArray;

				for(var i: int = 0; i < 5; ++i) {
					properties[i] = input.readUnsignedByte()
				}

				if(decoder.setDecoderProperties(properties)) {
					var outSize: uint = 0

					for(var j: int = 0; j < 8; ++j) {
						outSize |= input.readUnsignedByte() << (8 * j)
					}

					var output: ByteArray = new ByteArray()

					if(decoder.code(input, output, outSize)) {
						var loader: Loader = new Loader()
						loader.loadBytes(output, new LoaderContext(false, ApplicationDomain.currentDomain))
						addChild(loader)
					} else {
						throw new Error('LZMA decoder failed.')
					}
				} else {
					throw new Error('Could not set decoder properties.');
				}
			} finally {
				MatryoshkaContent.byteArray = null
				MatryoshkaContent.byteArrayAsset = null
			}
		}


		/**
		 * @inheritDoc
		 */
		override public function toString():String {
			return '[Matryoshka]'
		}
	}
}