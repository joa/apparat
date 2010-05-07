package apparat.tools.reducer

import apparat.tools._
import apparat.utils._
import apparat.swf._

import java.awt.image.{BufferedImage => JBufferedImage}
import javax.imageio.{IIOImage => JIIOImage}
import javax.imageio.{ImageIO => JImageIO}
import javax.imageio.{ImageWriteParam => JImageWriteParam}
import java.util.zip.{Inflater => JInflater}
import java.util.zip.{Deflater => JDeflater}
import apparat.actors.Futures._
import java.io.{File => JFile, ByteArrayOutputStream => JByteArrayOutputStream, ByteArrayInputStream => JByteArrayInputStream}

object Reducer {
	def main(args: Array[String]): Unit = ApparatApplication(new ReducerTool, args)

	class ReducerTool extends ApparatTool {
		var deblock = 0.0f
		var quality = 0.99f
		var input = ""
		var output = ""

		override def name: String = "Reducer"

		override def help: String = """  -i [file]	Input file
  -o [file]	Output file (optional)
  -d [float]	Strength of deblocking filter (optional)
  -q [float]	Quality from 0.0 to 1.0 (optional)"""

		override def configure(config: ApparatConfiguration) = {
			deblock = java.lang.Float parseFloat config("-d").getOrElse("0.0")
			quality = java.lang.Float parseFloat config("-q").getOrElse("0.99")
			input = config("-i") getOrElse error("Input is required.")
			output = config("-o") getOrElse input
			assert(new JFile(input) exists, "Input has to exist.")
		}

		override def run() = {
			SwfTags.tagFactory = (kind: Int) => kind match {
			/*case SwfTags.DefineBitsJPEG2 => Some(new DefineBitsJPEG2)
							case SwfTags.DefineBitsJPEG3 => Some(new DefineBitsJPEG3)
							case SwfTags.DefineBitsJPEG4 => Some(new DefineBitsJPEG4)*/
				case SwfTags.DefineBitsLossless2 => Some(new DefineBitsLossless2)
				case _ => None
			}
			val source = new JFile(input)
			val target = new JFile(output)
			val l0 = source length
			val cont = TagContainer fromFile source
			cont.tags = cont.tags filterNot (tag => tag.kind == SwfTags.Metadata || tag.kind == SwfTags.ProductInfo) map reduce
			cont write target
			val delta = l0 - (target length)
			ApparatLog("Compression ratio: " + ((delta).asInstanceOf[Float] / l0.asInstanceOf[Float]) * 100.0f + "%")
			ApparatLog("Total bytes: " + delta)
		}

		private def reduce(tag: SwfTag) = tag.kind match {
			case SwfTags.DefineBitsLossless2 => {
				val f = future {
					val dbl2 = tag.asInstanceOf[DefineBitsLossless2]
					if (5 == dbl2.bitmapFormat && (dbl2.bitmapWidth * dbl2.bitmapHeight) > 1024) {
						lossless2jpg(dbl2)
					} else {
						dbl2
					}
				}
				f()
			}
			case _ => tag
		}

		private def lossless2jpg(tag: DefineBitsLossless2) = {
			val width = tag.bitmapWidth
			val height = tag.bitmapHeight
			val inflater = new JInflater();
			val lossless = new Array[Byte]((width * height) << 2)
			val alphaData = new Array[Byte](width * height)
			var needsAlpha = false

			// decompress zlib data

			inflater setInput tag.zlibBitmapData

			var offset = -1
			while (0 != offset && !inflater.finished) {
				offset = inflater inflate lossless
				if (0 == offset && inflater.needsInput) {
					error("Need more input.")
				}
			}

			// create buffered image
			// fill alpha data

			val buffer = new JBufferedImage(width, height, JBufferedImage.TYPE_INT_ARGB)

			for (y <- 0 until height; x <- 0 until width) {
				val index = (x << 2) + (y << 2) * width
				val alpha = lossless(index) & 0xff
				val red = lossless(index + 1) & 0xff
				val green = lossless(index + 2) & 0xff
				val blue = lossless(index + 3) & 0xff

				if (0xff != alpha) {
					needsAlpha = true
				}

				// useless to go from premultiplied to normal
				//
				//if(alpha > 0 && alpha < 0xff) {
				//  val alphaMultiplier = 255.0f / alpha
				//  red = clamp(red * alphaMultiplier)
				//  green = clamp(green * alphaMultiplier)
				//  blue = clamp(blue * alphaMultiplier)
				//}

				alphaData(x + y * width) = lossless(index)
				buffer.setRGB(x, y, (0xff << 0x18) | (red << 0x10) | (green << 0x08) | blue)
			}

			// compress alpha data

			val deflater = new JDeflater(JDeflater.BEST_COMPRESSION)
			deflater setInput alphaData
			deflater.finish()

			val compressBuffer = new Array[Byte](0x400)
			var numBytesCompressed = 0
			val alphaOutput = new JByteArrayOutputStream()

			do {
				numBytesCompressed = deflater deflate compressBuffer
				alphaOutput write (compressBuffer, 0, numBytesCompressed)
			} while (0 != numBytesCompressed)

			alphaOutput.flush()
			alphaOutput.close()

			// create jpg

			val writer = JImageIO getImageWritersByFormatName ("jpg") next ()
			val imageOutput = new JByteArrayOutputStream()

			writer setOutput JImageIO.createImageOutputStream(imageOutput)

			val writeParam = writer.getDefaultWriteParam()
			writeParam setCompressionMode JImageWriteParam.MODE_EXPLICIT
			writeParam setCompressionQuality quality
			writer write (null, new JIIOImage(buffer.getData(), null, null), writeParam)
			imageOutput.flush()
			imageOutput.close()
			writer.dispose()

			// create tag

			val newTag: SwfTag with KnownLengthTag with DefineTag = if (needsAlpha) {
				if (0.0f == deblock) {
					val dbj3 = new DefineBitsJPEG3()
					dbj3.alphaData = alphaOutput.toByteArray()
					dbj3.imageData = imageOutput.toByteArray()
					dbj3
				} else {
					val dbj4 = new DefineBitsJPEG4()
					dbj4.alphaData = alphaOutput.toByteArray()
					dbj4.imageData = imageOutput.toByteArray()
					dbj4.deblock = deblock
					dbj4
				}
			} else {
				val dbj2 = new DefineBitsJPEG2()
				dbj2.imageData = imageOutput.toByteArray()
				dbj2
			}

			if (newTag.length < tag.length) {
				ApparatLog succ ("Compressed character " + tag.characterID)
				newTag.characterID = tag.characterID
				newTag
			} else {
				tag
			}
		}

		private def clamp(value: Float): Int = value match {
			case x if x < 0 => 0
			case x if x > 255 => 255
			case x => x.asInstanceOf[Int]
		}
	}
}
