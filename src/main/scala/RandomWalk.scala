import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import scala.util.Random
import org.apache.log4j.Logger

object RandomWalk {

  val logger = Logger.getLogger(getClass.getName)


  def main(args: Array[String]): Unit = {

    logger.info("This log will go to app_logs file")

    val spark = SparkSession.builder()
      .appName("GraphSim")
      .master("local[4]") // Set master to local with 4 cores. Adjust as needed.
      .getOrCreate()

    val sc = spark.sparkContext



    val vertexArray = Array(
      (1L, "Node1"), (2L, "Node2"), (3L, "Node3"), (4L, "Node4"), (5L, "Node5"),
      (6L, "Node6"), (7L, "Node7"), (8L, "Node8"), (9L, "Node9"), (10L, "Node10"),
      (11L, "Node11"), (12L, "Node12"), (13L, "Node13"), (14L, "Node14"), (15L, "Node15"),
      (16L, "Node16"), (17L, "Node17"), (18L, "Node18"), (19L, "Node19"), (20L, "Node20")
    )

    val edgeArray = Array(
      Edge(1L, 2L, "edge12"), Edge(2L, 3L, "edge23"), Edge(3L, 4L, "edge34"), Edge(4L, 5L, "edge45"),
      Edge(5L, 6L, "edge56"), Edge(6L, 7L, "edge67"), Edge(7L, 8L, "edge78"), Edge(8L, 9L, "edge89"),
      Edge(9L, 10L, "edge910"), Edge(10L, 11L, "edge1011"), Edge(11L, 12L, "edge1112"),
      Edge(12L, 13L, "edge1213"), Edge(13L, 14L, "edge1314"), Edge(14L, 15L, "edge1415"),
      Edge(15L, 16L, "edge1516"), Edge(16L, 17L, "edge1617"), Edge(17L, 18L, "edge1718"),
      Edge(18L, 19L, "edge1819"), Edge(19L, 20L, "edge1920"), Edge(1L, 5L, "edge15"),
      Edge(2L, 7L, "edge27"), Edge(3L, 8L, "edge38"), Edge(4L, 10L, "edge410"),
      Edge(5L, 11L, "edge511"), Edge(6L, 12L, "edge612")
    )

    val vertexRDD: RDD[(Long, String)] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[Edge[String]] = sc.parallelize(edgeArray)

    val vertexRDD2: RDD[(Long, String)] = sc.parallelize(vertexArray)
    val edgeRDD2: RDD[Edge[String]] = sc.parallelize(edgeArray)

    val graph: Graph[String, String] = Graph(vertexRDD, edgeRDD)

    val graph2: Graph[String, String] = Graph(vertexRDD2, edgeRDD2)

    val initialNodes =Array(1L,5L,10L,15L,20L)

    // First, identify the neighbors for each vertex
    val neighbors: RDD[(VertexId, Array[VertexId])] = graph.collectNeighborIds(EdgeDirection.Out)

    // Convert it to a map for easy lookup
    val neighborsMap: Map[VertexId, Array[VertexId]] = neighbors.collect().toMap

    val initialGraph: Graph[Long, String] = graph.mapVertices((id, _) => {
      if (initialNodes.contains(id)) {
        val nbrs = neighborsMap.getOrElse(id, Array.empty[VertexId])
        if (nbrs.nonEmpty) {
          nbrs(Random.nextInt(nbrs.length))
        } else {
          Long.MaxValue
        }
      } else {
        Long.MaxValue
      }
    })

    val graph2objs = graph2.vertices.map { case (id, attr) => s"Node ID: $id, Attribute: $attr" }
      .collect() // This action will trigger the execution

    // Pregel function
    val pregelGraph = initialGraph.pregel(Long.MaxValue, 5, EdgeDirection.Out)(
      // Vertex Program
      (id, oldValue, newValue) =>
        {

          // map over entire graph and print it to simulate behaviour of final program also access nodename from original graph

//          graph2objs.foreach(println)


          println("id: " + id + " oldValue: " + oldValue + " newValue: " + newValue)
          if (newValue!=Long.MaxValue) {

            newValue
          }else{
            oldValue
          }
        },

      triplet => {
        if (triplet.srcAttr != Long.MaxValue) {
          if (triplet.srcAttr == triplet.dstId.toInt) {

            // Iterator((triplet.dstId, 0))
            // Retrieve the neighbors of the source vertex
            val neighbours = neighborsMap.getOrElse(triplet.dstId, Array.empty[VertexId])

            // If there are neighbors, pick a random one
            if (neighbours.nonEmpty) {
              val randomNeighbour = neighbours(Random.nextInt(neighbours.length))
              Iterator((triplet.dstId, randomNeighbour))
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

    // Merge Message
      (a, b) => {
        val values = Seq(a, b)
        values(Random.nextInt(values.size))
      }
    )

    // Print the resulting graph
    // pregelGraph.vertices.collect.foreach(println)

  }
}
