package apparat.swc

import org.specs.SpecificationWithJUnit

/**
 * Created by IntelliJ IDEA.
 * User: mzaks
 * Date: 26.04.2010
 * Time: 09:11:12
 * To change this template use File | Settings | File Templates.
 */

class SwcSpec extends SpecificationWithJUnit {
  val swc = Swc.fromFile("spec/asset/ApparatTest.swc")

  "SWC is a zip file that" should {
    "contain catalog.xml" >> {
      swc.catalog.get.isEmpty must_== false
    }

    "contain a library" >> {
      swc.library.get.isEmpty must_== false
    }
  }

}