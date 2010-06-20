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
 */
package apparat.swf

import apparat.utils._

object SwfTags {
	val End = 0
	val ShowFrame = 1
	val DefineShape = 2
	val PlaceObject = 4
	val RemoveObject = 5
	val DefineBits = 6
	val DefineButton = 7
	val JPEGTables = 8
	val SetBackgroundColor = 9
	val DefineFont = 10
	val DefineText = 11
	val DoAction = 12
	val DefineFontInfo = 13
	val DefineSound = 14
	val StartSound = 15
	val DefineButtonSound = 17
	val SoundStreamHead = 18
	val SoundStreamBlock = 19
	val DefineBitsLossless = 20
	val DefineBitsJPEG2 = 21
	val DefineShape2 = 22
	val DefineButtonCxform = 23
	val Protect = 24
	val PlaceObject2 = 26
	val RemoveObject2 = 28
	val DefineShape3 = 32
	val DefineText2 = 33
	val DefineButton2 = 34
	val DefineBitsJPEG3 = 35
	val DefineBitsLossless2 = 36
	val DefineEditText = 37
	val DefineSprite = 39
	val ProductInfo = 41
	val FrameLabel = 43
	val SoundStreamHead2 = 45
	val DefineMorphShape = 46
	val DefineFont2 = 48
	val ExportAssets = 56
	val ImportAssets = 57
	val EnableDebugger = 58
	val DoInitAction = 59
	val DefineVideoStream = 60
	val VideoFrame = 61
	val DefineFontInfo2 = 62
	val DebugID = 63
	val EnableDebugger2 = 64
	val ScriptLimits = 65
	val SetTabIndex = 66
	val FileAttributes = 69
	val PlaceObject3 = 70
	val ImportAssets2 = 71
	val DoABC1 = 72
	val DefineFontAlignZones = 73
	val CSMTextSettings = 74
	val DefineFont3 = 75
	val SymbolClass = 76
	val Metadata = 77
	val DefineScalingGrid = 78
	val DoABC = 82
	val DefineShape4 = 83
	val DefineMorphShape2 = 84
	val DefineSceneAndFrameLabelData = 86
	val DefineBinaryData = 87
	val DefineFontName = 88
	val StartSound2 = 89
	val DefineBitsJPEG4 = 90
	val DefineFont4 = 91

	def toString(kind: Int): String = kind match {
		case End => "End"
		case ShowFrame => "ShowFrame"
		case DefineShape => "DefineShape"
		case PlaceObject => "PlaceObject"
		case RemoveObject => "RemoveObject"
		case DefineBits => "DefineBits"
		case DefineButton => "DefineButton"
		case JPEGTables => "JPEGTables"
		case SetBackgroundColor => "SetBackgroundColor"
		case DefineFont => "DefineFont"
		case DefineText => "DefineText"
		case DoAction => "DoAction"
		case DefineFontInfo => "DefineFontInfo"
		case DefineSound => "DefineSound"
		case StartSound => "StartSound"
		case DefineButtonSound => "DefineButtonSound"
		case SoundStreamHead => "SoundStreamHead"
		case SoundStreamBlock => "SoundStreamBlock"
		case DefineBitsLossless => "DefineBitsLossless"
		case DefineBitsJPEG2 => "DefineBitsJPEG2"
		case DefineShape2 => "DefineShape2"
		case DefineButtonCxform => "DefineButtonCxform"
		case Protect => "Protect"
		case PlaceObject2 => "PlaceObject2"
		case RemoveObject2 => "RemoveObject2"
		case DefineShape3 => "DefineShape3"
		case DefineText2 => "DefineText2"
		case DefineButton2 => "DefineButton2"
		case DefineBitsJPEG3 => "DefineBitsJPEG3"
		case DefineBitsLossless2 => "DefineBitsLossless2"
		case DefineEditText => "DefineEditText"
		case DefineSprite => "DefineSprite"
		case ProductInfo => "ProductInfo"
		case FrameLabel => "FrameLabel"
		case SoundStreamHead2 => "SoundStreamHead2"
		case DefineMorphShape => "DefineMorphShape"
		case DefineFont2 => "DefineFont2"
		case ExportAssets => "ExportAssets"
		case ImportAssets => "ImportAssets"
		case EnableDebugger => "EnableDebugger"
		case DoInitAction => "DoInitAction"
		case DefineVideoStream => "DefineVideoStream"
		case VideoFrame => "VideoFrame"
		case DefineFontInfo2 => "DefineFontInfo2"
		case DebugID => "DebugID"
		case EnableDebugger2 => "EnableDebugger2"
		case ScriptLimits => "ScriptLimits"
		case SetTabIndex => "SetTabIndex"
		case FileAttributes => "FileAttributes"
		case PlaceObject3 => "PlaceObject3"
		case ImportAssets2 => "ImportAssets2"
		case DoABC1 => "DoABC1"
		case DefineFontAlignZones => "DefineFontAlignZones"
		case CSMTextSettings => "CSMTextSettings"
		case DefineFont3 => "DefineFont3"
		case SymbolClass => "SymbolClass"
		case Metadata => "Metadata"
		case DefineScalingGrid => "DefineScalingGrid"
		case DoABC => "DoABC"
		case DefineShape4 => "DefineShape4"
		case DefineMorphShape2 => "DefineMorphShape2"
		case DefineSceneAndFrameLabelData => "DefineSceneAndFrameLabelData"
		case DefineBinaryData => "DefineBinaryData"
		case DefineFontName => "DefineFontName"
		case StartSound2 => "StartSound2"
		case DefineBitsJPEG4 => "DefineBitsJPEG4"
		case DefineFont4 => "DefineFont4"
		case _ => "(Unknown " + kind + ")"
	}

