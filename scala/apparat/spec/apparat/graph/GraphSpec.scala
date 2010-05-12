package apparat.graph


import org.specs.SpecificationWithJUnit
import apparat.utils.IndentingPrintWriter
import java.io.StringWriter

/**
 * Created by IntelliJ IDEA.
 * User: mzaks
 * Date: 26.04.2010
 * Time: 07:20:10
 * To change this template use File | Settings | File Templates.
 */

class GraphSpec extends SpecificationWithJUnit {
  "Immutable Graph creation" should {

    import immutable.Graph

    "be possible with list of edges" >> {
      val e1 = DefaultEdge("A", "B")
      val e2 = DefaultEdge("A", "C")
      val e3 = DefaultEdge("B", "C")
      val g = Graph(e1, e2, e3)

      g.outgoingOf("A") must (haveSize(2))
      g.incomingOf("C") must (haveSize(2))
    }

    "be possible with list of tuples if an implicit mapping provided" >> {
      implicit val mapping = DefaultEdge[String](_, _)
      val g = Graph("A" -> "B", "A" -> "C", "B" -> "C")

      g.outgoingOf("A") must (haveSize(2))
      g.incomingOf("C") must (haveSize(2))
    }

    "be possible with an adjacens mapping" >> {
      val e1 = DefaultEdge("A", "B")
      val e2 = DefaultEdge("A", "C")
      val e3 = DefaultEdge("B", "C")

      val g = new Graph(Map("A" -> List(e1, e2), "B" -> List(e3), "C" -> Nil))

      g.outgoingOf("A") must (haveSize(2))
      g.incomingOf("C") must (haveSize(2))
    }
  }

  "Immutable Graph have methods that" can {

    import immutable.Graph
    val g = Graph("A" -> "B", "A" -> "C", "B" -> "C")(DefaultEdge[String](_, _))

    "remove vertex" >> {
      val g1 = g - "C"

      g1 must_!= g
      g1.outgoingOf("A").size must_== 1
      g1.outgoingOf("B").size must_== 0
    }

    "throw exception when removing non existing vertex" >> {
      val g1 = g - "D" must throwAn[AssertionError]
    }

    "remove edge" >> {
      val e1 = DefaultEdge("A", "B")
      val g1 = g - e1

      g1 must_!= g
      g1.outgoingOf("A").size must_== 1
    }

    "throw exception when removing non existing edge" >> {

      "non existing direction" >> {
         val e4 = DefaultEdge("B", "A")
         g - e4 must throwAn[AssertionError]
      }

      "non existing starting vertex" >> {
        val e4 = DefaultEdge("U", "A")
         g - e4 must throwAn[AssertionError]
      }

      "non existing end vertex" >> {
        val e4 = DefaultEdge("B", "U")
         g - e4 must throwAn[AssertionError]
      }
    }

  }

  "Immutable Graph provide strategies that" can {

    import immutable.Graph

    "compute dominated vertex" >> {
      val g = Graph("A" -> "B", "A" -> "C", "B" -> "C")(DefaultEdge[String](_, _))
      g.dominance("B") must_== Some(List("C"))
    }

    "compute strongly connected components" >> {
      val g = Graph("A" -> "B", "A" -> "C", "B" -> "C", "C" -> "A", "C" -> "D", "D" -> "E", "E" -> "D")(DefaultEdge[String](_, _))
      g.sccs.toList.size must_== 2
      g.sccs.toList(0).vertices must_== List("A", "C", "B")
      g.sccs.toList(1).vertices must_== List("E", "D")
    }

    "compute top sort path" >> {
      val g = Graph("A" -> "B", "B" -> "C", "C" -> "B", "D" -> "E")(DefaultEdge[String](_, _))
      g.topsort.toList must_== List("E", "C", "B", "A", "D")
    }

    "compute depth first path" >> {
      val g = Graph("A" -> "B", "B" -> "C", "C" -> "B")(DefaultEdge[String](_, _))
      g.dft("A").toList must_== List("C", "B", "A")
      g.dft("B").toList must_== List("C", "B")
    }

    "produce dump" >> {
      val g = Graph("A" -> "B", "A" -> "C", "B" -> "C")(DefaultEdge[String](_, _))
      val s = new StringWriter();
      val is = new IndentingPrintWriter(s, false);
      g.dump(is);
      val expectation = "Graph:|A|->C|->B|B|->C|C|"
      val output: String = s.toString.replaceAll(" ", "").replaceAll("\\n", "|").replaceAll("\\r", "")
      output must_== expectation
    }
  }
}