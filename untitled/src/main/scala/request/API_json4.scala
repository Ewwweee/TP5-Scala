package request

import scala.io.Source
import org.json4s.native.JsonMethods.*
import org.json4s.*
import data.FullName

trait API_json4(val apiKey:String) {
  private def do_request(url: String): org.json4s.JValue = {

    val source = Source.fromURL(url)
    parse(source.mkString)
  }

  def findActorId(firstName: String, lastName: String): Option[Int] = {
    val query = s"$firstName+$lastName"

    val url = f"https://api.themoviedb.org/3/search/person?api_key=$apiKey&query=$query"
    val result = do_request(url)

    val result_option = (for {
      case JObject(root) <- result
      case JField("results", JArray(results)) <- root
      case JObject(actor) <- results
      case JField("id", JInt(id)) <- actor
    } yield id.toInt).headOption
    result_option
  }

  def findActorMovies(actorId: Int): Set[(Int, String)] = {
    val url = f"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=$apiKey"
    val result = do_request(url)
    val final_result = (for {
      case JObject(root) <- result
      case JField("cast", JArray(cast)) <- root
      case JObject(movie) <- cast
      case JField("id", JInt(movieId)) <- movie
      case JField("title", JString(title)) <- movie
    } yield (movieId.toInt, title)).toSet
    final_result
  }

  def findMovieDirector(movieId: Int): Option[(Int, String)] = {
    val url = f"https://api.themoviedb.org/3/movie/$movieId/credits?api_key=$apiKey"
    val result = do_request(url)
    val director = (for {
      case JObject(root) <- result
      case JField("crew", JArray(crew)) <- root
      case JObject(member) <- crew
      case JField("job", JString(job)) <- member if job == "Director"
      case JField("id", JInt(id)) <- member
      case JField("name", JString(name)) <- member
    } yield (id.toInt, name)).headOption
    director
  }

  def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {

    val actor1_id = findActorId(actor1.firstName, actor1.lastName)

    actor1_id match {
      case None => return Set.empty
      case _ =>
    }

    // We seek all the movies he played in
    val moviesActor1 = findActorMovies(actor1_id.get)

    val actor2_id = findActorId(actor2.firstName, actor2.lastName)
    actor2_id match {
      case None => return Set.empty
      case _ =>
    }
    // We seek all the movies he played in
    val moviesActor2 = findActorMovies(actor2_id.get)
    // Find shared movies
    val sharedMovies = moviesActor1.intersect(moviesActor2)

    // Find directors for shared movies and map them
    val collaboration = sharedMovies.flatMap { case (movieId, movieTitle) =>
      findMovieDirector(movieId).map { (directorid, name) =>
        (name, movieTitle) // Map director's name and movie title
      }
    }
    collaboration
  }
}
