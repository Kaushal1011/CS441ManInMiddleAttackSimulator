package NodeSimRank
import org.scalatest.{FlatSpec, Matchers}
import helpers.ComparableNode
import scala.io.Source

class NodeSimRankTest extends FlatSpec with Matchers {

  implicit val repeatable: Boolean = true
  behavior of "NodeSimRank"

  it should "return the correct simrank for a given node" in {

    val node1 = new ComparableNode(0,0,2,List(-1349935424, 1077067315, 110373521, 2109098941),List(), false)
    val node2 = new ComparableNode(0,1,2,List(-1349935424, 1077067315, 110373521, 2109098941),List(), false)

    val simRank = node1.SimRankFromJaccardSimilarity(node2)

    println(s"computer sim rank is : $simRank")

    simRank shouldBe 1.0
  }

  it should "return the correct simrank for a given node less than 1" in {

    val node1 = new ComparableNode(0, 0, 2, List(-1349935424, 1077067315, 110353521, 2109098941), List(), false)
    val node2 = new ComparableNode(0, 1, 2, List(-1349935424, 1077067315, 110373521, 2109098941), List(),false)

    val simRank = node1.SimRankFromJaccardSimilarity(node2)

    simRank shouldBe < (1.0)
  }

}
