package NodeParser

import helpers.NodeDataParser
import org.scalatest.{FlatSpec, Matchers}
import helpers.ComparableNode
import scala.io.Source

class NodeParserTest extends FlatSpec with Matchers {

  implicit val repeatable: Boolean = true
  behavior of "NodeParser for Nodes"

  it should "be able to parse string rep and make a comparable node class object" in {
    val stringToParse = "(0, 0, 2, List(-1349935424, 1077067315, 110373521, 2109098941), List(), true)"
    val parsedNode = NodeDataParser.parseNodeData(stringToParse)
    val expectedNode = new ComparableNode(0,0,2,List(-1349935424, 1077067315, 110373521, 2109098941),List(), true)
    parsedNode.toString shouldBe expectedNode.toString
  }

  it should "be able to parse in format string rep|string rep which is the output of mapper job" in {

    val resourcePath= "firstjob.txt"
    try {
      val source = Source.fromResource(resourcePath)

      // Get all lines from the file as a List
      val lines = source.getLines.toList

      // Close the source to release resources
      source.close()

      for ((line, lineNumber) <- lines.zipWithIndex) {
        try {
          val node1 = NodeDataParser.parseNodeData(line.trim)
        } catch {
          case e: Exception =>
            // Handle exceptions for individual lines here
            fail(s"Error processing line $lineNumber: ${e.getMessage}")
        }
      }

    } catch {
      case e: Exception =>
        // Handle exceptions related to file access here
        fail(s"Error reading the file: ${e.getMessage}")
    }
  }
}
