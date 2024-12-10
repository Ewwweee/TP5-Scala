import TMDBCache.{Director, movieCache, saveCache}
import org.json4s.*
import org.json4s.native.JsonMethods.*

import scala.io.Source
import java.io.{File, PrintWriter}

object Request extends App{
  private val apikey:String = "87551e2a0dd79b7a73f084bb30486087"
  private def do_request(url:String) : org.json4s.JValue = {
    val source = Source.fromURL(url)
    val contents = source.mkString

    parse(contents)
  }

  private def extractFirstActorId(jsonValue: JValue): Option[Int] = {
    (for {
      case JObject(child) <- jsonValue
      case JField("results", JArray(results)) <- child
      case JObject(actor) <- results
      case JField("id", JInt(id)) <- actor
    } yield id.toInt).headOption
  }

  def findActorId(firstName: String, lastName: String): Option[Int] = {
    val query = s"$firstName+$lastName"
    val url = f"https://api.themoviedb.org/3/search/person?api_key=$apikey&query=$query"
    val result = do_request(url)
    extractFirstActorId(result)
  }

  def findMovieDirector(movieId: Int): Option[Director] = {
    // Check the cache first
    movieCache.get(movieId) match {
      case Some(director) => Some(director) // Return cached result
      case None =>
        // If not in cache, fetch from the API
        val url = f"https://api.themoviedb.org/3/movie/$movieId/credits?api_key=$apikey"
        val result = Request.do_request(url)
        val director = (for {
          case JObject(root) <- result
          case JField("crew", JArray(crew)) <- root
          case JObject(member) <- crew
          case JField("job", JString(job)) <- member if job == "Director"
          case JField("id", JInt(id)) <- member
          case JField("name", JString(name)) <- member
        } yield Director(id.toInt, name)).headOption

        // Update the cache and save it to the file
        director.foreach { dir =>
          movieCache += (movieId -> dir)
          saveCache()
        }
        director
    }
  }

  def fetchMovies(actorId: Int): Set[(Int, String)] = {
    val url = f"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=$apikey"
    val result = do_request(url)
    (for {
      case JObject(child) <- result
      case JField("cast", JArray(cast)) <- child
      case JObject(movie) <- cast
      case JField("id", JInt(id)) <- movie
      case JField("title", JString(title)) <- movie
    } yield (id.toInt, title)).toSet
  }

  private type FullName = (String, String)

  def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    // Helper URLs for actor searches
    val url1: String = f"https://api.themoviedb.org/3/search/person?api_key=$apikey&query=${actor1._1}+${actor1._2}"
    val url2: String = f"https://api.themoviedb.org/3/search/person?api_key=$apikey&query=${actor2._1}+${actor2._2}"

    // Fetch actor IDs from the API
    val result1 = do_request(url1)
    val result2 = do_request(url2)

    val actor1Id = extractFirstActorId(result1)
    val actor2Id = extractFirstActorId(result2)

    // If either actor is not found, return an empty set
    if (actor1Id.isEmpty || actor2Id.isEmpty) return Set.empty

    // Fetch movies for both actors
    val moviesActor1 = fetchMovies(actor1Id.get)
    val moviesActor2 = fetchMovies(actor2Id.get)

    // Find shared movies
    val sharedMovies = moviesActor1.intersect(moviesActor2)

    // Find directors for shared movies and map them
    val collaboration = sharedMovies.flatMap { case (movieId, movieTitle) =>
      findMovieDirector(movieId).map { director =>
        (director.name, movieTitle) // Map director's name and movie title
      }
    }
    collaboration
  }

}
