import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx._
import helpers.{ComparableEdge, ComparableNode, NodeDataParser}
import scala.util.Random
import org.apache.log4j.Logger


object Main {
  val logger = Logger.getLogger("CS441HW2MitM")

//  println(Main.getClass.getName)

  logger.info("This log will go to app_logs file")

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("GraphSim")
      .master("local[4]") // Set master to local with 4 cores. Adjust as needed.
      .getOrCreate()

    val sc = spark.sparkContext

    val nodeFileOG = "./input/nodes.txt"

    val nodeFilePath = "./input/nodesPerturbed.txt"

    // Read the file and parse each line to create nodes
    val nodesRDD: RDD[(VertexId, ComparableNode)] = sc.textFile(nodeFilePath)
      .map(line => (NodeDataParser.parseNodeData(line).id,NodeDataParser.parseNodeData(line)))

    val edgeFilePath = "./input/edgesPerturbed.txt"

    val edgeRDD: RDD[Edge[ComparableEdge]] = sc.textFile(edgeFilePath)
      .map(line => Edge(NodeDataParser.parseEdgeData(line).srcId,NodeDataParser.parseEdgeData(line).dstId, NodeDataParser.parseEdgeData(line)))


    val initialNodes =Array(1L)

    val nodesOGRDD: RDD[ComparableNode] = sc.textFile(nodeFileOG)
      .map(line => NodeDataParser.parseNodeData(line))

    val originalGraph = nodesOGRDD.collect()

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

    // Neighbour to visit, Attribute of the node, successful attacks, failed attacks
    val initiaGraph: Graph[(Long, ComparableNode,Long,Long),ComparableEdge] = graph.mapVertices((id, e) => {
      if (initialNodes.contains(id)) {
        val nbrs = neighborsMap.getOrElse(id, Array.empty[ComparableNode])
        if (nbrs.nonEmpty) {
          (nbrs(Random.nextInt(nbrs.length)).id,e,0L,0L)
        } else {
          (Long.MaxValue,e,0L,0L)
        }
      } else {
        (Long.MaxValue,e,0L,0L)
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

    val pregelGraph = initiaGraph.pregel(
      (Long.MaxValue, initialMessage , 0L, 0L ),
      20,
      EdgeDirection.Out)(

      // Vertex Program
      (id, oldValue, newValue) =>
        {
          if (newValue._1!=Long.MaxValue) {

            // attr always stays the same
            // number of sucessful attacks and failed attacks are updated
            // based on similarity check and attack decision

            println("id: " + id + " oldValue: " + oldValue + " newValue: " + newValue)

            (newValue._1,oldValue._2,newValue._3,newValue._4)
          }else{
            (oldValue._1,oldValue._2,oldValue._3,oldValue._4)
          }
        },

      triplet => {
        if (triplet.srcAttr._1 != Long.MaxValue) {
          if (triplet.srcAttr._1 == triplet.dstId.toInt) {

            // Iterator((triplet.dstId, 0))
            // Retrieve the neighbors of the source vertex

            val neighbours = neighborsMap.getOrElse(triplet.dstId, Array.empty[ComparableNode])

            // If there are neighbors, pick a random one
            if (neighbours.nonEmpty) {
              val randomNeighbour = neighbours(Random.nextInt(neighbours.length)).id
              Iterator(
                (triplet.dstId, (randomNeighbour,triplet.dstAttr._2.asInstanceOf[ComparableNode],triplet.dstAttr._3,triplet.dstAttr._4)),
              )
            } else {
              Iterator.empty
            }
          }
          else {
            Iterator.empty
          }
        } else {
          Iterator.empty
        }
      },

    (a, b) => {
      val values = Seq(a, b)
      values(Random.nextInt(values.size))
    }

    )



    // Stop SparkSession
    spark.stop()
  }
}
