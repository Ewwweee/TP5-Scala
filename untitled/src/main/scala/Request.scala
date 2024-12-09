import TMDBCache.{Director, movieCache, saveCache}
import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.JsonDSL._
import scala.io.Source

import java.io.PrintWriter

object Request extends App{
  val apikey:String = "87551e2a0dd79b7a73f084bb30486087"
  val cache_movie_url:String = "file:./src/main/scala/cache/movie.json"
  val cache_actor_to_id_url:String = "./src/main/scala/cache/actor_to_id.json"
  val cache_actorid_to_movie:String = "./src/main/scala/cache/actorid_to_movie.json"
  var find_actor_id_map:Map[(String,String),Int] = Map();
  var find_actor_movies_map:Map[Int,Set[(Int, String)]] = Map()

  private def do_request(url:String) : org.json4s.JValue = {

    val source = Source.fromURL(url)
    val contents = source.mkString

    parse(contents)
  }

  private def seek_primary_cache[K,V](map:Map[K,V], key:K):Option[V] = {
      map.get(key)
  }

  private def get_documents(url:String):(JObject,PrintWriter) = {
    val contents = Source.fromURL("file:" + url).mkString
    val document_out = new PrintWriter(url)
    val jsonContents = parse(contents).asInstanceOf[JObject]

    (jsonContents,document_out)
  }

  def findActorId(firstName: String, lastName: String): Option[Int] = {
    println("Map : "+ find_actor_id_map)
    val query = s"$firstName+$lastName"

    val primary_cache = seek_primary_cache(find_actor_id_map,(firstName,lastName))

    primary_cache match {
      case Some(value) => return Some(value)
      case None => //ne rien faire
    }

    val (jsonContents,document_out) = get_documents(cache_actor_to_id_url)

    val elemInJson = for {
      JField(key, JInt(value)) <- jsonContents.obj
      if key == query
    } yield value.toInt

    if (elemInJson.nonEmpty) {
      println("J'utilise le cache secondaire")
      find_actor_id_map = find_actor_id_map + ((firstName,lastName) -> elemInJson.head);
      document_out.println(compact(render(jsonContents)))
      document_out.close()
      return Some(elemInJson.head)
    }

    val url = f"https://api.themoviedb.org/3/search/person?api_key=$apikey&query=$query"
    val result = do_request(url)
    val result_option = (for {
      JObject(root) <- result
      JField("results", JArray(results)) <- root
      JObject(actor) <- results
      JField("id", JInt(id)) <- actor
    } yield id.toInt).headOption

    result_option match {
      case None =>  None
      case Some(value) => {
        val updated_json = jsonContents ~ (query -> JInt(value))
        println("updated_json : " + compact(render(updated_json)))
        document_out.println(compact(render(updated_json)))
        find_actor_id_map = find_actor_id_map + ((firstName, lastName) ->value);
        document_out.close()
        Some(value)
      }
    }
  }

  def findActorMovies(actorId: Int): Set[(Int, String)] = {
    println("Map : " + find_actor_movies_map)
    val primary_value = seek_primary_cache(find_actor_movies_map,actorId)

    primary_value match {
      case Some(value) => return value
      case None =>
    }

    val (jsonContents,document_out) = get_documents(cache_actorid_to_movie)

    val elemInJson = (for {
      JField(key, JArray(value)) <- jsonContents.obj
      JObject(elem) <- value
      JField("id",JInt(id)) <- elem
      JField("title",JString(title)) <- elem
      if key == (actorId+ "")
    } yield (id.toInt,title)).toSet

    if (elemInJson.nonEmpty) {
      println("J'utilise le cache secondaire")
      find_actor_movies_map = find_actor_movies_map + (actorId -> elemInJson);
      document_out.println(compact(render(jsonContents)))
      document_out.close()
      return elemInJson
    }

    val url = f"https://api.themoviedb.org/3/person/$actorId/movie_credits?api_key=$apikey"
    val result = do_request(url)
    val final_result = (for {
      JObject(root) <- result
      JField("cast", JArray(cast)) <- root
      JObject(movie) <- cast
      JField("id", JInt(movieId)) <- movie
      JField("title", JString(title)) <- movie
    } yield (movieId.toInt, title)).toSet

    val actor_id_json = JArray(final_result.map((id,title) =>
      JObject("id" -> JInt(id), "title" -> JString(title))).toList)
    
    val updated_json = jsonContents ~ ((actorId+"") -> actor_id_json)
//    console.log("updated_json " + updated_json)
    document_out.println(compact(render(updated_json)))
    document_out.close()
    find_actor_movies_map = find_actor_movies_map + (actorId -> final_result)

    final_result
  }

  def findMovieDirector(movieId: Int): Option[Director] = {
    // Check the cache first
//    movieCache.get(movieId) match {
//      case Some(director) => Some(director) // Return cached result
//      case None =>
        // If not in cache, fetch from the API
        val url = f"https://api.themoviedb.org/3/movie/$movieId/credits?api_key=$apikey"
        val result = Request.do_request(url)
        val director = (for {
          JObject(root) <- result
          JField("crew", JArray(crew)) <- root
          JObject(member) <- crew
          JField("job", JString(job)) <- member if job == "Director"
          JField("id", JInt(id)) <- member
          JField("name", JString(name)) <- member
        } yield Director(id.toInt, name)).headOption

//        // Update the cache and save it to the file
//        director.foreach { dir =>
//          movieCache += (movieId -> dir)
//          saveCache()
//        }
        director
//    }
  }

  type FullName = (String,String)

  def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    // Helper URLs for actor searches
    val url1: String = f"https://api.themoviedb.org/3/search/person?api_key=$apikey&query=${actor1._1}+${actor1._2}"
    val url2: String = f"https://api.themoviedb.org/3/search/person?api_key=$apikey&query=${actor2._1}+${actor2._2}"

    // Fetch actor IDs from the API
    val result1 = do_request(url1)
    val result2 = do_request(url2)

    val actor1Id = findActorId(actor1._1,actor1._2)

    val actor2Id = findActorId(actor2._1,actor2._2)

    actor1Id match {
      case None => return Set.empty
      case _ => 
    }
    actor2Id match
      case None => return Set.empty
      case _ =>

    // Fetch movies for both actors
    val moviesActor1 = findActorMovies(actor1Id.get)
    val moviesActor2 = findActorMovies(actor2Id.get)

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
