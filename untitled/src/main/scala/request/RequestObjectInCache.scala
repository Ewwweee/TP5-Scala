package request

import data.{Actor, FullName,Cache}
import org.json4s.{JArray, JField, JInt, JObject, JString}
import org.json4s.native.JsonMethods.{compact, parse, render}

import java.io.PrintWriter
import scala.io.Source

//TO DO : Changer le
class RequestObjectInCache(override val apiKey:String,val cache:Cache) extends API_json4(apiKey) {
  val cache_movie_url: String = "path_cache" + "/movie.json"// path_cache = ./src/main/scala/cache"
  val cache_actor_to_id_url: String = "path_cache" + "/actor_to_id.json"// "./src/main/scala/cache/actor_to_id.json"
  val cache_actorid_to_movie: String ="path_cache" + "/actorid_to_movie.json" //"./src/main/scala/cache/actorid_to_movie.json"
  var find_actor_id_map: Map[(String, String), Actor] = Map();
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
    println("Map : " + cache.find_actor_id_map_primary_cache)
    val query = s"$firstName+$lastName"

    val primary_cache = cache.get_actor_id_from_name(firstName, lastName)

    primary_cache match {
      case Some(value) => println("J'utilise le cache primaire");return Some(value)
      case None => //ne rien faire
    }

    val secondary_cache = cache.get_actor_id_from_name_secondary_cache(firstName, lastName)

    secondary_cache match {
      case Some(value) =>
        println("J'utilise le cache secondaire")
        cache.add_name_to_id_for_actor(firstName,lastName,value)
        return Some(value)
      case None => //ne rien faire
    }
    val result_option = super.findActorId(firstName, lastName)

    result_option match {
      case None => None
      case Some(value) =>
        cache.add_name_to_id_for_actor(firstName, lastName,value)
        cache.add_name_to_id_actor_secondary_cache(firstName,lastName,value)
        Some(value)

    }
  }

  override def findActorMovies(actorId: Int): Set[(Int, String)] = {
    println("Map : " + cache.find_actor_movies_map_primary_cache)
    val primary_value = cache.get_actor_movies_from_name(actorId)

    primary_value match {
      case Some(value) =>println("J'utilise le cache primaire"); return value
      case None => //ne fait rien
    }

    val secondary_cache = cache.get_actor_movies_from_name_secondary_cache(actorId)

    secondary_cache match
      case None => //ne fait rien
      case Some(value) =>
        println("J'utilise le cache secondaire")
        cache.add_id_to_movies_for_actor_secondary_cache(actorId,value)
        return value

    val final_result = super.findActorMovies(actorId)

    cache.add_id_to_movies_for_actor(actorId,final_result)
    cache.add_id_to_movies_for_actor_secondary_cache(actorId,final_result)
    final_result
  }
//
  override def findMovieDirector(movieId: Int): Option[(Int, String)] = {
    //TO DO : With JSON
    super.findMovieDirector(movieId)
  }
//
  override def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    super.collaboration(actor1, actor2)
  }
}
