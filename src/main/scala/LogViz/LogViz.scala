package LogViz
import scala.io.Source
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

// process logs from the app for multiple iterations and makes it into atomic log files that can be visualized
// by the log visualizer created by @kaushal1011
object LogViz {

  def main(args: Array[String]): Unit = {
    // example usage
    val sourcePath = "./logs/app_logs"  // Change this to the path of your log file
    val outputPath = "./logs/logs4vis"     // Change this to the path of your desired output folder

    val lines = Source.fromFile(sourcePath).getLines.toList

    var currentIteration: Option[Int] = None
    var currentLogs: List[String] = List()

    lines.foreach { line =>
      if (line.contains("Starting iteration")) {
        currentIteration = Some(line.split(",")(1).trim.toInt)
        // currentLogs = List(line)  // initialize with the starting line
      } else if (line.contains("Finished iteration") && currentIteration.isDefined) {
        // currentLogs = currentLogs :+ line // append the finished line

        // Save logs of the current iteration to a file
        val filename = s"$outputPath/iteration_${currentIteration.get}.log"
        Files.write(Paths.get(filename), currentLogs.mkString("\n").getBytes(StandardCharsets.UTF_8))

        // reset
        currentLogs = List()
        currentIteration = None
      } else if (currentIteration.isDefined) {
        currentLogs = currentLogs :+ line
      }
    }
  }
}
