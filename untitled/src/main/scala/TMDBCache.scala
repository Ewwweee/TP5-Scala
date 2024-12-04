import Request.findMovieDirector
import java.io.{File, PrintWriter}
import scala.io.Source
import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

object TMDBCache {
  implicit val formats: Formats = DefaultFormats
  val cacheDir: String = "cache"
  val cacheFile: String = s"src/main/scala/cache/movie.json"

  // Case class for a director
  case class Director(id: Int, name: String)

  // Ensure the cache directory and file exist
  private def ensureCacheFile(): Unit = {
    val dir = new File(cacheDir)
    if (!dir.exists()) dir.mkdirs() // Create directory if it doesn't exist

    val file = new File(cacheFile)
    if (!file.exists()) {
      val writer = new PrintWriter(file)
      writer.write("{}") // Initialize with an empty JSON object
      writer.close()
    }
  }

  // Load cache into memory at startup
  var movieCache: Map[Int, Director] = {
    ensureCacheFile()
    val source = Source.fromFile(cacheFile)
    val contents = source.mkString
    source.close()
    parse(contents).extract[Map[Int, Director]]
  }

  def saveCache(): Unit = {
    val sortedCache = movieCache.toSeq.sortBy(_._1).toMap
    val writer = new PrintWriter(new File(cacheFile))
    writer.write(write(sortedCache)) // Save cache with descriptive keys
    writer.close()
  }
}

// Test class
object TMDBCacheTest extends App {
  import TMDBCache._

  // Example: Find director for a movie
  val movieId = 272 // Example movie ID
  val director = findMovieDirector(movieId)
  println(s"Director of movie $movieId: $director")

  // Example: Call again to demonstrate caching
  val cachedDirector = findMovieDirector(movieId)
  println(s"Cached Director of movie $movieId: $cachedDirector")
}