	val defaultTagFactory = (kind: Int) => kind match {
		case FileAttributes => Some(new FileAttributes)
		case Metadata => Some(new Metadata)
		case ScriptLimits => Some(new ScriptLimits)
		case SetBackgroundColor => Some(new SetBackgroundColor)
		case ProductInfo => Some(new ProductInfo)
		case FrameLabel => Some(new FrameLabel)
		case DoABC1 => Some(new DoABC)
		case DoABC => Some(new DoABC)
		case SymbolClass => Some(new SymbolClass)
		case ShowFrame => Some(new ShowFrame)
		case End => Some(new End)
		case DefineBitsJPEG2 => Some(new DefineBitsJPEG2)
		case DefineBitsJPEG3 => Some(new DefineBitsJPEG3)
		case DefineBitsJPEG4 => Some(new DefineBitsJPEG4)
		case DefineBitsLossless2 => Some(new DefineBitsLossless2)
		case _ => None
	}

	var tagFactory: (Int => Option[SwfTag]) = defaultTagFactory

	protected[swf] def create(kind: Int): SwfTag = tagFactory(kind) match {
		case Some(x) => x
		case None => new GenericTag(kind)
	}

	def isLongTag(kind: Int) = kind match {
		case
		DefineBits
				| DefineBitsJPEG2
				| DefineBitsJPEG3
				| DefineBitsJPEG4
				| DefineBitsLossless
				| DefineBitsLossless2 => true
		case _ => false
	}
}

trait DefineTag {
	var characterID: Int = 0

	def read(header: Recordheader)(implicit input: SwfInputStream): Unit = {
		characterID = input.readUI16()
	}

	def write(implicit output: SwfOutputStream): Unit = {
		output writeUI16 characterID
	}
}

trait NoDataTag extends KnownLengthTag {
	override def length = 0

	def read(header: Recordheader)(implicit input: SwfInputStream): Unit = {}

	def write(implicit output: SwfOutputStream): Unit = {}
}

trait KnownLengthTag {
	def length: Int
}

abstract class SwfTag(val kind: Int) {
	def read(header: Recordheader)(implicit input: SwfInputStream): Unit

	def write(implicit output: SwfOutputStream): Unit

	def foreach(body: this.type => Unit) = body(this)

	def map[T](f: this.type => T) = f(this)
}

class GenericTag(override val kind: Int) extends SwfTag(kind) with KnownLengthTag {
	private var data: Option[Array[Byte]] = None
	private var header: Option[Recordheader] = None

	override def length = data match {
		case Some(x) => x.length
		case None => 0
	}

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		this.header = Some(header)
		data = Some(IO read header.length)
	}

	override def write(implicit output: SwfOutputStream) = data match {
		case Some(x) => output write x
		case None =>
	}

	override def toString() = "[" + (SwfTags toString kind) + "]"
}

////////////////////////////////////////////////////////////////////////////////
// Control Tags
////////////////////////////////////////////////////////////////////////////////

class End extends SwfTag(SwfTags.End) with NoDataTag {
	override def toString = "[End]"
}

class ShowFrame extends SwfTag(SwfTags.ShowFrame) with NoDataTag {
	override def toString = "[ShowFrame]"
}

