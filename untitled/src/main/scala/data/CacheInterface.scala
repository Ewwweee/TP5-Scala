package data

import org.json4s.JObject

import org.json4s.*
import org.json4s.native.JsonMethods.*
import java.io.PrintWriter
import scala.io.Source

trait CacheInterface(val path_cache_folder:String) {
  def get_actor_id_from_name(firstName:String,lastName:String): Option[Int]
  def add_name_to_id_for_actor(firstName:String,lastName:String,id:Int):Unit
  def get_actor_movies_from_name(actor_id:Int): Option[Set[(Int,String)]]
  def add_id_to_movies_for_actor(actor_id:Int,movies:Set[(Int,String)]):Unit
  def get_actor_id_from_name_secondary_cache(firstName:String,lastName:String): Option[Int]
  def add_name_to_id_actor_secondary_cache(firstName:String,lastName:String,id:Int):Unit
  def get_actor_movies_from_name_secondary_cache(actor_id:Int) : Option[Set[(Int,String)]]
  def add_id_to_movies_for_actor_secondary_cache(actor_id:Int,movies:Set[(Int,String)]):Unit
  protected def get_documents(url: String): (JObject, PrintWriter) = {
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
}
