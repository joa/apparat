package {
	import apparat.lzma.LZMADecoder

	import flash.display.Loader
	import flash.display.Sprite
	import flash.utils.ByteArray

	/**
	 * @author Joa Ebert
	 */
	public class Main extends Sprite {
		public function Main() {
			var input: ByteArray = null;
			var decoder: LZMADecoder = new LZMADecoder()
			var properties: Vector.<int> = new Vector.<int>(5, true)

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
					loader.loadBytes(output)
					addChild(loader)
				} else {
					throw new Error('LZMA decoder failed.')
				}
			} else {
				throw new Error('Could not set decoder properties.');
			}
		}


		/**
		 * @inheritDoc
		 */
		override public function toString():String {
			return '[Main]';
		}
	}
}