class FileAttributes extends SwfTag(SwfTags.FileAttributes) with KnownLengthTag {
	var actionScript3 = true
	var hasMetadata = false
	var useDirectBlit = false
	var useGPU = false
	var useNetwork = false

	override def length = 4

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		if (0 != input.readUB(1)) error("Reserved bit must be zero.")

		useDirectBlit = input.readUB(1) == 1
		useGPU = input.readUB(1) == 1
		hasMetadata = input.readUB(1) == 1
		actionScript3 = input.readUB(1) == 1

		if (0 != input.readUB(2)) error("Reserved bits must be zero.")

		useNetwork = input.readUB(1) == 1

		if (0 != input.readUB(24)) error("Reserved bits must be zero.")
	}

	override def write(implicit output: SwfOutputStream) = {
		implicit def bool2int(value: Boolean): Int = if (value) 1 else 0

		output writeUB (0, 1)
		output writeUB (useDirectBlit, 1)
		output writeUB (useGPU, 1)
		output writeUB (hasMetadata, 1)
		output writeUB (actionScript3, 1)
		output writeUB (0, 2)
		output writeUB (useNetwork, 1)
		output writeUB (0, 24)
	}

	override def toString = "[FileAttributesTag useDirectBlit: " + useDirectBlit +
			", useGPU: " + useGPU + ", hasMetadata: " + hasMetadata +
			", actionScript3: " + actionScript3 + ", useNetwork: " +
			useNetwork + "]"
}

class Metadata extends SwfTag(SwfTags.Metadata) {
	var metadata = ""

	override def read(header: Recordheader)(implicit input: SwfInputStream) = metadata = input readSTRING

	override def write(implicit output: SwfOutputStream) = output writeSTRING metadata

	override def toString = "[Metadata \"" + metadata + "\"]"
}

class ScriptLimits extends SwfTag(SwfTags.ScriptLimits) with KnownLengthTag {
	var maxRecursionDepth = 1000
	var scriptTimeoutSeconds = 60

	override def length = 4

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		maxRecursionDepth = input readUI16 ()
		scriptTimeoutSeconds = input readUI16 ()
	}

	override def write(implicit output: SwfOutputStream) = {
		output writeUI16 maxRecursionDepth
		output writeUI16 scriptTimeoutSeconds
	}

	override def toString = "[ScriptLimitsTag maxRecursionDepth: " +
			maxRecursionDepth + ", scriptTimeoutSeconds: " + scriptTimeoutSeconds + "]"
}

class SetBackgroundColor extends SwfTag(SwfTags.SetBackgroundColor) with KnownLengthTag {
	var color = new RGB(0, 0, 0)

	override def length = 3

	override def read(header: Recordheader)(implicit input: SwfInputStream) = color = input readRGB

	override def write(implicit output: SwfOutputStream) = output writeRGB color

	override def toString = "[SetBackgroundColor color: " + color + "]"
}

class ProductInfo extends SwfTag(SwfTags.ProductInfo) with KnownLengthTag {
	var product = 0L
	var edition = 0L
	var versionMajor = 0
	var versionMinor = 0
	var build = 0
	var compileDate = new java.util.Date()

	override def length = 26

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		product = input.readUI32()
		edition = input.readUI32()
		versionMajor = input.readUI08()
		versionMinor = input.readUI08()
		build = input.readUI64().intValue()
		compileDate = new java.util.Date(input.readUI64().longValue())
	}

	override def write(implicit output: SwfOutputStream) = {
		output writeUI32 product
		output writeUI32 edition
		output writeUI08 versionMajor
		output writeUI08 versionMinor
		output writeUI64 build
		output writeUI64 compileDate.getTime()
	}

	override def toString = "[ProductInfo product: " + product + ", edition: " +
			edition + ", version " + versionMajor + "." + versionMinor +
			", build: " + build + ", compileDate: " + compileDate + "]"
}

class FrameLabel extends SwfTag(SwfTags.FrameLabel) {
	var name = ""

	override def read(header: Recordheader)(implicit input: SwfInputStream) = name = input readSTRING

	override def write(implicit output: SwfOutputStream) = output writeSTRING name

	override def toString = "[FrameLabel \"" + name + "\"]"
}

class SymbolClass extends SwfTag(SwfTags.SymbolClass) {
	var symbols = new Array[(Int, String)](0)

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		val n = input readUI16 ()

		symbols = new Array[(Int, String)](n)

