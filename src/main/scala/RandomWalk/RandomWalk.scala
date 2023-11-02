package RandomWalk

import org.apache.spark.graphx._

import scala.util.Random
import helpers.ComparableNode
import MitMSimulator.MitMSimulator.attackingOriginalGraph
import org.apache.log4j.Logger
import org.apache.spark.broadcast.Broadcast

object RandomWalk {
  val logger: Logger = Logger.getLogger("CS441HW2MitM")

  def vertexProgram(originalGraph: Broadcast[Array[ComparableNode]]): (VertexId, (Long, ComparableNode, Long, Long, Long, Long, Long), (Long, ComparableNode, Long, Long, Long, Long, Long)) => (Long, ComparableNode, Long, Long, Long, Long, Long) = {
    (id, oldValue, newValue) => {
      // attr always stays the same
      // number of sucessful attacks and failed attacks are updated
      // based on similarity check and attack decision

      // _1 is neighbor id
      // _2 is current attr
      // _3 is number of successful attacks : by design should be 1 or more given we identify it every time
      // _4 is number of failed attacks: should be 0 but if more attack with low probability
      // _5 is misidentified nodes but no valuable data
      // _6 is uneventful attacks
      // _7 is number of supersteps

      if (newValue._1 != Long.MaxValue) {
        val (newSuccessful, newFailed, newMissidentified , newUneventful) = attackingOriginalGraph(newValue._2, originalGraph.value, oldValue._3, oldValue._4, oldValue._5, oldValue._6)
        (newValue._1, oldValue._2, newSuccessful, newFailed, newMissidentified, newUneventful, newValue._7)

      } else oldValue
    }
  }

  def sendMessage(triplet: EdgeTriplet[(Long, ComparableNode, Long, Long, Long, Long, Long), _], neighborsMap: Broadcast[Map[VertexId, Array[ComparableNode]]]): Iterator[(VertexId, (Long, ComparableNode, Long, Long, Long, Long, Long))] = {
    // only send message to one of the neighbors of the node that received the message
    if (triplet.srcAttr._1 != Long.MaxValue && triplet.srcAttr._1 == triplet.dstId) {
      // pick a neighbour at random for the destination node, this will be processed in its vertex program
      val neighbours = neighborsMap.value.getOrElse(triplet.dstId, Array.empty[ComparableNode])
      if (neighbours.nonEmpty) {
        val randomNeighbour = neighbours(Random.nextInt(neighbours.length)).id
        logger.info(s"Message Passed,${triplet.srcId},${triplet.dstId},${triplet.srcAttr._7}")
        Iterator((triplet.dstId, (randomNeighbour, triplet.dstAttr._2, triplet.dstAttr._3, triplet.dstAttr._4, triplet.dstAttr._5, triplet.dstAttr._6, triplet.srcAttr._7 + 1)))
      } else {
        Iterator.empty
      }
    } else {
      Iterator.empty
    }
  }

  def mergeMessage(a: (Long, ComparableNode, Long, Long, Long, Long, Long), b: (Long, ComparableNode, Long, Long, Long, Long, Long)): (Long, ComparableNode, Long, Long, Long, Long, Long) = {
    val values = Seq(a, b)
    // when we send multiple messages to the same node at a super step, just pick one of them
    values(Random.nextInt(values.size))
  }
}

//// Usage in Pregel:
//val pregelGraph = initialGraph.pregel(
//  (Long.MaxValue, initialMessage, 0L, 0L),
//  20,
//  EdgeDirection.Out
//)(
//  RandomWalk.vertexProgram,
//  triplet => RandomWalk.sendMessage(triplet, neighborsMap),  // Assuming neighborsMap is already defined.
//  RandomWalk.mergeMessage
//)
