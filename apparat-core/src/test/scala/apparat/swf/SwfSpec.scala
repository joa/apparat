package apparat.swf

import org.specs.SpecificationWithJUnit
import java.util.Date


/**
 * Created by IntelliJ IDEA.
 * User: mzaks
 * Date: 28.04.2010
 * Time: 22:05:05
 * To change this template use File | Settings | File Templates.
 */

class SwfSpec extends SpecificationWithJUnit {
  "SWF file" should {

    var swf = Swf.fromFile("target/test-classes/myFunction.swf")

    "contain Flash player version" >> {
      "9" >> {
        swf.version must_== 9
      }
      "10" >> {
        swf = Swf.fromFile("target/test-classes/Fp10App.swf")
        swf.version must_== 10
      }
    }

    "have framerate of 31" >> {
      swf.frameRate must_== 31
    }

    "be compressed" >> {
      swf.compressed must_== true
    }

    "have framecount" >> {
      swf.frameCount must_== 1
    }

    "have frameSize" >> {
      def framesize = swf.frameSize;
      framesize must_!= null

      "framesize has minX" >> {
        swf.frameSize.minX must_== 0
      }

      "framesize has maxX" >> {
        swf.frameSize.maxX must_== 11000
      }

      "framesize has minY" >> {
        swf.frameSize.minY must_== 0
      }

      "framesize has maxY" >> {
        swf.frameSize.maxY must_== 8000
      }

    }

    "have Metadata tag" >> {
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.Metadata)
      idx must_!= -1
      val tag = swf.tags(idx).asInstanceOf[Metadata]

      "Metadata have metadata attribute" >> {
        tag.metadata.isEmpty must_== false
      }
    }

    "have ScriptLimits tag" >> {
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.ScriptLimits)
      idx must_!= -1
      val tag = swf.tags(idx).asInstanceOf[ScriptLimits]

      "ScriptLimits have maxRecursionDepth" >> {
        tag.maxRecursionDepth must_== 1000
      }

      "ScriptLimits have scriptTimeoutSeconds" >> {
        tag.scriptTimeoutSeconds must_== 60
      }

    }

    "have SetBackgroundColor tag" >> {
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.SetBackgroundColor)
      idx must_!= -1
      val tag = swf.tags(idx).asInstanceOf[SetBackgroundColor]

      "SetBackgroundColor have color" >> {
        tag.color must_== new RGB(255, 255, 255)
      }

    }

    "have ProductInfo tag" >> {
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.ProductInfo)
      idx must_!= -1
      val tag = swf.tags(idx).asInstanceOf[ProductInfo]

      "ProductInfo have build" >> {
        tag.build must_== 4852
      }

      "ProductInfo have compileDate" >> {
        tag.compileDate.before(new Date()) must_== true
      }
      "ProductInfo have edition" >> {
        tag.edition must_== 6
      }
      "ProductInfo have product" >> {
        tag.product must_== 3
      }
      "ProductInfo have versionMajor" >> {
        tag.versionMajor must_== 3
      }
      "ProductInfo have versionMinor" >> {
        tag.versionMinor must_== 3
      }
    }

    "have FrameLabel tag" >> {
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.FrameLabel)
      idx must_!= -1
      val tag = swf.tags(idx).asInstanceOf[FrameLabel]

      "FrameLabel have name" >> {
        tag.name must_== "myFunction"
      }
    }

    "have DoABC tag" >> {
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.DoABC)
      idx must_!= -1
      val tag = swf.tags(idx).asInstanceOf[DoABC]

      "DoABC have flags" >> {
        tag.flags must_== 1
      }
      "DoABC have name" >> {
        tag.name must_== "frame1"
      }
    }

    "have SymbolClass tag" >> {
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.SymbolClass)
      idx must_!= -1
      val tag = swf.tags(idx).asInstanceOf[SymbolClass]

      "SymbolClass have symbols" >> {
        def symbols = tag.symbols
        symbols must_!= null

        "size of simbols is one" >> {
          symbols.size must_== 1

          "first simbol is myFunction" >> {
            symbols(0)._2 must_== "myFunction"
          }
        }
      }
    }

    "have ShowFrame tag" >> {
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.ShowFrame)
      idx must_!= -1
    }

    "have END tag as last tag" >> {
      swf.tags.last.kind must_== SwfTags.End
    }

    "have no DebugID tag" >> {
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.DebugID)
      idx must_== -1
    }

    "be compiled in debug mode in order to have DebugID tag" >> {
      swf = Swf.fromFile("target/test-classes/myFunctionInDebug.swf")
      var idx = swf.tags.findIndexOf(_.kind == SwfTags.DebugID)
      idx must_!= -1
    }
  }
}