package RandomWalk

import org.apache.spark.graphx._
import scala.util.Random
import helpers.ComparableNode
import MitMSimulator.MitMSimulator.attackingOriginalGraph

object RandomWalk {

  def vertexProgram(originalGraph: Array[ComparableNode]): (VertexId, (Long, ComparableNode, Long, Long, Long, Long), (Long, ComparableNode, Long, Long, Long, Long)) => (Long, ComparableNode, Long, Long, Long, Long) = {
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

      if (newValue._1 != Long.MaxValue) {
        val (newSuccessful, newFailed, newMissidentified , newUneventful) = attackingOriginalGraph(newValue._2, originalGraph, oldValue._3, oldValue._4, oldValue._5, oldValue._6)
        (newValue._1, oldValue._2, newSuccessful, newFailed, newMissidentified, newUneventful)
      } else oldValue
    }
  }

  def sendMessage(triplet: EdgeTriplet[(Long, ComparableNode, Long, Long, Long, Long), _], neighborsMap: Map[VertexId, Array[ComparableNode]]): Iterator[(VertexId, (Long, ComparableNode, Long, Long, Long, Long))] = {
    if (triplet.srcAttr._1 != Long.MaxValue && triplet.srcAttr._1 == triplet.dstId) {
      val neighbours = neighborsMap.getOrElse(triplet.dstId, Array.empty[ComparableNode])
      if (neighbours.nonEmpty) {
        val randomNeighbour = neighbours(Random.nextInt(neighbours.length)).id
        Iterator((triplet.dstId, (randomNeighbour, triplet.dstAttr._2, triplet.dstAttr._3, triplet.dstAttr._4, triplet.dstAttr._5, triplet.dstAttr._6)))
      } else {
        Iterator.empty
      }
    } else {
      Iterator.empty
    }
  }

  def mergeMessage(a: (Long, ComparableNode, Long, Long, Long, Long), b: (Long, ComparableNode, Long, Long, Long, Long)): (Long, ComparableNode, Long, Long, Long, Long) = {
    val values = Seq(a, b)
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