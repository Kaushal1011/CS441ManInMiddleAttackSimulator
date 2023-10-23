import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD

object Main {
  def main(args: Array[String]): Unit = {
//    if (args.length < 2) {
//      println("Usage: WordCount <input-path> <output-path>")
//      System.exit(1)
//    }

    val inputPath = "./build.sbt"
    val outputPath = "./sparkout"

    val spark: SparkSession
    = SparkSession.builder()
      .appName("WordCount")
      .master("local[4]") // Set master to local with 4 cores. Adjust as needed.
      .getOrCreate()

    val textFile: RDD[String] = spark.sparkContext.textFile(inputPath)

    val counts: RDD[(String, Int)] = textFile
      .flatMap(line => line.split("\\s+"))
      .map(word => (word.toLowerCase, 1))
      .reduceByKey(_ + _)

    counts.saveAsTextFile(outputPath)

    spark.stop()
  }
}
