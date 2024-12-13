package request

import data.FullName
import org.json4s.*
import org.json4s.native.JsonMethods.*
import org.json4s.JsonDSL.*

import scala.io.Source
import java.io.PrintWriter

class RequestJSONCache(override val apiKey:String) extends API_json4(apiKey) {
  val cache_movie_url: String = "file:./src/main/scala/cache/movie.json"
  val cache_actor_to_id_url: String = "./src/main/scala/cache/actor_to_id.json"
  val cache_actorid_to_movie: String = "./src/main/scala/cache/actorid_to_movie.json"
  var find_actor_id_map: Map[(String, String), Int] = Map();
  var find_actor_movies_map: Map[Int, Set[(Int, String)]] = Map()

  private def seek_primary_cache[K, V](map: Map[K, V], key: K): Option[V] = {
    map.get(key)
  }

  private def get_documents(url: String): (JObject, PrintWriter) = {
    val source = Source.fromURL("file:" + url)
    try {
      val contents = source.mkString
      val document_out = new PrintWriter(url)
      val jsonContents = parse(contents).asInstanceOf[JObject]

      (jsonContents, document_out)
    } finally {
      source.close() // Libère les ressources
    }

  }

  override def findActorId(firstName: String, lastName: String): Option[Int] = {
    println("Map : " + find_actor_id_map)
    val query = s"$firstName+$lastName"

    val primary_cache = seek_primary_cache(find_actor_id_map, (firstName, lastName))

    primary_cache match {
      case Some(value) => println("J'utilise le cache primaire");return Some(value)
      case None => //ne rien faire
    }

    val (jsonContents, document_out) = get_documents(cache_actor_to_id_url)

    val elemInJson = for {
      case JField(key, JInt(value)) <- jsonContents.obj
      if key == query
    } yield value.toInt

    if (elemInJson.nonEmpty) {
      println("J'utilise le cache secondaire")
      find_actor_id_map = find_actor_id_map + ((firstName, lastName) -> elemInJson.head);
      document_out.println(compact(render(jsonContents)))
      document_out.close()
      return Some(elemInJson.head)
    }

    val result_option = super.findActorId(firstName, lastName)

    result_option match {
      case None => None
      case Some(value) => {
        val updated_json = jsonContents ~ (query -> JInt(value))
        println("updated_json : " + compact(render(updated_json)))
        document_out.println(compact(render(updated_json)))
        find_actor_id_map = find_actor_id_map + ((firstName, lastName) -> value);
        document_out.close()
        Some(value)
      }
    }
  }

  override def findActorMovies(actorId: Int): Set[(Int, String)] = {
    println("Map : " + find_actor_movies_map)
    val primary_value = seek_primary_cache(find_actor_movies_map, actorId)

    primary_value match {
      case Some(value) =>println("J'utilise le cache primaire"); return value
      case None =>
    }

    val (jsonContents, document_out) = get_documents(cache_actorid_to_movie)

    val elemInJson = (for {
      case JField(key, JArray(value)) <- jsonContents.obj
      case JObject(elem) <- value
      case JField("id", JInt(id)) <- elem
      case JField("title", JString(title)) <- elem
      if key == (actorId + "")
    } yield (id.toInt, title)).toSet

    if (elemInJson.nonEmpty) {
      println("J'utilise le cache secondaire")
      find_actor_movies_map = find_actor_movies_map + (actorId -> elemInJson);
      document_out.println(compact(render(jsonContents)))
      document_out.close()
      return elemInJson
    }

    val final_result = super.findActorMovies(actorId)

    val actor_id_json = JArray(final_result.map((id, title) =>
      JObject("id" -> JInt(id), "title" -> JString(title))).toList)

    val updated_json = jsonContents ~ ((actorId + "") -> actor_id_json)
    //    console.log("updated_json " + updated_json)
    document_out.println(compact(render(updated_json)))
    document_out.close()
    find_actor_movies_map = find_actor_movies_map + (actorId -> final_result)

    final_result
  }

  override def findMovieDirector(movieId: Int): Option[(Int, String)] = {
    //TO DO : With JSON
    super.findMovieDirector(movieId)
  }

  override def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    super.collaboration(actor1, actor2)
  }
}
