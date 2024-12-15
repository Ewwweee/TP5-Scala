package data

import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{JArray, JField, JInt, JObject, JString}
import org.json4s.JsonDSL.*

import java.io.{PrintWriter, StringWriter}

class Cache(override val path_cache_folder:String) extends CacheInterface(path_cache_folder) {
  private val name_actorid_to_movie:String = "/actorid_to_movie.json"
  private val name_actor_to_id:String = "/actor_to_id.json"

  private var _find_actor_id_map_primary_cache: Map[(String, String), Actor] = Map()
  private var _find_actor_movies_map_primary_cache: Map[Int, Set[Movie]] = Map()

  // Pour actor_id
  private var _find_actor_id_document:PrintWriter= PrintWriter(new StringWriter())
  private var _find_actor_id_jobject:JObject = JObject()
  private var _find_actor_id_set:Set[Actor] = Set()

  // Pour actorid_to_movie
  private var _find_actor_movies_document: PrintWriter = PrintWriter(new StringWriter())
  private var _find_actor_movies_jobject: JObject = JObject()
  private var _find_actor_movies_set: Map[Int, Set[Movie]] = Map()


  load_file_secondary_caches()

  //Quand le programme s'arrête, on ferme le doc avec le nouveau contenue
  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    try {
      this.close_all_documents()
      println("ShutdownHook exécuté avec succès.")
    } catch {
      case e: Exception =>
        println(s"Erreur dans le ShutdownHook : ${e.getMessage}")
        e.printStackTrace()
    }
  }))

  private def load_file_secondary_caches(): Unit = {
    // On get le cache de actor_ia
    val (jobject, doc) = super.get_documents(path_cache_folder + name_actor_to_id)
    _find_actor_id_jobject = jobject
    _find_actor_id_document.close()
    _find_actor_id_document = doc //Pour éviter de perdre les données si il y a un arrêt forcé
    _find_actor_id_document.println(compact(render(_find_actor_id_jobject)))
    _find_actor_id_document.close()
    _find_actor_id_set = (for {
      case JField(key, JInt(value)) <- jobject.obj
    } yield Actor(value.toInt, key.split("\\+")(0), key.split("\\+")(1))).toSet

    // On get les valeurs dans actorid_json
    val (jobject2, doc2) = super.get_documents(path_cache_folder + name_actorid_to_movie)
    _find_actor_movies_jobject = jobject2
    _find_actor_movies_document.close()
    _find_actor_movies_document = doc2
    _find_actor_movies_document.println(compact(render(_find_actor_movies_jobject)))
    _find_actor_movies_document.close()
    var mapMovies:Map[Int,List[Movie]] = Map()
    val result: List[(Int, Movie)] = (for {
      case JField(key, JArray(movies)) <- jobject2.obj
      case JObject(elem) <- movies
      case JField("id", JInt(id)) <- elem
      case JField("title", JString(title)) <- elem
    } yield (key.toInt, Movie(id.toInt, title)))

    for ((key, movie) <- result) {
      mapMovies = mapMovies.get(key) match {
        case Some(existingList) =>
          mapMovies + (key -> (movie :: existingList)) // Ajoute le film au début de la liste
        case None =>
          mapMovies + (key -> List(movie))
      }
    }

    _find_actor_movies_set = mapMovies.map((key,list) => (key,list.toSet))
  }

  private def close_all_documents() :Unit = {
    //We write the new datas in the doc

    //actor_to_id
    _find_actor_id_document = PrintWriter(path_cache_folder + name_actor_to_id)
    _find_actor_id_document.println(compact(render(_find_actor_id_jobject)))
    _find_actor_id_document.flush() // Force l'écriture dans le fichier
    _find_actor_id_document.close()

    //actorid_to_movie
    _find_actor_movies_document = PrintWriter(path_cache_folder + name_actorid_to_movie)
    _find_actor_movies_document.println(compact(render(_find_actor_movies_jobject)))
    _find_actor_movies_document.flush() // Force l'écriture dans le fichier
    _find_actor_movies_document.close()
  }

  def find_actor_id_map_primary_cache:String = _find_actor_id_map_primary_cache.mkString
  def find_actor_movies_map_primary_cache:String = _find_actor_movies_map_primary_cache.mkString

  override def get_actor_id_from_name(firstName:String,lastName:String): Option[Int] = {
    val actor = _find_actor_id_map_primary_cache.get((firstName,lastName))
    actor match {
      case None => None
      case Some(value) => Some(value.id)
    }
  }

  override def get_actor_id_from_name_secondary_cache(firstName:String,lastName:String): Option[Int] = {
    (for {
      actor <- _find_actor_id_set
      if actor.firstName==firstName && actor.lastName == lastName
    } yield actor.id).headOption
  }

  override def add_name_to_id_for_actor(firstName:String,lastName:String,id:Int):Unit = {
    _find_actor_id_map_primary_cache = _find_actor_id_map_primary_cache + ((firstName, lastName) -> Actor(id,firstName,lastName))
  }

  override def add_name_to_id_actor_secondary_cache(firstName:String,lastName:String,id:Int):Unit = {
    val query:String = firstName + "+" + lastName
    _find_actor_id_jobject =_find_actor_id_jobject ~ (query -> JInt(id))
  }

  override def get_actor_movies_from_name(actor_id:Int): Option[Set[(Int,String)]] = {
    val movies = _find_actor_movies_map_primary_cache.get(actor_id)

    movies match {
      case None => None
      case Some(value) => Some(for {
        movie <- value
      } yield (movie.id,movie.title))
    }
  }

  override def get_actor_movies_from_name_secondary_cache(actor_id:Int) : Option[Set[(Int,String)]] = {
    val movies = _find_actor_movies_set.get(actor_id)
    val result = movies match {
      case None => None
      case Some(value) => Some(for {
        movie <- value
      } yield (movie.id, movie.title))
    }
    result
  }

  override def add_id_to_movies_for_actor(actor_id:Int,movies:Set[(Int,String)]):Unit = {
    val set_movies = for {
      (movie_id,movie_title) <- movies
    } yield Movie(movie_id,movie_title)
    _find_actor_movies_map_primary_cache = _find_actor_movies_map_primary_cache + (actor_id -> set_movies)
  }

  override def add_id_to_movies_for_actor_secondary_cache(actor_id:Int,movies:Set[(Int,String)]):Unit = {
    val actor_id_json = JArray(movies.map((id, title) =>
      JObject("id" -> JInt(id), "title" -> JString(title))).toList)

   _find_actor_movies_jobject = _find_actor_movies_jobject ~ ((actor_id + "") -> actor_id_json)
  }
}