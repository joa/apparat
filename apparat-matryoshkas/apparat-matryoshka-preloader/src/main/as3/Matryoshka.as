/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package {
	import apparat.lzma.LZMADecoder;

	import flash.display.Graphics;
	import flash.display.Loader;
	import flash.display.MovieClip;
	import flash.display.Stage;
	import flash.display.StageAlign;
	import flash.display.StageScaleMode;
	import flash.events.Event;
	import flash.system.ApplicationDomain;
	import flash.system.LoaderContext;
	import flash.utils.ByteArray;
	import flash.utils.getDefinitionByName;

	[SWF(backgroundColor='0xffffff', frameRate='16', width='128', height='32')]

	/**
	 * @author Joa Ebert
	 */
	public final class Matryoshka extends MovieClip {
		public function Matryoshka() {
			super()

			if(null == stage) {
				addEventListener(Event.ADDED_TO_STAGE, onAddedToStage)
			} else {
				init()
			}
		}

		private function init(): void {
			addEventListener(Event.ENTER_FRAME, onEnterFrame);
		}

		private function onAddedToStage(event: Event): void {
			removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage)
			init()
		}

		private function onEnterFrame(event: Event): void {
			var graphics: Graphics = this.graphics

			if(framesLoaded == totalFrames) {
				removeEventListener(Event.ENTER_FRAME, onEnterFrame)
				nextFrame()

				var matryoshka: Object = getDefinitionByName('MatryoshkaContent')

				try {
					var decoder: LZMADecoder = new LZMADecoder()
					var properties: Vector.<int> = new Vector.<int>(5, true)
					var input: ByteArray = matryoshka.byteArray;

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
					matryoshka.byteArray = null
					matryoshka.byteArrayAsset = null
				}
			} else {
				var stage: Stage = this.stage
				var preloaderWidth: Number = Math.min(100.0, stage.stageWidth * 0.33)
				var preloaderX: Number = (stage.stageWidth - preloaderWidth) * 0.5
				var preloaderY: Number = (stage.stageHeight - 2) * 0.5

				graphics.clear()
				graphics.beginFill(0x000000, 1.0)
				graphics.drawRect(preloaderX - 1.0, preloaderY - 1.0,
						preloaderWidth + 2.0, 4.0)
				graphics.endFill()
				graphics.beginFill(0xffffff, 1.0)
				graphics.drawRect(preloaderX, preloaderY,
						preloaderWidth * (root.loaderInfo.bytesLoaded / root.loaderInfo.bytesTotal),
						2)
				graphics.endFill()
			}
		}


		/**
		 * @inheritDoc
		 */
		override public function toString(): String {
			return '[Matryoshka]'
		}
	}
}