		for (i <- 0 until n)
			symbols(i) = input.readUI16() -> input.readSTRING()
	}

	override def write(implicit output: SwfOutputStream) = {
		output writeUI16 (symbols length)
		for (x <- symbols) {
			output writeUI16 x._1
			output writeSTRING x._2
		}
	}

	override def toString = {
		val builder = new StringBuilder(0x20 * symbols.length)
		for (x <- symbols)
			builder append (" " + x)

		"[SymbolClass" + builder.toString() + "]"
	}
}

class DoABC extends SwfTag(SwfTags.DoABC) {
	var flags = 0L
	var name = ""
	var abcData = new Array[Byte](0)

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		if (header.kind == SwfTags.DoABC1) {
			abcData = IO read (header.length)
		} else {
			flags = input readUI32 ()
			name = input readSTRING ()
			abcData = IO read (header.length - name.length - 5)
		}
	}

	override def write(implicit output: SwfOutputStream) = {
		output writeUI32 flags
		output writeSTRING name
		output write abcData
	}

	override def toString = {
		"[DoABC flags: " + flags + ", name: \"" + name + "\"]"
	}
}

////////////////////////////////////////////////////////////////////////////////
// Define Tags
////////////////////////////////////////////////////////////////////////////////

class DefineBitsJPEG2 extends SwfTag(SwfTags.DefineBitsJPEG2) with KnownLengthTag with DefineTag {
	var imageData = new Array[Byte](0)

	override def length = 2 + imageData.length

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		super.read(header)
		imageData = IO read (header.length - 2)
	}

	override def write(implicit output: SwfOutputStream) = {
		super.write(output)
		output write imageData
	}

	override def toString = "[DefineBitsJPEG2]"
}

class DefineBitsJPEG3 extends SwfTag(SwfTags.DefineBitsJPEG3) with KnownLengthTag with DefineTag {
	var imageData = new Array[Byte](0)
	var alphaData = new Array[Byte](0)

	override def length = 6 + imageData.length + alphaData.length

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		super.read(header)
		val imageLength = input.readUI32().asInstanceOf[Int]
		val alphaLength = header.length - imageLength - 6
		imageData = IO read imageLength
		alphaData = IO read alphaLength
	}

	override def write(implicit output: SwfOutputStream) = {
		super.write(output)
		output writeUI32 imageData.length
		output write imageData
		output write alphaData
	}

	override def toString = "[DefineBitsJPEG3]"
}

class DefineBitsJPEG4 extends SwfTag(SwfTags.DefineBitsJPEG4) with KnownLengthTag with DefineTag {
	var imageData = new Array[Byte](0)
	var alphaData = new Array[Byte](0)
	var deblock = 0.0f

	override def length = 8 + imageData.length + alphaData.length

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		super.read(header)
		val imageLength = input.readUI32().asInstanceOf[Int]
		val alphaLength = header.length - imageLength - 8
		deblock = input.readFIXED8()
		imageData = IO read imageLength
		alphaData = IO read alphaLength
	}

	override def write(implicit output: SwfOutputStream) = {
		super.write(output)
		output writeUI32 imageData.length
		output writeFIXED8 deblock
		output write imageData
		output write alphaData
	}

	override def toString = "[DefineBitsJPEG4]"
}

class DefineBitsLossless2 extends SwfTag(SwfTags.DefineBitsLossless2) with KnownLengthTag with DefineTag {
	var bitmapFormat = 0
	var bitmapWidth = 0
	var bitmapHeight = 0
	var bitmapColorTableSize = 0
	var zlibBitmapData = new Array[Byte](0)

	override def length = 7 + zlibBitmapData.length + (if (3 == bitmapFormat) 1 else 0)

	override def read(header: Recordheader)(implicit input: SwfInputStream) = {
		super.read(header)

		bitmapFormat = input.readUI08()
		bitmapWidth = input.readUI16()
		bitmapHeight = input.readUI16()

		zlibBitmapData = IO read (bitmapFormat match {
			case 3 => {bitmapColorTableSize = input.readUI08(); header.length - 8}
			case _ => header.length - 7
		})
	}

	override def write(implicit output: SwfOutputStream) = {
		super.write(output)
		output writeUI08 bitmapFormat
		output writeUI16 bitmapWidth
		output writeUI16 bitmapHeight
		if (3 == bitmapFormat) output writeUI08 bitmapColorTableSize
		output write zlibBitmapData
	}

	override def toString = "[DefineBitsLossless2]"
}
