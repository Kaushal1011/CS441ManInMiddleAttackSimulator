import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.util.LongAccumulator
import org.apache.spark.graphx._
import helpers.{ComparableEdge, ComparableNode, NodeDataParser}
import scala.util.Random
import org.apache.log4j.Logger
import RandomWalk.RandomWalk.{vertexProgram, sendMessage, mergeMessage}
import com.typesafe.config.ConfigFactory
import Utilz.ConfigReader
object Main {
  val logger: Logger = Logger.getLogger("CS441HW2MitM")

  //  println(Main.getClass.getName)

  logger.info("This log will go to app_logs file")

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("GraphSim")
      .master("local[4]") // Set master to local with 4 cores. Adjust as needed.
      .getOrCreate()

    val sc = spark.sparkContext

    val config = ConfigFactory.load()
    val mitmSimConfig = ConfigReader.getMitMSimConfig(config)

    val nodeFileOG = "./input/nodes.txt"

    val nodeFilePath = "./input/nodesPerturbed.txt"

    // Read the file and parse each line to create nodes
    val nodesRDD: RDD[(VertexId, ComparableNode)] = sc.textFile(nodeFilePath)
      .map(line => (NodeDataParser.parseNodeData(line).id, NodeDataParser.parseNodeData(line)))

    val edgeFilePath = "./input/edgesPerturbed.txt"

    val edgeRDD: RDD[Edge[ComparableEdge]] = sc.textFile(edgeFilePath)
      .map(line => Edge(NodeDataParser.parseEdgeData(line).srcId, NodeDataParser.parseEdgeData(line).dstId, NodeDataParser.parseEdgeData(line)))


    // Calculate the total number of nodes
    val totalNodes = nodesRDD.count()

    // Calculate 10% of the total nodes
    val initialNodeCount = (totalNodes * mitmSimConfig.simConfig.initNodesPercentage).toInt

    val nodesOGRDD: RDD[ComparableNode] = sc.textFile(nodeFileOG)
      .map(line => NodeDataParser.parseNodeData(line))

    val originalGraph: Array[ComparableNode] = nodesOGRDD.collect()

    val graph = Graph(nodesRDD, edgeRDD)

    // First, identify the neighbors for each vertex
    val neighbors: RDD[(VertexId, Array[VertexId])] = graph.collectNeighborIds(EdgeDirection.Out)

    val vertexAttrs: RDD[(VertexId, ComparableNode)] = graph.vertices

    val neighborsWithAttrs: RDD[(VertexId, Array[ComparableNode])] = neighbors
      .flatMap { case (vertexId, arr) => arr.map(neighborId => (neighborId, vertexId)) }
      .join(vertexAttrs)
      .map { case (neighborId, (vertexId, comparableNode)) => (vertexId, comparableNode) }
      .groupByKey()
      .mapValues(_.toArray)


    // Convert it to a map for easy lookup
    val neighborsMap: Map[VertexId, Array[ComparableNode]] = neighborsWithAttrs.collect().toMap

    // Define accumulators for successful and failed attacks
    val successfulAttacks: LongAccumulator = sc.longAccumulator("Successful Attacks")
    val failedAttacks: LongAccumulator = sc.longAccumulator("Failed Attacks")
    val missidentifiedAttacks: LongAccumulator = sc.longAccumulator("Missidentified Attacks")
    val uneventfulAttacks: LongAccumulator = sc.longAccumulator("Uneventful Attacks")

    (1 to mitmSimConfig.simConfig.simIterations).foreach { _ =>

      val initialNodes = nodesRDD.takeSample(withReplacement = false, num = initialNodeCount).map(_._1)

      // Pregel simulation
      val pregelGraph = runPregelSimulation(graph, neighborsMap, originalGraph, initialNodes)

      // Accumulate successful and failed attacks
      pregelGraph.vertices.foreach {
        case (_, (_, _, success, fail,misidentified,uneventful)) =>
          successfulAttacks.add(success)
          failedAttacks.add(fail)
          missidentifiedAttacks.add(misidentified)
          uneventfulAttacks.add(uneventful)
      }
    }

    println(s"Total Successful Attacks: ${successfulAttacks.value}")
    println(s"Total Failed Attacks: ${failedAttacks.value}")
    println(s"Total Missidentified Attacks: ${missidentifiedAttacks.value}")
    println(s"Total Uneventful Attacks: ${uneventfulAttacks.value}")
    // Stop SparkSession
    spark.stop()
  }

  private def runPregelSimulation(
                           graph: Graph[ComparableNode, ComparableEdge],
                           neighborsMap: Map[VertexId, Array[ComparableNode]],
                           originalGraph: Array[ComparableNode],
                           initialNodes: Array[VertexId]
                         ): Graph[(Long, ComparableNode, Long, Long, Long, Long), ComparableEdge] = {

    val initialGraph: Graph[(Long, ComparableNode, Long, Long, Long, Long), ComparableEdge] = graph.mapVertices((id, e) => {


      if (initialNodes.contains(id)) {
        val nbrs = neighborsMap.getOrElse(id, Array.empty[ComparableNode])
        if (nbrs.nonEmpty) {
          (nbrs(Random.nextInt(nbrs.length)).id, e, 0L, 0L,0L,0L )
        } else {
          (Long.MaxValue, e, 0L, 0L,0L,0L)
        }
      } else {
        (Long.MaxValue, e, 0L, 0L,0L,0L)
      }
    })

    val initialMessage = new ComparableNode(
      -1,
      0,
      0,
      List(0),
      List(0),
      false,
      "null"
    )

    // Pregel function
    val pregelGraph = initialGraph.pregel(
      (Long.MaxValue, initialMessage, 0L, 0L, 0L, 0L),
      50,
      EdgeDirection.Out
    )(
      vertexProgram(originalGraph),
      triplet => sendMessage(triplet, neighborsMap),
      mergeMessage
    )

    pregelGraph
  }


}
