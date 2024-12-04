import org.json4s._; import org.json4s.native.JsonMethods._
import scala.io.Source
import java.io.PrintWriter

object Request extends App{
  val apikey:String = "87551e2a0dd79b7a73f084bb30486087"
  val cache_movie_url:String = "file:./src/main/scala/cache/movie.json"
  val cache_actor_url:String = "file:./src/main/scala/cache/actor.json"

  private def do_request(url:String) : org.json4s.JValue = {
    val source = Source.fromURL(url)
    val contents = source.mkString

    parse(contents)
  }

  def findActorId(firstName: String, lastName: String): Option[Int] = {
//    val query = s"$firstName+$lastName"
//    val contents = Source.fromURL(cache_actor_url).mkString

//    val jsonContents = parse(contents).asInstanceOf[JObject]
    
    val url = f"https://api.themoviedb.org/3/search/person?api_key=$apikey&query=$query"
    val result = do_request(url)
    (for {
      JObject(root) <- result
      JField("results", JArray(results)) <- root
      JObject(actor) <- results
      JField("id", JInt(id)) <- actor
    } yield id.toInt).headOption
  }

  def findActorMovies(actorId: Int): Set[(Int, String)] = {
    val url = f"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=$apikey"
    val result = do_request(url)
    (for {
      JObject(root) <- result
      JField("cast", JArray(cast)) <- root
      JObject(movie) <- cast
      JField("id", JInt(movieId)) <- movie
      JField("title", JString(title)) <- movie
    } yield (movieId.toInt, title)).toSet
  }

  def findMovieDirector(movieId: Int): Option[(Int,String)] = {
    val url:String = f"https://api.themoviedb.org/3/movie/$movieId/credits?api_key=$apikey"

    val result = do_request(url)

    val list_filtered = for {JObject(child) <- result
                   JField("crew", JArray(crew)) <- child
                   JObject(crewMember) <- crew
                   JField("job", JString(job)) <-crewMember
                   JField("id",JInt(id)) <-crewMember
                   JField("name",JString(name)) <- crewMember
      if job=="Director"} yield (id.intValue,name)

    list_filtered.headOption
  }
  type FullName = (String,String)

  def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    val url1:String = f"https://api.themoviedb.org/3/search/person?api_key=$apikey&query=${actor1._1}+${actor1._2}"
    val url2:String = f"https://api.themoviedb.org/3/search/person?api_key=$apikey&query=${actor2._1}+${actor2._2}"

    val result1 = do_request(url1)
    val result2 = do_request(url2)

    val actor1_id = for {JObject(child) <- result1
                             JField("id",JInt(id)) <- child} yield id.toInt

    // We seek all the movies he played in
    val movies_actor1_url = f"https://api.themoviedb.org/3/person/${actor1_id.head}/movie_credits?api_key=${apikey}"
    val all_result = do_request(movies_actor1_url)
    val all_movies_actor1 = for {JObject(child) <- all_result
                             JField("cast",JArray(cast)) <- child
                             JObject(movie) <- cast
                             JField("id",JInt(id)) <- movie
                             JField("title",JString(title)) <-movie} yield (id.toInt,title)

    val actor2_id = for {JObject(child) <- result2
                         JField("id", JInt(id)) <- child} yield id.toInt

    // We seek all the movies he played in
    val movies_actor2_url = f"https://api.themoviedb.org/3/person/${actor2_id.head}/movie_credits?api_key=${apikey}"
    val all_result2 = do_request(movies_actor2_url)
    val all_movies_actor2 = for {JObject(child) <- all_result2
                                 JField("cast", JArray(cast)) <- child
                                 JObject(movie) <- cast
                                 JField("id", JInt(id)) <- movie
                                 JField("title", JString(title)) <- movie} yield (id.toInt, title)

    val collaboration = for {elem <- all_movies_actor1
                             elem2 <- all_movies_actor2
                             if elem._1 == elem2._1} yield (findMovieDirector(elem._1).map((x,y) => y),elem._2)

    val filtered_collab = collaboration.filter {
      case (Some(_), _) => true // Garder l'élément si le réalisateur est présent
      case (None, _) => false // Exclure si le réalisateur est None
    }.map {
      case (Some(director), movieTitle) => (director, movieTitle) // Extraire le réalisateur et le titre du film
    }
    filtered_collab.toSet
  }

}
