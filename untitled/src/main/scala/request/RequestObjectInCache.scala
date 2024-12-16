package request

import data.{Actor, FullName,CacheInterface}
import org.json4s.{JArray, JField, JInt, JObject, JString}
import org.json4s.native.JsonMethods.{compact, parse, render}

import java.io.PrintWriter
import scala.io.Source

//TO DO : Changer le
class RequestObjectInCache(override val apiKey:String,val cache:CacheInterface) extends API_json4(apiKey) {

  override def findActorId(firstName: String, lastName: String): Option[Int] = {
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
    val primary_cache = cache.get_director_movies_primary_cache(movieId)

    primary_cache match {
      case None => //ne fait rien
      case Some(value) =>
        println("J'utilise le cache primaire");
        return Some(value)
    }

    val secondary_cache = cache.get_director_movies_secondary_cache(movieId)
    secondary_cache match {
      case None => //ne fait rien
      case Some(value) =>
        println("J'utilise le cache secondaire")
        cache.add_director_primary_cache(movieId = movieId, directorId = value._1, directorName = value._2)
        return Some(value)
    }
    
    val final_result = super.findMovieDirector(movieId)
    final_result match {
      case None => None
      case Some(value) => 
        cache.add_director_primary_cache(movieId,value._1,value._2)
        cache.add_director_secondary_cache(movieId,value._1,value._2)
        Some(value)
    }
  }

  override def collaboration(actor1: FullName, actor2: FullName): Set[(String, String)] = {
    super.collaboration(actor1, actor2)
  }
}
