package data

import org.json4s.JObject
import org.json4s._

implicit val formats: Formats = DefaultFormats

class Movie(private val _id:Int,private val _title:String) {
  val _director:Option[Director] = None
  def id:Int = _id
  def title:String = _title
  def director:Option[Director] = _director
}

// Conversion implicite pour convertir JObject en Movie
object Movie {
  def conversionJObjectMovie(jObject: JObject): Movie = {
    val id = (jObject \ "id").extract[Int]
    val title = (jObject \ "title").extract[String]
    new Movie(id, title)
  }
}